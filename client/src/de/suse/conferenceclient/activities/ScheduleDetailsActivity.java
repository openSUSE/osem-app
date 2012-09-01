package de.suse.conferenceclient.activities;

import android.os.Bundle;
import android.util.Log;

import com.actionbarsherlock.app.SherlockFragmentActivity;

import de.suse.conferenceclient.R;
import de.suse.conferenceclient.SUSEConferences;
import de.suse.conferenceclient.app.Database;
import de.suse.conferenceclient.fragments.ScheduleDetailsFragment.OnDetailsListener;
import de.suse.conferenceclient.models.Event;


// Only useful for the phone
public class ScheduleDetailsActivity extends SherlockFragmentActivity implements OnDetailsListener {
	private Event mEvent;
	public ScheduleDetailsActivity() {
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_phone_details);
		Bundle extras = getIntent().getExtras();
		long eventId = extras.getLong("eventId");
		long conferenceId = extras.getLong("conferenceId");
		Database db = SUSEConferences.getDatabase();
		mEvent = db.getEvent(conferenceId, eventId);
	}
	@Override
    public void onSaveInstanceState (Bundle outState) {
		Log.d("SUSEConferences", "Activity onSaveInstance");
	}
	@Override
	public void onFavoriteToggle(boolean checked, Event event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Event getCurrentEvent() {
		Log.d("SUSEConferences", "Get currentEvent");
		return mEvent;
	}

}
