/*******************************************************************************
 * Copyright (c) 2012 Matt Barringer <matt@incoherent.de>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Matt Barringer <matt@incoherent.de> - initial API and implementation
 ******************************************************************************/
package de.incoherent.suseconferenceclient.fragments;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockListFragment;

import de.incoherent.suseconferenceclient.SUSEConferences;
import de.incoherent.suseconferenceclient.activities.ScheduleDetailsActivity;
import de.incoherent.suseconferenceclient.adapters.ScheduleAdapter;
import de.incoherent.suseconferenceclient.adapters.ScheduleAdapter.ScheduleItem;
import de.incoherent.suseconferenceclient.app.Database;
import de.incoherent.suseconferenceclient.models.Event;
import de.incoherent.suseconferenceclient.R;

public class ScheduleFragment extends SherlockListFragment {
	public interface OnGetEventsListener {
		public List<Event> getEvents();
	}
	
	public ScheduleFragment() { }

	private Database db;
    private long mConferenceId;
    private String mConferenceName;
    private List<Event> mEventList;
    private DateFormat mHeaderFormatter;
    private ScheduleAdapter mAdapter;
    private int mScrollIndex = -1;
    private int mIndex = -1;
    private int mTop = 0;
    
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mHeaderFormatter = DateFormat.getDateInstance(DateFormat.LONG);
		Bundle args = getArguments();
		this.mConferenceId = args.getLong("conferenceId");
		this.db = SUSEConferences.getDatabase();
		this.mConferenceName = args.getString("conferenceName");
	}

	public List<ScheduleItem> getScheduleItems() {
        final String languageSetting = mConferenceName + "_language_filter";
        final String trackSetting = mConferenceName + "_track_filter";
		final SharedPreferences settings = getActivity().getSharedPreferences("SUSEConferences", 0);
		String trackFilter = settings.getString(trackSetting, null);
		String languageFilter = settings.getString(languageSetting, null);

		this.mEventList = db.getScheduleTitles(mConferenceId, trackFilter, languageFilter);

		List<ScheduleItem> items = new ArrayList<ScheduleItem>();
		Collections.sort(mEventList);
		if (mEventList.size() > 0) {
			Date now = new Date();
			ScheduleItem newItem = new ScheduleItem(buildHeaderText(mEventList.get(0)), getDayString(mEventList.get(0)));
			items.add(newItem);
			Event previousEvent = null;
			for (Event event : mEventList) {
				if (previousEvent != null) {
					if (!sameDay(previousEvent.getDate(), event.getDate(), previousEvent.getTimeZone(), event.getTimeZone())) {
						ScheduleItem newHeader= new ScheduleItem(buildHeaderText(event), getDayString(event));
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
		getListView().setFastScrollEnabled(true);

		// See if we are in the middle of the conference, and scroll to the upcoming
		// talk.  Set it to > 1 so we show from the top in the case where the user is viewing
		// the schedule before the conference starts
		if (mScrollIndex > 1)
			getListView().setSelection(mScrollIndex);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		mAdapter = new ScheduleAdapter(getActivity(),
				true,
				R.layout.schedule_list_item,
				getResources().getColor(R.color.dark_suse_green),
				getResources().getColor(R.color.suse_grey),
				getScheduleItems());
		setListAdapter(mAdapter);
		if(mIndex!=-1){
			this.getListView().setSelectionFromTop(mIndex, mTop);
		}
	}

	public void loadNewConference(long conferenceId, String conferenceName) {
		Log.d("SUSEConferences", "Loading new conference " + conferenceId + " " + conferenceName);
		this.mConferenceId = conferenceId;
		this.mConferenceName = conferenceName;
		requery();
	}
	
	public void requery() {
		mIndex = -1;
		mTop = 0;
		mAdapter = new ScheduleAdapter(getActivity(),
				true,
				R.layout.schedule_list_item,
				getResources().getColor(R.color.dark_suse_green),
				getResources().getColor(R.color.suse_grey),
				getScheduleItems());
		setListAdapter(mAdapter);
		if (mScrollIndex > 1)
			getListView().setSelection(mScrollIndex);
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
	
	private String getDayString(Event e) {
		Calendar cal = new GregorianCalendar();
		cal.setTime(e.getDate());
		return String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
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
