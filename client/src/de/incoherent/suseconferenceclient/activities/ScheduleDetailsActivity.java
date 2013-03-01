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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;
import android.provider.CalendarContract.Instances;
import android.text.Html;
import android.text.format.DateFormat;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import de.incoherent.suseconferenceclient.SUSEConferences;
import de.incoherent.suseconferenceclient.app.AlarmReceiver;
import de.incoherent.suseconferenceclient.app.Database;
import de.incoherent.suseconferenceclient.models.Event;
import de.incoherent.suseconferenceclient.models.Speaker;
import de.incoherent.suseconferenceclient.R;

public class ScheduleDetailsActivity extends SherlockActivity  {
	private Event mEvent;
	private Database mDb;
	private long mConferenceId;
	private TextView mTitleView, mTitleTime, mAbstractView, mTrackView;
	private boolean mFavoriteCheck = false, mCalendarCheck = false;
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.agenda_item_details);
		
		Bundle extras = getIntent().getExtras();
		mConferenceId = extras.getLong("conferenceId");
		mDb = SUSEConferences.getDatabase();
		mEvent = mDb.getEvent(mConferenceId, extras.getLong("eventId"));

		mTitleView = (TextView) findViewById(R.id.agendaItemName);
		mTitleTime = (TextView) findViewById(R.id.agendaItemTime);
		mAbstractView = (TextView) findViewById(R.id.abstractContents);
		mTrackView = (TextView) findViewById(R.id.trackTextView);

		if (savedInstanceState != null) {
			mFavoriteCheck = savedInstanceState.getBoolean("favoriteChecked");
			mCalendarCheck = savedInstanceState.getBoolean("calendarChecked");
			mTitleView.setText(savedInstanceState.getString("title"));
			mTitleTime.setText(savedInstanceState.getString("time"));
			mAbstractView.setText(savedInstanceState.getString("abstract"));
			mTrackView.setText(savedInstanceState.getString("track"));
		} else {
			setEvent(mEvent);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		// Since we launch a Calendar intent when the user clicks the calendar button,
		// check if they cancelled the calendar addition
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			if (findEventId() >= 0)
				mCalendarCheck = true;
			else
				mCalendarCheck = false;
		}
	}

	@Override
	public void onSaveInstanceState (Bundle outState) {
		Log.d("SUSEConferences", "onSaveInstanceState");
		outState.putLong("conferenceId", mConferenceId);
		outState.putBoolean("favoriteChecked", mFavoriteCheck);
		outState.putBoolean("calendarChecked", mCalendarCheck);
		outState.putString("title", mTitleView.getText().toString());
		outState.putString("time", mTitleTime.getText().toString());
		outState.putString("abstract", mAbstractView.getText().toString());
		outState.putString("track", mTrackView.getText().toString());
	}

	private void setEvent(Event event) {
		if (event == null) return;

		if (event.isInMySchedule())
			mFavoriteCheck = true;

		mTitleView.setText(mEvent.getTitle());
		String startTime = "";
		String endTime = "";

		java.text.DateFormat formatter = DateFormat.getTimeFormat(this);
		SimpleDateFormat sdf = new SimpleDateFormat("EEE");
		String dayOfTheWeek = sdf.format(mEvent.getDate());
		
		formatter.setTimeZone(mEvent.getTimeZone());
		startTime = formatter.format(mEvent.getDate());
		endTime = formatter.format(mEvent.getEndDate());

		String time = String.format("Room: %s, %s %s - %s",
				mEvent.getRoomName(),
				dayOfTheWeek,
				startTime,
				endTime);
		mTitleTime.setText(time);
		mAbstractView.setText(Html.fromHtml(mEvent.getAbstract()));
		mTrackView.setText("Track: " + mEvent.getTrackName());

		List<Speaker> speakerList = mEvent.getSpeakers();
		LinearLayout speakerLayout = (LinearLayout) findViewById(R.id.speakersLayout);
		speakerLayout.removeAllViews();

		if (speakerList.size() == 0) {
			TextView v = (TextView) findViewById(R.id.speakerLayoutTextView);
			v.setVisibility(View.GONE);
		} else {
			for (Speaker speaker : speakerList) {
				View newView = View.inflate(this, R.layout.speaker_view, null);
				TextView v = (TextView) newView.findViewById(R.id.nameTextView);
				v.setText(speaker.getName());
				
				v = (TextView) newView.findViewById(R.id.companyTextView);
				v.setText(speaker.getCompany());
				
				v = (TextView) newView.findViewById(R.id.biographyView);
				String bio = speaker.getBio();
				if (bio.length() == 0) {
					v.setVisibility(View.GONE);
				} else {
					v.setText(Html.fromHtml(bio));
					v.setMovementMethod(LinkMovementMethod.getInstance());
				}
				
				newView.setPadding(0, 10, 0, 0);
				speakerLayout.addView(newView);
			}
		}
		// Check if this event is in the calendar
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			if (findEventId() >= 0)
				mCalendarCheck = true;
		} else {
			Intent intent = generateAlarmIntent();
			PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 
																	intent.getStringExtra("intentId").hashCode(), 
																	intent, 
																	PendingIntent.FLAG_NO_CREATE);
			Log.d("SUSEConferences", "Looking for " + intent.getStringExtra("intentId").hashCode());

			if (pendingIntent != null) {
				Log.d("SUSEConferences", "It is not null");
				mCalendarCheck = true;
			} else {
				mCalendarCheck=false;
			}
		}
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuItem item = menu.add(Menu.NONE, R.id.actionBarFavorite, Menu.NONE, "");
    	item.setCheckable(true);
    	item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    	if (mFavoriteCheck) {
    		item.setIcon(R.drawable.favorite_on);
    		item.setChecked(true);
    	} else {
    		item.setIcon(R.drawable.favorite_off);
    	}
    	
    	item = menu.add(Menu.NONE, R.id.actionBarCalendar, Menu.NONE, "");
    	item.setCheckable(true);
    	item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    	Log.d("SUSEConferences", "Calendar is " + mCalendarCheck);
    	if (mCalendarCheck) {
    		item.setIcon(R.drawable.event_on);
    		item.setChecked(true);
    	} else {
    		item.setIcon(R.drawable.event_off);
    	}
    	return true;
    }
    
    @SuppressLint("NewApi")
	@Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
    	int itemId = menuItem.getItemId();
		if (itemId == R.id.actionBarFavorite) {
			if (menuItem.isChecked()) {
    			menuItem.setChecked(false);
    			menuItem.setIcon(R.drawable.favorite_off);
    			mFavoriteCheck = false;
				mDb.toggleEventInMySchedule(mEvent.getSqlId(), 0);
				Toast.makeText(this, "Removed from My Schedule", Toast.LENGTH_SHORT).show();
    		} else {
    			menuItem.setChecked(true);
    			menuItem.setIcon(R.drawable.favorite_on);
    			mFavoriteCheck = true;
				mDb.toggleEventInMySchedule(mEvent.getSqlId(), 1);
				Toast.makeText(this, "Added to My Schedule", Toast.LENGTH_SHORT).show();
    		}
			return true;
		} else if (itemId == R.id.actionBarCalendar) {
			if (menuItem.isChecked()) {
    			Log.d("SUSEConferences", "Toggle Off");
    			menuItem.setChecked(false);
    			menuItem.setIcon(R.drawable.event_off);
    			mCalendarCheck = false;
    		} else {
    			Log.d("SUSEConferences", "Toggle On");
    			menuItem.setChecked(true);
    			menuItem.setIcon(R.drawable.event_on);
    			mCalendarCheck = true;
    		}
			if (android.os.Build.VERSION.SDK_INT >=android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
				if (mCalendarCheck) {
					Intent intent = new Intent(Intent.ACTION_INSERT);
					intent.setType("vnd.android.cursor.item/event");
					intent.putExtra(Events.TITLE, mEvent.getTitle());
					intent.putExtra(Events.EVENT_LOCATION, mEvent.getRoomName());
					intent.putExtra(CalendarContract.Events.EVENT_TIMEZONE, mEvent.getTimeZone().getDisplayName());
					intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, mEvent.getDate().getTime());
					intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, mEvent.getEndDate().getTime());
					intent.setData(CalendarContract.Events.CONTENT_URI);
					startActivity(intent); 
				} else {
					long id = findEventId();
					if (id >= 0)
						removeEvent(id);
				}
			} else {
				AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
				Intent intent = generateAlarmIntent();
				
				if (mCalendarCheck) {
					// Add an alarm to notify the user 5 minutes before the talk
					PendingIntent pendingIntent = PendingIntent.getBroadcast(this, intent.getStringExtra("intentId").hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
					manager.set(AlarmManager.RTC_WAKEUP, mEvent.getDate().getTime() - 300000 , pendingIntent);
					mDb.toggleEventAlert(mEvent.getSqlId(), 1);
					Toast.makeText(this, "Alert set", Toast.LENGTH_SHORT).show();
				} else {
					PendingIntent pendingIntent = PendingIntent.getBroadcast(this, intent.getStringExtra("intentId").hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
					manager.cancel(pendingIntent);
					pendingIntent.cancel();
					mDb.toggleEventAlert(mEvent.getSqlId(), 0);
					Toast.makeText(this, "Alert canceled", Toast.LENGTH_SHORT).show();
				}
			}
			return true;
		}
    	return super.onOptionsItemSelected(menuItem);
    }
    
	private Intent generateAlarmIntent() {
		Intent intent = new Intent(ScheduleDetailsActivity.this, AlarmReceiver.class);
		intent.putExtras(generateAlarmIntentBundle(this, mEvent));
		return intent;
	}
	
	public static Bundle generateAlarmIntentBundle(Context context, Event event) {
		Bundle b = new Bundle();
		String startTime = "";
		String endTime = "";

		java.text.DateFormat formatter = DateFormat.getTimeFormat(context);
		formatter.setTimeZone(event.getTimeZone());
		startTime = formatter.format(event.getDate());
		endTime = formatter.format(event.getEndDate());

		String time = String.format("%s, %s - %s",
				event.getRoomName(),
				startTime,
				endTime);

		Calendar cal = GregorianCalendar.getInstance();
		cal.setTime(event.getDate());
		
		// This is used to keep track of the alarm intents, the hashCode() is used as the PendingIntent id
		String id = event.getTitle() + event.getRoomName() + event.getGuid() + time;
		b.putString("intentId", id);
		b.putString("title", event.getTitle());
		b.putString("room", event.getRoomName());
		b.putString("timetext", time);
		b.putLong("milliseconds", cal.getTimeInMillis());
		return b;
	}
	
	@TargetApi(14)
	private long findEventId() {
		long id = -1;
		ContentResolver cr = getContentResolver();
		String[] fields = { Instances.EVENT_ID, Instances.TITLE };
		Cursor c = CalendarContract.Instances.query(cr, fields, mEvent.getDate().getTime(), mEvent.getEndDate().getTime());
		while (c.moveToNext()) {
			String title = c.getString(1);
			if (title.equals(mEvent.getTitle())) {
				id = c.getLong(0);
				break;
			}
		}
		c.close();
		return id;
	}

	@TargetApi(14)
	private void removeEvent(long id) {
		ContentResolver cr = getContentResolver();
		Uri uri = ContentUris.withAppendedId(Events.CONTENT_URI, id);
		int rows = cr.delete(uri, null, null);
	}
}
