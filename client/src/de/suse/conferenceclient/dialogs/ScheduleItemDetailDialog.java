/**
 * 
 */
package de.suse.conferenceclient.dialogs;

import java.util.GregorianCalendar;
import java.util.List;

import de.suse.conferenceclient.R;
import de.suse.conferenceclient.SUSEConferences;
import de.suse.conferenceclient.app.Database;
import de.suse.conferenceclient.models.Event;
import de.suse.conferenceclient.models.Speaker;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

/**
 * @author Matt Barringer <mbarringer@suse.de>
 *
 */
public class ScheduleItemDetailDialog extends Dialog implements View.OnClickListener {
	public interface OnFavoriteListener {
		public void onFavoriteToggle(boolean checked, Event event);
	}
	
	private Event mEvent;
	private Context mContext;
	private Database mDb;
	private long mConferenceId;
	private OnFavoriteListener mListener;
	
	public ScheduleItemDetailDialog(Context context, Event event, long conferenceId, OnFavoriteListener listener) {
		super(context, R.style.PlainDialog);
		setCancelable(true);
		this.mEvent = event;
		this.mContext = context;
		this.mConferenceId = conferenceId;
		this.mListener = listener;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        mDb = SUSEConferences.getDatabase();
        
		setContentView(R.layout.agenda_item_details);
		ToggleButton favoriteButton = (ToggleButton) findViewById(R.id.favoriteButton);
		ToggleButton calendarButton = (ToggleButton) findViewById(R.id.calendarButton);
		favoriteButton.setOnClickListener(this);
		calendarButton.setOnClickListener(this);
		if (mEvent.isInMySchedule())
			favoriteButton.setChecked(true);
		
		TextView titleView = (TextView) findViewById(R.id.agendaItemName);
		TextView titleTime = (TextView) findViewById(R.id.agendaItemTime);
		TextView abstractView = (TextView) findViewById(R.id.abstractContents);
		TextView trackView = (TextView) findViewById(R.id.trackTextView);
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
		LinearLayout speakerLayout = (LinearLayout) findViewById(R.id.speakersLayout);
		for (Speaker speaker : speakerList) {
			View newView = View.inflate(mContext, R.layout.speaker_view, null);
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
				mDb.toggleEventInMySchedule(mEvent.getSqlId(), 1);
			else
				mDb.toggleEventInMySchedule(mEvent.getSqlId(), 0);
		} else {
			Log.d("SUSEConferences", "Calendar clicked");
		}
	}
}
