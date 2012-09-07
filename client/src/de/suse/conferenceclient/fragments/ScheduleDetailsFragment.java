/**
 * 
 */
package de.suse.conferenceclient.fragments;

import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;
import android.provider.CalendarContract.Instances;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.actionbarsherlock.app.SherlockFragment;

import de.suse.conferenceclient.R;
import de.suse.conferenceclient.SUSEConferences;
import de.suse.conferenceclient.app.Database;
import de.suse.conferenceclient.fragments.ScheduleFragment.OnEventListener;
import de.suse.conferenceclient.fragments.ScheduleFragment.OnGetEventsListener;
import de.suse.conferenceclient.models.Event;
import de.suse.conferenceclient.models.Speaker;

/**
 * @author Matt Barringer <mbarringer@suse.de>
 *
 */
public class ScheduleDetailsFragment extends SherlockFragment implements OnClickListener {
	public interface OnDetailsListener {
		public void onFavoriteToggle(boolean checked, Event event);
		public Event getCurrentEvent();
	}

	private Event mEvent;
	private Database mDb;
	private long mConferenceId;
	private OnDetailsListener mListener;
	private ToggleButton mFavoriteButton, mCalendarButton;
	private TextView mTitleView, mTitleTime, mAbstractView, mTrackView;
	private boolean mStateRetained = false;
	
	public ScheduleDetailsFragment() {
	}
	
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.d("SUSEConferences", "details onAttach");
        try {
        	mListener = (OnDetailsListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnDetailsListener");
        }
    }

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
    	Log.d("SUSEConferences", "Details onCreateView()");
        View view = inflater.inflate(R.layout.agenda_item_details, container, false);
        mFavoriteButton = (ToggleButton) view.findViewById(R.id.favoriteButton);
        mCalendarButton = (ToggleButton) view.findViewById(R.id.calendarButton);
		mTitleView = (TextView) view.findViewById(R.id.agendaItemName);
		mTitleTime = (TextView) view.findViewById(R.id.agendaItemTime);
		mAbstractView = (TextView) view.findViewById(R.id.abstractContents);
		mTrackView = (TextView) view.findViewById(R.id.trackTextView);
		mFavoriteButton.setOnClickListener(this);
		mCalendarButton.setOnClickListener(this);
        return view;
    }
    
    @Override
    public void onActivityCreated (Bundle savedInstanceState) {
    	super.onActivityCreated(savedInstanceState);
		if (savedInstanceState != null) {
			Log.d("SUSEConferences", "Reading from savedInstanceState");
			mStateRetained = true;
			mFavoriteButton.setChecked(savedInstanceState.getBoolean("favoriteChecked"));
			mCalendarButton.setChecked(savedInstanceState.getBoolean("calendarChecked"));
			mTitleView.setText(savedInstanceState.getString("title"));
			mTitleTime.setText(savedInstanceState.getString("time"));
			mAbstractView.setText(savedInstanceState.getString("abstract"));
			mTrackView.setText(savedInstanceState.getString("track"));
		} else {
			if (mListener != null) {
				Event e = mListener.getCurrentEvent();
				setEvent(e);
			}
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
    
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
	    super.setUserVisibleHint(isVisibleToUser);
	
	    if (isVisibleToUser) { 
	    	if (!mStateRetained)
	    		setEvent(mListener.getCurrentEvent());
	    }
    }
    
    public void setEvent(Event event) {
    	if (event == null) return;
    	
       	this.mEvent = event;
    	this.mConferenceId = event.getConferenceId();
        mDb = SUSEConferences.getDatabase();
    	View view = getView();

    	Log.d("SUSEConferences", "Loading details for " + mEvent.getSqlId());
		if (event.isInMySchedule())
			mFavoriteButton.setChecked(true);
		
		// Check if this event is in the calendar
		if (android.os.Build.VERSION.SDK_INT>=android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			if (findEventId() >= 0)
				mCalendarButton.setChecked(true);
		}
		
		mTitleView.setText(mEvent.getTitle());
		String startTime = "";
		String endTime = "";
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(mEvent.getDate());
		int hour = cal.get(GregorianCalendar.HOUR_OF_DAY);
		if (hour < 10)
			startTime = "0";
		startTime = startTime + hour + ":00";
		cal.setTime(mEvent.getEndDate());
		hour = cal.get(GregorianCalendar.HOUR_OF_DAY);
		if (hour < 10)
			endTime = "0";
		endTime = endTime + hour + ":00";
		
		String time = String.format("%s, %s - %s",
									mEvent.getRoomName(),
									startTime,
									endTime);
		mTitleTime.setText(time);
		mAbstractView.setText(Html.fromHtml(mEvent.getAbstract()));
		mTrackView.setText(mEvent.getTrackName());
		
		List<Speaker> speakerList = mEvent.getSpeakers();
		LinearLayout speakerLayout = (LinearLayout) view.findViewById(R.id.speakersLayout);
		speakerLayout.removeAllViews();
		
		for (Speaker speaker : speakerList) {
			View newView = View.inflate(getActivity(), R.layout.speaker_view, null);
			TextView v = (TextView) newView.findViewById(R.id.nameTextView);
			v.setText(speaker.getName());
			v = (TextView) newView.findViewById(R.id.companyTextView);
			v.setText(speaker.getCompany());
			v = (TextView) newView.findViewById(R.id.biographyView);
			v.setText(speaker.getBio());
			newView.setPadding(0, 10, 0, 0);
			speakerLayout.addView(newView);
		}
    }

	@Override
	public void onClick(View v) {
		ToggleButton button = (ToggleButton) v;
		
		if (v.getId() == R.id.favoriteButton) {
			Log.d("SUSEConferences", "Toggling " + mEvent.getSqlId());
			if (button.isChecked())
				mDb.toggleEventInMySchedule(mEvent.getSqlId(), 1);
			else
				mDb.toggleEventInMySchedule(mEvent.getSqlId(), 0);
			mListener.onFavoriteToggle(button.isChecked(), mEvent);
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
					// TODO The conference should specify the timezone
					TimeZone timeZone = TimeZone.getDefault();
					intent.putExtra(Events.EVENT_TIMEZONE, timeZone.getID());
					intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, mEvent.getDate().getTime());
					intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, mEvent.getEndDate().getTime());
					intent.setData(CalendarContract.Events.CONTENT_URI);
					startActivity(intent); 
				} else {
					long id = findEventId();
					removeEvent(id);
				}
			}
		}
	}
	
	private long findEventId() {
		long id = -1;
		ContentResolver cr = getActivity().getContentResolver();
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
	
	private void removeEvent(long id) {
		ContentResolver cr = getActivity().getContentResolver();
		Uri uri = ContentUris.withAppendedId(Events.CONTENT_URI, id);
		int rows = cr.delete(uri, null, null);
	}
}
