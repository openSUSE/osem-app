/**
 * 
 */
package de.suse.conferenceclient.activities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;

import de.suse.conferenceclient.R;
import de.suse.conferenceclient.SUSEConferences;
import de.suse.conferenceclient.app.Database;
import de.suse.conferenceclient.models.Event;
import de.suse.conferenceclient.views.ScheduleView;
import de.suse.conferenceclient.views.ScheduleView.OnEventClickListener;

/**
 * @author Matt Barringer <mbarringer@suse.de>
 *
 */
@SuppressLint("NewApi")
public class ScheduleActivity extends SherlockActivity implements OnEventClickListener {
	public static int FULL_SCHEDULE = 0;
	public static int MY_SCHEDULE = 1;
	private Database db;
	private long mConferenceId;
	private int mType;

	private HashMap<String, List<Event>> dailyEvents;
	private List<TextView> dailyTextViews;
	private TextView mActiveDay;
	private int mDarkText, mLightText;
	private ScheduleView mScheduleView;
	private TextView mAgendaTitle;
	private boolean mMySchedule = false;
	private List<Event> mEventList;

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putLong("conferenceId", this.mConferenceId);
		outState.putInt("type", this.mType);
	}

	@SuppressLint("NewApi")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_agenda);
		mScheduleView = (ScheduleView) findViewById(R.id.scheduleView);
		mScheduleView.setOnEventClickListener(this);
		mAgendaTitle = (TextView) findViewById(R.id.agendaTextView);
		dailyEvents = new HashMap<String, List<Event>>();
		dailyTextViews = new ArrayList<TextView>();
		mDarkText = getResources().getColor(R.color.dark_suse_green);
		mLightText = getResources().getColor(R.color.light_suse_green);

		ActionBar bar = getSupportActionBar();
		this.db = SUSEConferences.getDatabase();

		if (savedInstanceState != null) {
			this.mConferenceId = savedInstanceState.getLong("conferenceId", -1);
			this.mType = savedInstanceState.getInt("type", FULL_SCHEDULE);
		} else {
			this.mConferenceId = getIntent().getLongExtra("conferenceId", -1);
			this.mType = getIntent().getIntExtra("type", FULL_SCHEDULE);
		}
		mEventList = db.getScheduleTitles(mConferenceId);
		if (this.mType == FULL_SCHEDULE) {
			bar.setTitle(getString(R.string.fullSchedule));
		} else {
			bar.setTitle(getString(R.string.mySchedule));
			mMySchedule = true;
		}

		setEventList(mEventList);
	}

	public void toggleFavorite(boolean checked, Event event) {
		if (mMySchedule) {
			String text = mActiveDay.getText().toString();
			List<Event> eventList = dailyEvents.get(text);

			if (checked) {
				eventList.add(event);
			} else {
				eventList.remove(event);
				mEventList.remove(event);
			}
			// TODO orientation
			mScheduleView.setEvents(eventList, (mType == FULL_SCHEDULE));
		}
	}

	public void setEventList(List<Event> eventList) {
		if (eventList != null)
			mEventList = eventList;

		Log.d("SUSEConferences", "setEventList");
		GregorianCalendar cal = new GregorianCalendar();
		LinearLayout daysLayout = (LinearLayout) findViewById(R.id.datesLayout);
		this.db = SUSEConferences.getDatabase();

		for (Event event : mEventList) {
			cal.setTime(event.getDate());
			cal.setTimeZone(event.getTimeZone());
			String month = cal.getDisplayName(GregorianCalendar.MONTH, GregorianCalendar.SHORT, Locale.getDefault());
			String day = String.valueOf(cal.get(GregorianCalendar.DAY_OF_MONTH));
			String dayMonth = month + " " + day;

			if (!dailyEvents.containsKey(dayMonth)) {
				List<Event> newList = new ArrayList<Event>();
				newList.add(event);
				dailyEvents.put(dayMonth, newList);
			} else {
				List<Event> list = dailyEvents.get(dayMonth);
				list.add(event);
			}
		}

		// Now sort the date keys so they look appropriate
		Vector<String> dateVector = new Vector<String>(dailyEvents.keySet()); 
		Collections.sort(dateVector);
		for (String day : dateVector) {
			TextView newDay = new TextView(this);
			newDay.setText(day);
			newDay.setTextSize(25);
			newDay.setClickable(true);
			newDay.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					setDay((TextView) v);
				}
			});

			newDay.setPadding(0, 0, 10, 0);
			daysLayout.addView(newDay);
			dailyTextViews.add(newDay);
		}

		if (dailyTextViews.size() > 0)
			setDay(dailyTextViews.get(0));
	}

	private void setDay(TextView day) {
		mActiveDay = day;
		day.setTextColor(mDarkText);
		for (TextView view : dailyTextViews) {
			if (view != day) {
				view.setTextColor(mLightText);
			}
		}

		String text = day.getText().toString();
		if (mMySchedule)
			mAgendaTitle.setText(getResources().getString(R.string.myAgendaFor) + " " + text);
		else
			mAgendaTitle.setText(getResources().getString(R.string.agendaFor) + " " + text);

		// TODO orientation for the calendar
		mScheduleView.setEvents(dailyEvents.get(text), (mType == FULL_SCHEDULE));
	}

	@Override
	public void clicked(Event event) {
		Intent intent = new Intent(this, ScheduleDetailsActivity.class);
		intent.putExtra("eventId", event.getSqlId());
		intent.putExtra("conferenceId", mConferenceId);
		startActivity(intent);
	}
}
