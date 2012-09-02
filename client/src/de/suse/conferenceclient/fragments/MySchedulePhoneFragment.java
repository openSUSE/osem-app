package de.suse.conferenceclient.fragments;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockListFragment;

import de.suse.conferenceclient.R;
import de.suse.conferenceclient.SUSEConferences;
import de.suse.conferenceclient.activities.ScheduleDetailsActivity;
import de.suse.conferenceclient.adapters.PhoneScheduleAdapter;
import de.suse.conferenceclient.adapters.PhoneScheduleAdapter.ScheduleItem;
import de.suse.conferenceclient.app.Database;
import de.suse.conferenceclient.models.Event;

public class MySchedulePhoneFragment extends SherlockListFragment {
	public interface OnGetEventsListener {
		public List<Event> getEvents();
	}
	
	public MySchedulePhoneFragment() { }

	private Database db;
    private long mConferenceId;
    private List<Event> mEventList;
    private DateFormat mHeaderFormatter;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mHeaderFormatter = DateFormat.getDateInstance(DateFormat.LONG);		
		Bundle args = getArguments();
		boolean isEmpty = false;
		boolean conflict = false;
		this.mConferenceId = args.getLong("conferenceId");
		this.db = SUSEConferences.getDatabase();
		this.mEventList = db.getScheduleTitles(mConferenceId);
		List<ScheduleItem> items = new ArrayList<ScheduleItem>();

		if (mEventList.size() > 0) {
			ScheduleItem newItem = new ScheduleItem(buildHeaderText(mEventList.get(0)));
			items.add(newItem);
			Event event;
			Event previousEvent = null;
			Event nextEvent = null;
			
			int eventLen = mEventList.size();
			for (int i = 0; i < eventLen; i++) {
				event = mEventList.get(i);
				if (i + 1 != eventLen)
					nextEvent = mEventList.get(i + 1);
				else
					nextEvent = null;
				
				conflict = false;
				isEmpty = true;
				Log.d("SUSEConferences", "Event: " + event.getDate());
				if (previousEvent != null) {
					// If the time slot is the same as the one before,
					// and the event is *not* in My Schedule, skip it
					if (sameTime(previousEvent.getDate(), event.getDate())) {
						if (!event.isInMySchedule()) {
							previousEvent = event;
							continue;
						}
					}
										
					// Now, check if the times overlap *and* both events are 
					// in the schedule
					if (timesOverlap(previousEvent.getDate(),
									 previousEvent.getEndDate(),
									 event.getDate(),
									 event.getEndDate()) && previousEvent.isInMySchedule() && event.isInMySchedule()) {
						conflict = true;
						items.get(items.size() - 1).setConflict(true);
					}
					
					// If we've hit a new day, add a header item to the list
					if (!sameDay(previousEvent.getDate(), event.getDate())) {
						ScheduleItem newHeader= new ScheduleItem(buildHeaderText(event));
						items.add(newHeader);
					}
				}
				
				// Duplicate slots
				if (nextEvent != null) {
					if (sameTime(nextEvent.getDate(), event.getDate()) && !event.isInMySchedule()) {
						continue;
					}
				}
				
				previousEvent = event;
				if (event.isInMySchedule()) {
					isEmpty = false;
					Log.d("SUSEConferences", "Event id in My Sched: " + event.getSqlId());
				}
				
				ScheduleItem newEvent = new ScheduleItem(event, isEmpty);
				newEvent.setConflict(conflict);
				items.add(newEvent);
			}

		}
		
		PhoneScheduleAdapter adapter = new PhoneScheduleAdapter(getActivity(),
																R.layout.schedule_list_item,
																getResources().getColor(R.color.dark_suse_green),
																getResources().getColor(R.color.suse_grey),
																items);
		setListAdapter(adapter);
	}
	
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getListView().setDrawSelectorOnTop(true);
	}

	private String buildHeaderText(Event event) {
		return mHeaderFormatter.format(event.getDate());
	}
	
	private boolean timesOverlap(Date s1, Date e1, Date s2, Date e2) {
		return (s1.before(e2) && s2.before(e1));
	}
	
	private boolean sameDay(Date day1, Date day2) {
		Calendar cal1 = Calendar.getInstance();
		Calendar cal2 = Calendar.getInstance();
		cal1.setTime(day1);
		cal2.setTime(day2);
		boolean sameDay = cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
				cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
		return sameDay;
	}

	private boolean sameTime(Date day1, Date day2) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(day1);
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int minute = cal.get(Calendar.MINUTE);
		cal.setTime(day2);
		int hour2 = cal.get(Calendar.HOUR_OF_DAY);
		int minute2 = cal.get(Calendar.MINUTE);
		return (hour == hour2 && minute == minute2);
	}
	
	@Override
	public void onListItemClick (ListView l,
								 View v,
								 int position,
								 long id) {
		ScheduleItem item = (ScheduleItem) l.getItemAtPosition(position);
		if (item.isHeader())
			return;
		
		Intent intent = new Intent(getActivity(), ScheduleDetailsActivity.class);
		intent.putExtra("eventId", item.getEvent().getSqlId());
		intent.putExtra("conferenceId", mConferenceId);
		startActivity(intent);
	}

}
