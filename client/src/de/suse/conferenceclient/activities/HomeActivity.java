package de.suse.conferenceclient.activities;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.SherlockFragmentActivity;

import de.suse.conferenceclient.R;
import de.suse.conferenceclient.SUSEConferences;
import de.suse.conferenceclient.adapters.TabAdapter;
import de.suse.conferenceclient.adapters.WhatsOnAdapter;
import de.suse.conferenceclient.app.Database;
import de.suse.conferenceclient.app.DatabaseHelper;
import de.suse.conferenceclient.app.HTTPWrapper;
import de.suse.conferenceclient.fragments.MyScheduleFragment;
import de.suse.conferenceclient.fragments.NewsFeedFragment;
import de.suse.conferenceclient.fragments.WhatsOnFragment;
import de.suse.conferenceclient.models.Conference;
import de.suse.conferenceclient.models.Event;
import de.suse.conferenceclient.tasks.GetConferencesTask;
import de.suse.conferenceclient.views.WheelView;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Matrix;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;
import android.widget.ListView;

public class HomeActivity extends SherlockFragmentActivity implements 
		GetConferencesTask.ConferenceListListener, WheelView.OnLaunch {
	private ViewPager mViewPager;
	private TabAdapter mTabsAdapter;
	private MyScheduleFragment mMyScheduleFragment;
	private NewsFeedFragment mNewsFeedFragment;
	private WhatsOnFragment mWhatsOnFragment;
	private ImageView mWheelView;
	private long mConferenceId = -1;
	private ProgressDialog mDialog;
	private static Matrix mMatrix;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mDialog = null;
        mConferenceId = ((SUSEConferences) getApplicationContext()).getActiveId();
        if (mConferenceId == -1) {
        	Log.d("SUSEConferences", "Conference ID is -1");
        	loadConferences();
        } else {
        	Log.d("SUSEConferences", "Conference ID is NOT -1");
        	setView();
        }
    }
    
    private void setView() {
    	if (mMatrix == null)
    		mMatrix = new Matrix();
    	if (mDialog != null)
        	mDialog.dismiss();
      setContentView(R.layout.activity_home);
      mViewPager = (ViewPager) findViewById(R.id.homePager);
      if (mViewPager !=  null) {
      	// Phone layout
      	ActionBar bar = getSupportActionBar();
          bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

          mTabsAdapter = new TabAdapter(this, mViewPager);

          mTabsAdapter.addTab(
                          bar.newTab().setText(getString(R.string.mySchedule)),
                          MyScheduleFragment.class, null);
          mTabsAdapter.addTab(
                          bar.newTab().setText(getString(R.string.whatsOn)),
                          WhatsOnFragment.class, null);
          mTabsAdapter.addTab(
                  bar.newTab().setText(getString(R.string.newsFeed)),
                  NewsFeedFragment.class, null);
      } else {
      	// Tablet layout
      	FragmentManager fm = getSupportFragmentManager();
      	WheelView view = (WheelView) findViewById(R.id.wheelView);
      	view.setOnLaunchListener(this);
//      	mMyScheduleFragment = (MyScheduleFragment) fm.findFragmentById(R.id.myScheduleFragment); 
      	mNewsFeedFragment = (NewsFeedFragment) fm.findFragmentById(R.id.newsFeedFragment);
		LinearLayout whatsOnLayout = (LinearLayout) findViewById(R.id.whatsOnLayout);
		LayoutInflater inflater = LayoutInflater.from(this);
		Database db = SUSEConferences.getDatabase();
		List<Event> eventList = db.getNextTwoEvents(mConferenceId);
		SimpleDateFormat formatter = new SimpleDateFormat("MMM dd HH:mm");
		for (Event event : eventList) {
			View root = inflater.inflate(R.layout.whats_on_list_item, null);
			TextView title = (TextView) root.findViewById(R.id.titleTextView);
			TextView room = (TextView) root.findViewById(R.id.roomTextView);
			TextView time = (TextView) root.findViewById(R.id.timeTextView);
			title.setText(event.getTitle());
			room.setText(event.getRoomName());
			time.setText(formatter.format(event.getDate()));
			whatsOnLayout.addView(root);
		}
//      	mWhatsOnFragment = (WhatsOnFragment) fm.findFragmentById(R.id.whatsOnFragment);
//      	mWhatsOnFragment.setConferenceId(mConferenceId);
      	
      }
    }

    private void loadConferences() {
    	 mDialog = ProgressDialog.show(HomeActivity.this, "", 
                "Loading. Please wait...", true);
    	GetConferencesTask task = new GetConferencesTask("http://incoherent.de/suseconferenceapp", this);
    	task.execute();
    }

    @Override
	public void handled(ArrayList<Conference> conferences) {
    	if (conferences.size() == 1) {
    		conferenceChosen(conferences.get(0));
    	} else if (conferences.size() > 1) {
    		// Show the list
    	}
    }
    
    private void conferenceChosen(Conference conference) {
    	Database db = SUSEConferences.getDatabase();
    	long id = db.getConferenceIdFromGuid(conference.getGuid());
    	if (id == -1) {
    		mConferenceId = db.addConference(conference);
    		conference.setSqlId(mConferenceId);
    		CacheConferenceTask task = new CacheConferenceTask(conference);
    		task.execute();
    	} else {
    		mConferenceId = id;
    	}

    	SharedPreferences settings = getSharedPreferences("SUSEConferences", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong("active_conference", mConferenceId);
        editor.commit();
        
        SUSEConferences app = ((SUSEConferences) getApplicationContext());
        app.setActiveId(mConferenceId);
        if (id != -1)
        	setView();
    }
    
    /*
     * OnTouchListener for the wheel
     */
    private class WheelOnTouchListener implements OnTouchListener {
        
        private double startAngle;
     
        @Override
        public boolean onTouch(View v, MotionEvent event) {
     
            switch (event.getAction()) {
                 
                case MotionEvent.ACTION_DOWN:
                	Log.d("SUSEConferences", "Rotating");
                    mMatrix.postRotate(10);
                    mWheelView.setImageMatrix(mMatrix);
                    break;
                case MotionEvent.ACTION_MOVE:
                    break;
                     
                case MotionEvent.ACTION_UP:
                     
                    break;
            }       
             
            return true;
        }
         
    }
    /*
     * Downloads all of the data about a conference and stores it
     * in SQLite.
     */
    private class CacheConferenceTask extends AsyncTask<Void, Void, Void> {    	
    	private Conference mConference;
    	private Database db;
    	
    	public CacheConferenceTask(Conference conference) {
    		this.mConference = conference;
    		this.db = SUSEConferences.getDatabase();
    	}

    	@Override
    	protected Void doInBackground(Void... params) {
    		String url = mConference.getUrl();
    		String eventsUrl = url + "/events.json";
    		String roomsUrl = url + "/rooms.json";
    		String speakersUrl = url + "/speakers.json";
    		String tracksUrl = url + "/tracks.json";
    		String venueUrl = url + "/venue.json";
    		HashMap<String, Long> roomMap = new HashMap<String, Long>();
    		HashMap<String, Long> trackMap = new HashMap<String, Long>();
    		HashMap<String, Long> speakerMap = new HashMap<String, Long>();
    		
    		try {
				Log.d("SUSEConferences", "Venues");

    			JSONObject venueReply = HTTPWrapper.get(venueUrl);
    			JSONObject venue = venueReply.getJSONObject("venue");
    			long venueId = db.insertVenue(venue.getString("guid"),
    										  venue.getString("name"),
    										  venue.getString("address"));
    			
				Log.d("SUSEConferences", "Rooms");

    			JSONObject roomsReply = HTTPWrapper.get(roomsUrl);
    			JSONArray rooms = roomsReply.getJSONArray("rooms");
    			int roomsLen = rooms.length();
    			for (int i = 0; i < roomsLen; i++) {
    				JSONObject room = rooms.getJSONObject(i);
    				String guid = room.getString("guid");
    				Long roomId = db.insertRoom(guid,
    											room.getString("name"),
    											room.getString("description"),
    											venueId);
    				roomMap.put(guid, roomId);
    			}
				Log.d("SUSEConferences", "Tracks");

    			JSONObject tracksReply = HTTPWrapper.get(tracksUrl);
    			JSONArray tracks = tracksReply.getJSONArray("tracks");
    			int tracksLen = tracks.length();
    			for (int i = 0; i < tracksLen; i++) {
    				JSONObject track = tracks.getJSONObject(i);
    				String guid = track.getString("guid");
    				Long trackId = db.insertTrack(guid,
    											   track.getString("name"),
    											   track.getString("color"),
    											   mConference.getSqlId());
    				trackMap.put(guid, trackId);
    			}
				Log.d("SUSEConferences", "Speakers");

    			JSONObject speakersReply = HTTPWrapper.get(speakersUrl);
    			JSONArray speakers = speakersReply.getJSONArray("speakers");
    			int speakersLen = speakers.length();
    			for (int i = 0; i < speakersLen; i++) {
    				JSONObject speaker = speakers.getJSONObject(i);
    				String guid = speaker.getString("guid");
    				Long speakerId = db.insertSpeaker(guid,
    											   speaker.getString("name"),
    											   speaker.getString("company"),
    											   speaker.getString("biography"),
    											   "");
    				speakerMap.put(guid, speakerId);
    			}

				Log.d("SUSEConferences", "Events");

    			JSONObject eventsReply = HTTPWrapper.get(eventsUrl);
    			JSONArray events = eventsReply.getJSONArray("events");
    			int eventsLen = events.length();
    			for (int i = 0; i < eventsLen; i++) {
    				Log.d("SUSEConferences", "Event #" + i);
    				JSONObject event = events.getJSONObject(i);
    				String guid = event.getString("guid");
    				Long roomId = roomMap.get(event.getString("room"));
    				if (roomId == null) continue;
    				
    				Long trackId = trackMap.get(event.getString("track"));
    				if (trackId == null) continue;
    				
    				Long eventId = db.insertEvent(guid,
    											   mConference.getSqlId(),
    											   roomId.longValue(),
    											   trackId.longValue(),
    											   event.getString("date"),
    											   event.getInt("length"),
    											   event.getString("type"),
    											   event.getString("language"),
    											   event.getString("title"),
    											   event.getString("abstract"),
    											   "");
    				
    				JSONArray eventSpeakers = event.getJSONArray("speaker_ids");
    				int eventSpeakersLen = eventSpeakers.length();
    				for (int j = 0; j < eventSpeakersLen; j++) {
    					Long speakerId = speakerMap.get(eventSpeakers.getString(j));
    					if (speakerId != null)
    						db.insertEventSpeaker(speakerId, eventId);
    				}
    			}
    		} catch (IllegalStateException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		} catch (SocketException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		} catch (UnsupportedEncodingException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		} catch (IOException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		} catch (JSONException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
    		return null;
    	}

    	protected void onPostExecute(Void v) {
    		Log.d("SUSEConferences", "OnPostExecute");
        	setView();
    	}

    }

	@Override
	public void launchActivity(int activity) {
		if (activity == WheelView.ACTIVITY_SCHEDULE) {
			Intent intent = new Intent(HomeActivity.this, ScheduleActivity.class);
			intent.putExtra("conferenceId", mConferenceId);
			intent.putExtra("type", ScheduleActivity.FULL_SCHEDULE);
			startActivity(intent);
		} else if (activity == WheelView.ACTIVITY_MYSCHEDULE) {
			Intent intent = new Intent(HomeActivity.this, ScheduleActivity.class);
			intent.putExtra("conferenceId", mConferenceId);
			intent.putExtra("type", ScheduleActivity.MY_SCHEDULE);
			startActivity(intent);
		}
	}

}
