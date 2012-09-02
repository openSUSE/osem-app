package de.suse.conferenceclient.fragments;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockListFragment;

import de.suse.conferenceclient.R;
import de.suse.conferenceclient.SUSEConferences;
import de.suse.conferenceclient.activities.HomeActivity;
import de.suse.conferenceclient.activities.ScheduleDetailsActivity;
import de.suse.conferenceclient.activities.SocialActivity;
import de.suse.conferenceclient.adapters.PhoneScheduleAdapter;
import de.suse.conferenceclient.adapters.PhoneScheduleAdapter.ScheduleItem;
import de.suse.conferenceclient.app.Database;
import de.suse.conferenceclient.models.Event;

public class SchedulePhoneFragment extends SherlockListFragment {
	public interface OnGetEventsListener {
		public List<Event> getEvents();
	}
	
	public SchedulePhoneFragment() { }

	private Database db;
    private long mConferenceId;
    private List<Event> mEventList;
    private DateFormat mHeaderFormatter;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mHeaderFormatter = DateFormat.getDateInstance(DateFormat.LONG);
		
		Bundle args = getArguments();
		this.mConferenceId = args.getLong("conferenceId");
		this.db = SUSEConferences.getDatabase();
		this.mEventList = db.getScheduleTitles(mConferenceId);
		List<ScheduleItem> items = new ArrayList<ScheduleItem>();

		if (mEventList.size() > 0) {
			ScheduleItem newItem = new ScheduleItem(buildHeaderText(mEventList.get(0)));
			items.add(newItem);
			Event previousEvent = null;
			for (Event event : mEventList) {
				if (previousEvent != null) {
					if (!sameDay(previousEvent.getDate(), event.getDate())) {
						ScheduleItem newHeader= new ScheduleItem(buildHeaderText(event));
						items.add(newHeader);
					}
				}
				Log.d("SUSEConferences", "#" + event.getSqlId() + " - " + event.getTitle());
				previousEvent = event;
				ScheduleItem newEvent = new ScheduleItem(event, false);
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
	
	private boolean sameDay(Date day1, Date day2) {
		Calendar cal1 = Calendar.getInstance();
		Calendar cal2 = Calendar.getInstance();
		cal1.setTime(day1);
		cal2.setTime(day2);
		boolean sameDay = cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
				cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
		return sameDay;
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
