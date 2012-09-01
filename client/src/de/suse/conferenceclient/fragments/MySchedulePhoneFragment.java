package de.suse.conferenceclient.fragments;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.os.Bundle;
import android.util.Log;

import com.actionbarsherlock.app.SherlockListFragment;

import de.suse.conferenceclient.R;
import de.suse.conferenceclient.SUSEConferences;
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
		this.mConferenceId = args.getLong("conferenceId");
		this.db = SUSEConferences.getDatabase();
		this.mEventList = db.getScheduleTitles(mConferenceId);
		List<ScheduleItem> items = new ArrayList<ScheduleItem>();

		if (mEventList.size() > 0) {
			ScheduleItem newItem = new ScheduleItem(buildHeaderText(mEventList.get(0)));
			items.add(newItem);
			Event previousEvent = null;
			for (Event event : mEventList) {
				isEmpty = false;

				if (previousEvent != null) {
					if (!sameDay(previousEvent.getDate(), event.getDate())) {
						ScheduleItem newHeader= new ScheduleItem(buildHeaderText(event));
						items.add(newHeader);
					}
				}
				previousEvent = event;
				if (!event.isInMySchedule()) {
						isEmpty = true;
				}

				ScheduleItem newEvent = new ScheduleItem(event, isEmpty);
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

	private String buildHeaderText(Event event) {
		return mHeaderFormatter.format(event.getDate());
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

}
