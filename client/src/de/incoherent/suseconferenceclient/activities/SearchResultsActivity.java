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

package de.incoherent.suseconferenceclient.activities;

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

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockListActivity;

import de.incoherent.suseconferenceclient.R;
import de.incoherent.suseconferenceclient.SUSEConferences;
import de.incoherent.suseconferenceclient.adapters.ScheduleAdapter;
import de.incoherent.suseconferenceclient.adapters.ScheduleAdapter.ScheduleItem;
import de.incoherent.suseconferenceclient.app.Database;
import de.incoherent.suseconferenceclient.models.Event;

/**
 * TODO See if this could be merged with ScheduleFragment
 *
 */
public class SearchResultsActivity extends SherlockListActivity {
    private DateFormat mHeaderFormatter;
    private long mConferenceId = -1;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		mHeaderFormatter = DateFormat.getDateInstance(DateFormat.LONG);

		Database db = SUSEConferences.getDatabase();
		getListView().setFastScrollEnabled(true);
		getListView().setDrawSelectorOnTop(true);

		Bundle extras = getIntent().getExtras();
        ActionBar bar = getSupportActionBar();
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        bar.setTitle("Results for \"" + extras.getString("query") + "\"");
        mConferenceId = extras.getLong("conferenceId");
        List<Event> results = db.getEventsFromIdList(mConferenceId, extras.getStringArrayList("results"));
		List<ScheduleItem> items = new ArrayList<ScheduleItem>();
		Collections.sort(results);
		
		if (results.size() > 0) {
			ScheduleItem newItem = new ScheduleItem(buildHeaderText(results.get(0)), getDayString(results.get(0)));
			items.add(newItem);
			Event previousEvent = null;
			for (Event event : results) {
				if (previousEvent != null) {
					if (!sameDay(previousEvent.getDate(), event.getDate(), previousEvent.getTimeZone(), event.getTimeZone())) {
						ScheduleItem newHeader= new ScheduleItem(buildHeaderText(event), getDayString(event));
						items.add(newHeader);
					}
				}
				
				previousEvent = event;
				ScheduleItem newEvent = new ScheduleItem(event, false);
				items.add(newEvent);
			}
		}
		
		ScheduleAdapter adapter = new ScheduleAdapter(this,
				true,
				R.layout.schedule_list_item,
				getResources().getColor(R.color.dark_suse_green),
				getResources().getColor(R.color.suse_grey),
				items);
		setListAdapter(adapter);
    }
    
	private String buildHeaderText(Event event) {
		return mHeaderFormatter.format(event.getDate());
	}
	
	private String getDayString(Event e) {
		Calendar cal = new GregorianCalendar();
		cal.setTime(e.getDate());
		return String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
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
		
		Intent intent = new Intent(SearchResultsActivity.this, ScheduleDetailsActivity.class);
		intent.putExtra("eventId", item.getEvent().getSqlId());
		intent.putExtra("conferenceId", mConferenceId);
		startActivity(intent);
	}


}
