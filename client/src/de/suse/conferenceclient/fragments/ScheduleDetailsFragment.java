/**
 * 
 */
package de.suse.conferenceclient.fragments;

import java.util.GregorianCalendar;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
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
		}
    }
    @Override
    public void onSaveInstanceState (Bundle outState) {
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

		if (mDb.isEventInMySchedule(mEvent.getSqlId()))
			mFavoriteButton.setChecked(true);
		
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
			mListener.onFavoriteToggle(button.isChecked(), mEvent);
			if (button.isChecked())
				mDb.addEventToMySchedule(mEvent.getSqlId(), mConferenceId);
			else
				mDb.removeEventFromMySchedule(mEvent.getSqlId());
		} else {
			Log.d("SUSEConferences", "Calendar clicked");
		}
	}
}
