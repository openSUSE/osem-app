package de.suse.conferenceclient.activities;

import java.util.Calendar;
import java.util.Date;
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
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.actionbarsherlock.app.SherlockActivity;

import de.suse.conferenceclient.R;
import de.suse.conferenceclient.SUSEConferences;
import de.suse.conferenceclient.app.AlarmReceiver;
import de.suse.conferenceclient.app.Database;
import de.suse.conferenceclient.models.Event;
import de.suse.conferenceclient.models.Speaker;

public class ScheduleDetailsActivity extends SherlockActivity implements OnClickListener {
	private Event mEvent;
	private Database mDb;
	private long mConferenceId;
	private ToggleButton mFavoriteButton, mCalendarButton;
	private TextView mTitleView, mTitleTime, mAbstractView, mTrackView;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.agenda_item_details);
		Bundle extras = getIntent().getExtras();
		mConferenceId = extras.getLong("conferenceId");
		mDb = SUSEConferences.getDatabase();
		mEvent = mDb.getEvent(mConferenceId, extras.getLong("eventId"));

		mFavoriteButton = (ToggleButton) findViewById(R.id.favoriteButton);
		mCalendarButton = (ToggleButton) findViewById(R.id.calendarButton);
		mTitleView = (TextView) findViewById(R.id.agendaItemName);
		mTitleTime = (TextView) findViewById(R.id.agendaItemTime);
		mAbstractView = (TextView) findViewById(R.id.abstractContents);
		mTrackView = (TextView) findViewById(R.id.trackTextView);
		mFavoriteButton.setOnClickListener(this);
		mCalendarButton.setOnClickListener(this);

		if (savedInstanceState != null) {
			mFavoriteButton.setChecked(savedInstanceState.getBoolean("favoriteChecked"));
			mCalendarButton.setChecked(savedInstanceState.getBoolean("calendarChecked"));
			mTitleView.setText(savedInstanceState.getString("title"));
			mTitleTime.setText(savedInstanceState.getString("time"));
			mAbstractView.setText(savedInstanceState.getString("abstract"));
			mTrackView.setText(savedInstanceState.getString("track"));
		} else {
			setEvent(mEvent);
		}
	}

	@Override
	public void onSaveInstanceState (Bundle outState) {
		Log.d("SUSEConferences", "onSaveInstanceState");
		outState.putLong("conferenceId", mConferenceId);
		outState.putBoolean("favoriteChecked", mFavoriteButton.isChecked());
		outState.putBoolean("calendarChecked", mCalendarButton.isChecked());
		outState.putString("title", mTitleView.getText().toString());
		outState.putString("time", mTitleTime.getText().toString());
		outState.putString("abstract", mAbstractView.getText().toString());
		outState.putString("track", mTrackView.getText().toString());
	}

	private void setEvent(Event event) {
		if (event == null) return;

		if (event.isInMySchedule())
			mFavoriteButton.setChecked(true);


		mTitleView.setText(mEvent.getTitle());
		String startTime = "";
		String endTime = "";

		java.text.DateFormat formatter = DateFormat.getTimeFormat(this);
		formatter.setTimeZone(mEvent.getTimeZone());
		startTime = formatter.format(mEvent.getDate());
		endTime = formatter.format(mEvent.getEndDate());

		String time = String.format("%s, %s - %s",
				mEvent.getRoomName(),
				startTime,
				endTime);
		mTitleTime.setText(time);
		mAbstractView.setText(Html.fromHtml(mEvent.getAbstract()));
		mTrackView.setText(mEvent.getTrackName());

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
				v.setText(Html.fromHtml(speaker.getBio()));
				newView.setPadding(0, 10, 0, 0);
				speakerLayout.addView(newView);
			}
		}
		// Check if this event is in the calendar
		if (android.os.Build.VERSION.SDK_INT>=android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			if (findEventId() >= 0)
				mCalendarButton.setChecked(true);
		} else {
			Intent intent = generateAlarmIntent();
			PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 
																	intent.getStringExtra("intentId").hashCode(), 
																	intent, 
																	PendingIntent.FLAG_NO_CREATE);
			if (pendingIntent != null)
				mCalendarButton.setChecked(true);
		}

	}

	@SuppressLint({ "NewApi", "NewApi" })
	@Override
	public void onClick(View v) {
		ToggleButton button = (ToggleButton) v;

		if (v.getId() == R.id.favoriteButton) {
			Log.d("SUSEConferences", "Toggling " + mEvent.getSqlId());
			if (button.isChecked())
				mDb.toggleEventInMySchedule(mEvent.getSqlId(), 1);
			else
				mDb.toggleEventInMySchedule(mEvent.getSqlId(), 0);
		} else {
			// Before ICS, there was no reliable way to add events
			// to the user's calendar.  So if this runs on API 14+,
			// we'll use the built in.  If not, we'll add it to
			// their Google Calendar.
			if (android.os.Build.VERSION.SDK_INT>=android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
				if (button.isChecked()) {
					Intent intent = new Intent(Intent.ACTION_INSERT);
					intent.setType("vnd.android.cursor.item/event");
					intent.putExtra(Events.TITLE, mEvent.getTitle());
					intent.putExtra(Events.EVENT_LOCATION, mEvent.getRoomName());
					intent.putExtra(Events.EVENT_TIMEZONE, mEvent.getTimeZone());
					intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, mEvent.getDate().getTime());
					intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, mEvent.getEndDate().getTime());
					intent.setData(CalendarContract.Events.CONTENT_URI);
					startActivity(intent); 
				} else {
					long id = findEventId();
					removeEvent(id);
				}
			} else {
				AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
				Intent intent = generateAlarmIntent();
				PendingIntent pendingIntent = PendingIntent.getBroadcast(this, intent.getStringExtra("intentId").hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
				
				if (button.isChecked()) {
					// Add an alarm to notify the user 5 minutes before the talk
					manager.set(AlarmManager.RTC_WAKEUP, mEvent.getDate().getTime() - 300000 , pendingIntent);
				} else {
					manager.cancel(pendingIntent);
				}
			}
		}
	}

	private Intent generateAlarmIntent() {
		Intent intent = new Intent(ScheduleDetailsActivity.this, AlarmReceiver.class);
		Calendar cal = GregorianCalendar.getInstance();
		cal.setTime(mEvent.getDate());
		// This is used to keep track of the alarm intents, the hashCode() is used as the PendingIntent id
		String id = mEvent.getTitle() + mEvent.getRoomName() + mTitleTime.getText().toString();
		intent.putExtra("intentId", id);
		intent.putExtra("title", mEvent.getTitle());
		intent.putExtra("room", mEvent.getRoomName());
		intent.putExtra("timetext", mTitleTime.getText().toString());
		intent.putExtra("milliseconds", cal.getTimeInMillis());
		return intent;
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
