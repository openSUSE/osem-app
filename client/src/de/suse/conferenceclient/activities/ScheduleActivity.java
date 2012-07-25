/**
 * 
 */
package de.suse.conferenceclient.activities;

import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.actionbarsherlock.app.SherlockFragmentActivity;

import de.suse.conferenceclient.R;
import de.suse.conferenceclient.SUSEConferences;
import de.suse.conferenceclient.app.Database;
import de.suse.conferenceclient.models.Event;
import de.suse.conferenceclient.views.ScheduleView;

/**
 * @author Matt Barringer <mbarringer@suse.de>
 *
 */
public class ScheduleActivity extends SherlockFragmentActivity {
	
    private Database db;
    private long conferenceId;
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agenda);
		this.db = SUSEConferences.getDatabase();
		this.conferenceId = getIntent().getLongExtra("conferenceId", -1);
		List<Event> eventList = db.getScheduleTitles(conferenceId);
		Log.d("SUSEConferences", "Got " + eventList.size() + " events");
		
		ScheduleView view = (ScheduleView) findViewById(R.id.scheduleView);
		view.setEvents(eventList);
    }
    
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
    }

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
    }
}
