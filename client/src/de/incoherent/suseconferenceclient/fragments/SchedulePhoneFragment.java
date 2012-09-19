package de.incoherent.suseconferenceclient.fragments;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockListFragment;

import de.incoherent.suseconferenceclient.SUSEConferences;
import de.incoherent.suseconferenceclient.activities.ScheduleDetailsActivity;
import de.incoherent.suseconferenceclient.adapters.PhoneScheduleAdapter;
import de.incoherent.suseconferenceclient.adapters.PhoneScheduleAdapter.ScheduleItem;
import de.incoherent.suseconferenceclient.app.Database;
import de.incoherent.suseconferenceclient.models.Event;
import de.incoherent.suseconferenceclient.R;

public class SchedulePhoneFragment extends SherlockListFragment {
	public interface OnGetEventsListener {
		public List<Event> getEvents();
	}
	
	public SchedulePhoneFragment() { }

	private Database db;
    private long mConferenceId;
    private List<Event> mEventList;
    private DateFormat mHeaderFormatter;
    private int mScrollIndex = -1;
    private int mIndex = -1;
    private int mTop = 0;
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mHeaderFormatter = DateFormat.getDateInstance(DateFormat.LONG);
		
		Bundle args = getArguments();
		this.mConferenceId = args.getLong("conferenceId");
		this.db = SUSEConferences.getDatabase();		
	}

	public List<ScheduleItem> getScheduleItems() {
		this.mEventList = db.getScheduleTitles(mConferenceId);

		List<ScheduleItem> items = new ArrayList<ScheduleItem>();
		Collections.sort(mEventList);
		if (mEventList.size() > 0) {
			Date now = new Date();
			ScheduleItem newItem = new ScheduleItem(buildHeaderText(mEventList.get(0)));
			items.add(newItem);
			Event previousEvent = null;
			for (Event event : mEventList) {
				if (previousEvent != null) {
					if (!sameDay(previousEvent.getDate(), event.getDate(), previousEvent.getTimeZone(), event.getTimeZone())) {
						ScheduleItem newHeader= new ScheduleItem(buildHeaderText(event));
						items.add(newHeader);
					}
				}
				
				previousEvent = event;
				ScheduleItem newEvent = new ScheduleItem(event, false);
				items.add(newEvent);
				if (mScrollIndex == -1 && (event.getDate().after(now) || eventWithinRange(now, event))) {
					Log.d("SUSEConferences", "Nearest date match: " + event.getTitle());
					mScrollIndex = items.size() - 1;
				}
			}
		}

		return items;
	}
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getListView().setDrawSelectorOnTop(true);
		// See if we are in the middle of the conference, and scroll to the upcoming
		// talk.  Set it to > 1 so we show from the top in the case where the user is viewing
		// the schedule before the conference starts
		if (mScrollIndex > 1)
			getListView().setSelection(mScrollIndex);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		PhoneScheduleAdapter adapter = new PhoneScheduleAdapter(getActivity(),
				true,
				R.layout.schedule_list_item,
				getResources().getColor(R.color.dark_suse_green),
				getResources().getColor(R.color.suse_grey),
				getScheduleItems());
		setListAdapter(adapter);
		if(mIndex!=-1){
			this.getListView().setSelectionFromTop(mIndex, mTop);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		try{
			mIndex = this.getListView().getFirstVisiblePosition();
			View v = this.getListView().getChildAt(0);
			mTop = (v == null) ? 0 : v.getTop();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private String buildHeaderText(Event event) {
		return mHeaderFormatter.format(event.getDate());
	}
	
	private boolean eventWithinRange(Date now, Event event) {
		Date start = event.getDate();
		Date end = event.getEndDate();
		return !(now.before(start) || now.after(end));
	}
	private boolean sameDay(Date day1, Date day2, TimeZone tz1, TimeZone tz2) {
		Calendar cal1 = Calendar.getInstance();
		cal1.setTimeZone(tz1);
		Calendar cal2 = Calendar.getInstance();
		cal2.setTimeZone(tz2);
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
		if (item.isHeader() || item.getEvent().isMetaInformation())
			return;
		
		Intent intent = new Intent(getActivity(), ScheduleDetailsActivity.class);
		intent.putExtra("eventId", item.getEvent().getSqlId());
		intent.putExtra("conferenceId", mConferenceId);
		startActivity(intent);
	}
}
