package de.suse.conferenceclient.activities;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import de.suse.conferenceclient.R;
import de.suse.conferenceclient.SUSEConferences;
import de.suse.conferenceclient.adapters.TabAdapter;
import de.suse.conferenceclient.app.Database;
import de.suse.conferenceclient.app.HTTPWrapper;
import de.suse.conferenceclient.fragments.MySchedulePhoneFragment;
import de.suse.conferenceclient.fragments.NewsFeedFragment;
import de.suse.conferenceclient.fragments.NewsFeedPhoneFragment;
import de.suse.conferenceclient.fragments.SchedulePhoneFragment;
import de.suse.conferenceclient.fragments.WhatsOnFragment;
import de.suse.conferenceclient.models.Conference;
import de.suse.conferenceclient.models.Venue;
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
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;
import android.widget.ListView;

public class HomeActivity extends SherlockFragmentActivity implements 
		GetConferencesTask.ConferenceListListener, WheelView.OnLaunch, OnClickListener {

	private ViewPager mPhonePager;
	private TabAdapter mTabsAdapter;
	private NewsFeedFragment mNewsFeedFragment;
	private WhatsOnFragment mWhatsOnFragment;
	private long mConferenceId = -1;
	private ProgressDialog mDialog;
	private static Matrix mMatrix;
	private boolean mIsTablet = false;
	
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
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	if (!mIsTablet) {
    		menu.add(Menu.NONE, R.id.mapsOptionMenuItem, Menu.NONE, getString(R.string.mapsOptionMenuItem))
    			.setIcon(R.drawable.icon_venue_off)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
    	}
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
    	switch (menuItem.getItemId()) {
    	case R.id.mapsOptionMenuItem:
    		Intent i = new Intent(HomeActivity.this, VenueMapsActivity.class);
    		i.putExtra("venueId", mConferenceId);
    		startActivity(i);
    		return true;
    	}
    	
    	return super.onOptionsItemSelected(menuItem);
    }

    private void setView() {
    	if (mMatrix == null)
    		mMatrix = new Matrix();
    	if (mDialog != null)
        	mDialog.dismiss();
      setContentView(R.layout.activity_home);
      Database db = SUSEConferences.getDatabase();
      Conference conference = db.getConference(mConferenceId);

      mPhonePager= (ViewPager) findViewById(R.id.phonePager);
      if (mPhonePager !=  null) { // Phone layout
      	ActionBar bar = getSupportActionBar();
      	bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
      	Bundle args = new Bundle();
      	args.putLong("conferenceId", this.mConferenceId);
      	args.putString("socialTag", conference.getSocialTag());
      	mTabsAdapter = new TabAdapter(this, mPhonePager);
      	mTabsAdapter.addTab(
      			bar.newTab().setText(getString(R.string.mySchedule)),
      			MySchedulePhoneFragment.class, args);
      	mTabsAdapter.addTab(
      			bar.newTab().setText(getString(R.string.fullSchedule)),
      			SchedulePhoneFragment.class, args);
      	mTabsAdapter.addTab(
      			bar.newTab().setText(getString(R.string.newsFeed)),
      			NewsFeedPhoneFragment.class, args);
      } else { // Tablet layout
    	mIsTablet = true;
    	TextView t = (TextView) findViewById(R.id.conferenceNameTextView);
    	t.setText(conference.getName());
    	t = (TextView) findViewById(R.id.locationTextView);
    	t.setText(conference.getDateRange());
      	FragmentManager fm = getSupportFragmentManager();
      	WheelView view = (WheelView) findViewById(R.id.wheelView);
      	view.setOnLaunchListener(this);
      	mNewsFeedFragment = (NewsFeedFragment) fm.findFragmentById(R.id.newsFeedFragment);
      	mNewsFeedFragment.setSearch(conference.getSocialTag());
      	
      	mWhatsOnFragment = (WhatsOnFragment) fm.findFragmentById(R.id.upcomingFragment);
      	mWhatsOnFragment.setConferenceId(mConferenceId);
      			
		ImageButton mapButton = (ImageButton) findViewById(R.id.mapButton);
		mapButton.setOnClickListener(this);
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

    		SharedPreferences settings = getSharedPreferences("SUSEConferences", 0);
    		SharedPreferences.Editor editor = settings.edit();
    		editor.putLong("active_conference", mConferenceId);
    		editor.commit();

    		SUSEConferences app = ((SUSEConferences) getApplicationContext());
    		app.setActiveId(mConferenceId);
    		if (id != -1)
    			setView();
    	}
    }

    /*
     * Downloads all of the data about a conference and stores it
     * in SQLite.
     */
    private class CacheConferenceTask extends AsyncTask<Void, Void, Long> {    	
    	private Conference mConference;
    	private Database db;
    	
    	public CacheConferenceTask(Conference conference) {
    		this.mConference = conference;
    		this.db = SUSEConferences.getDatabase();
    	}

    	@Override
    	protected Long doInBackground(Void... params) {
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
				Log.d("SUSEConferences", "Venues: " + venueUrl);

    			JSONObject venueReply = HTTPWrapper.get(venueUrl);
    			JSONObject venue = venueReply.getJSONObject("venue");
    			String infoUrl = url + "/" + venue.getString("info_text");
    			String info = HTTPWrapper.getRawText(infoUrl);
    			String venueName = venue.getString("name");
    			String venueAddr =  venue.getString("address");
    			long venueId = db.insertVenue(venue.getString("guid"),
    										  venueName,
    										  venueAddr,
    										  info);
    			JSONArray mapPoints = venue.getJSONArray("map_points");
    			int mapLen = mapPoints.length();
    			for (int i = 0; i < mapLen; i++) {
    				JSONObject point = mapPoints.getJSONObject(i);
    				String lat = point.getString("lat");
    				String lon = point.getString("lon");
    				String type = point.getString("type");
    				String name = venueName;
    				String addr = venueAddr;
    				String desc = "Conference Venue";
    				
    				if (!type.equals("venue")) {
    					name = point.getString("name");
    					addr = point.getString("address");
    					desc = point.getString("description");
    				} 
    				db.insertVenuePoint(venueId, lat, lon, type, name, addr, desc);
    			}
    			db.setConferenceVenue(venueId, mConferenceId);
    			
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
    				JSONObject event = events.getJSONObject(i);
    				String guid = event.getString("guid");
    				String track = event.getString("track");
    				Long trackId = trackMap.get(track);
    				Long roomId = roomMap.get(event.getString("room"));

    				Log.d("SUSEConferences", "Track: " + track + ":" + trackId);
    				if (track.equals("meta")) {
    					Log.d("SUSEConferences", "Found a meta track");
    					
    					// The "meta" track is used to insert information
    					// into the schedule that automatically appears on "my schedule",
    					// and also isn't clickable.
    					db.insertEvent(guid,
    								   mConference.getSqlId(),
    								   roomId.longValue(),
    								   trackId.longValue(),
    								   event.getString("date"),
    								   event.getInt("length"),
    								   "",
    								   "",
    								   event.getString("title"),
    								   "",
    								   "");
    				} else {
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
    		return mConference.getSqlId();
    	}

    	protected void onPostExecute(Long v) {
    		SharedPreferences settings = getSharedPreferences("SUSEConferences", 0);
    		SharedPreferences.Editor editor = settings.edit();
    		editor.putLong("active_conference", v.longValue());
    		editor.commit();

    		SUSEConferences app = ((SUSEConferences) getApplicationContext());
    		app.setActiveId(v.longValue());

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
		} else if (activity == WheelView.ACTIVITY_SOCIAL) {
			Intent intent = new Intent(HomeActivity.this, SocialActivity.class);
			intent.putExtra("conferenceId", mConferenceId);
			startActivity(intent);
		}
	}
	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.mapButton) {
	    	Database db = SUSEConferences.getDatabase();
			long venueId = db.getConferenceVenue(mConferenceId);
			Intent intent = new Intent(HomeActivity.this, VenueMapsActivity.class);
			intent.putExtra("venueId", venueId);
			startActivity(intent);
		}
	}

}
