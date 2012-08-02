/**
 * 
 */
package de.suse.conferenceclient.fragments;

import java.util.GregorianCalendar;
import java.util.List;

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
import de.suse.conferenceclient.models.Event;
import de.suse.conferenceclient.models.Speaker;

/**
 * @author Matt Barringer <mbarringer@suse.de>
 *
 */
public class ScheduleDetailsFragment extends SherlockFragment implements OnClickListener {
	public interface OnDetailsListener {
		public void onFavoriteToggle(boolean checked, Event event);
	}

	private Event mEvent;
	private Database mDb;
	private long mConferenceId;
	private OnDetailsListener mListener;
	
	public ScheduleDetailsFragment(OnDetailsListener listener) {
		this.mListener = listener;
	}
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.agenda_item_details, container, false);
        return view;
    }
    
    public void setEvent(Event event, long conferenceId) {
    	this.mEvent = event;
    	this.mConferenceId = conferenceId;
        mDb = SUSEConferences.getDatabase();
    	View view = getView();

		ToggleButton favoriteButton = (ToggleButton) view.findViewById(R.id.favoriteButton);
		ToggleButton calendarButton = (ToggleButton) view.findViewById(R.id.calendarButton);
		favoriteButton.setOnClickListener(this);
		calendarButton.setOnClickListener(this);
		if (mDb.isEventInMySchedule(mEvent.getSqlId()))
			favoriteButton.setChecked(true);
		
		TextView titleView = (TextView) view.findViewById(R.id.agendaItemName);
		TextView titleTime = (TextView) view.findViewById(R.id.agendaItemTime);
		TextView abstractView = (TextView) view.findViewById(R.id.abstractContents);
		TextView trackView = (TextView) view.findViewById(R.id.trackTextView);
		titleView.setText(mEvent.getTitle());
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
		titleTime.setText(time);
		abstractView.setText(Html.fromHtml(mEvent.getAbstract()));
		trackView.setText(mEvent.getTrackName());
		
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

	/* (non-Javadoc)
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
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
