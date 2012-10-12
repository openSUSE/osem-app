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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.conn.HttpHostConnectException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import de.incoherent.suseconferenceclient.SUSEConferences;
import de.incoherent.suseconferenceclient.adapters.TabAdapter;
import de.incoherent.suseconferenceclient.app.AboutDialog;
import de.incoherent.suseconferenceclient.app.Database;
import de.incoherent.suseconferenceclient.app.HTTPWrapper;
import de.incoherent.suseconferenceclient.fragments.FilterDialogFragment;
import de.incoherent.suseconferenceclient.fragments.MySchedulePhoneFragment;
import de.incoherent.suseconferenceclient.fragments.NewsFeedFragment;
import de.incoherent.suseconferenceclient.fragments.SchedulePhoneFragment;
import de.incoherent.suseconferenceclient.models.Conference;
import de.incoherent.suseconferenceclient.tasks.GetConferencesTask;
import de.incoherent.suseconferenceclient.R;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class HomeActivity extends SherlockFragmentActivity implements 
		GetConferencesTask.ConferenceListListener {

	private ViewPager mPhonePager;
	private TabAdapter mTabsAdapter;
	private long mConferenceId = -1;
	private ProgressDialog mDialog;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDialog = null;
        mConferenceId = ((SUSEConferences) getApplicationContext()).getActiveId();
        if (mConferenceId == -1) {
        	Log.d("SUSEConferences", "Conference ID is -1");
        	if (!hasInternet()) {
        		Log.d("SUSEConferences", "Info is null");
        		AlertDialog.Builder builder = new AlertDialog.Builder(this);
        		builder.setMessage("Please enable internet access and try again.");
        		builder.setCancelable(false);
        		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
        		           public void onClick(DialogInterface dialog, int id) {
        		        	   Log.d("SUSEConferences", "Exiting");
        		               HomeActivity.this.finish();
        		           }
        		       });
        		builder.show();
        	} else {
        		loadConferences();
        	}
        } else {
        	Log.d("SUSEConferences", "Conference ID is NOT -1");
        	if (savedInstanceState != null)
        		setView(false);
        	else
        		setView(true);
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	menu.add(Menu.NONE, R.id.mapsOptionMenuItem, Menu.NONE, getString(R.string.mapsOptionMenuItem))
    	.setIcon(R.drawable.icon_venue_off)
    	.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
    	
//    	menu.add(Menu.NONE, R.id.checkForUpdates, Menu.NONE, getString(R.string.menu_checkForUpdates))
//    	.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
//		menu.add(Menu.NONE, R.id.settingsItem, Menu.NONE, getString(R.string.menu_settings))
//        .setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
		menu.add(Menu.NONE, R.id.filterEvents, Menu.NONE, getString(R.string.filter))
        .setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
		menu.add(Menu.NONE, R.id.aboutItem, Menu.NONE, getString(R.string.menu_about))
        .setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
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
    	case R.id.aboutItem:
            AboutDialog about = new AboutDialog(this);
            about.setTitle("About");
            about.show();
            return true;
    	case R.id.filterEvents:
    	    FragmentManager fragmentManager = getSupportFragmentManager();
    	    FilterDialogFragment newFragment = FilterDialogFragment.newInstance(this.mConferenceId);
	        newFragment.show(fragmentManager, "Filter");
    		return true;
//    	case R.id.checkForUpdates:
//    		checkForUpdates();
//    		return true;
    	}
    	
    	return super.onOptionsItemSelected(menuItem);
    }

    
    // When the device is rotated, we don't want to go and load up
    // the social stream again, so loadSocial will be set to false
    // and it will just reuse the existing fragment.
    private void setView(boolean loadSocial) {
    	if (mDialog != null)
        	mDialog.dismiss();
      setContentView(R.layout.activity_home);
      Database db = SUSEConferences.getDatabase();
      Conference conference = db.getConference(mConferenceId);
      getSupportActionBar().setTitle(conference.getName());
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
    	  if (hasInternet())
    		  mTabsAdapter.addTab(
    				  bar.newTab().setText(getString(R.string.newsFeed)),
    				  NewsFeedFragment.class, args);
      } else { // Tablet layout
    	  FragmentManager fm = getSupportFragmentManager();
    	  Bundle args = new Bundle();
    	  args.putLong("conferenceId", this.mConferenceId);
    	  args.putString("socialTag", conference.getSocialTag());

    	  MySchedulePhoneFragment mySched = new MySchedulePhoneFragment();
    	  mySched.setArguments(args);
    	  fm.beginTransaction()
    	  .add(R.id.myScheduleFragmentLayout, mySched).commit();

    	  SchedulePhoneFragment sched = new SchedulePhoneFragment();
    	  sched.setArguments(args);
    	  fm.beginTransaction()
    	  .add(R.id.scheduleFragmentLayout, sched).commit();

    	  if (hasInternet() && loadSocial) {
    		  NewsFeedFragment newsFeed = new NewsFeedFragment();
    		  newsFeed.setArguments(args);
    		  fm.beginTransaction().add(R.id.socialLayout, newsFeed).commit();
    	  }
    	  
    	  if (!hasInternet()) {
    		  RelativeLayout horizontal = (RelativeLayout) findViewById(R.id.newsFeedHorizontalLayout);
    		  if (horizontal != null)
    			  horizontal.setVisibility(View.GONE);
    		  FrameLayout socialLayout = (FrameLayout) findViewById(R.id.socialLayout);
    		  socialLayout.setVisibility(View.GONE);
    		  TextView labelView = (TextView) findViewById(R.id.newsFeedTextView);
    		  labelView.setVisibility(View.GONE);
    	  }
      }
    }

    private void loadConferences() {    	
    	mDialog = ProgressDialog.show(HomeActivity.this, "", 
    			"Loading. Please wait...", true);
    	GetConferencesTask task = new GetConferencesTask("http://incoherent.de/conferences", this);
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
    			setView(true);
    	}
    }

//    private void checkForUpdates() {
//    	if (!hasInternet()) {
//    		Toast.makeText(this, "You don't have internet access!", 3).show();
//    		return;
//    	}
//    }
    
    
    private boolean hasInternet() {
    	boolean ret = true;
    	ConnectivityManager manager =  (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    	NetworkInfo info = manager.getActiveNetworkInfo();
    	if (info == null || !info.isConnected())
    		ret = false;
    		
    	return ret;
    }
    
    /*
     * Check the server for schedule updates
     */
//    private class ScheduleUpdatesTask extends AsyncTask<Void, Void, Integer> {
//    	private Conference mConference;
//    	private Database mDb;
//    	
//    	public ScheduleUpdatesTask(Conference conference) {
//    		this.mConference = conference;
//    		this.mDb = SUSEConferences.getDatabase();
//    	}
//    	
//    	private void handleAdd(JSONObject object) throws JSONException {
//    		
//    	}
//
//    	private void handleEdit(JSONObject object) throws JSONException {
//    		String room = null;
//    		String guid = object.getString("guid");
//    		if (object.has("room"))
//    			room = object.getString("room");
//    			
//    	}
//    	
//    	private void handleDelete(JSONObject object) throws JSONException {
//    		
//    	}
//
//    	@Override
//    	protected Integer doInBackground(Void... params) {
//    		String url = mConference.getUrl();
//    		String updatesUrl = url + "/updates.json";
//    		long lastUpdateTime = mDb.getLastUpdateTime(mConference.getSqlId());
//    		long newUpdateTime = lastUpdateTime;
//    		try {
//				JSONObject updateReply = HTTPWrapper.get(updatesUrl);
//				if (updateReply == null)
//					return 0;
//				JSONArray updateArray = updateReply.getJSONArray("updates");
//				int len = updateArray.length();
//				for (int i = 0; i < len; i++) {
//					JSONObject update = updateArray.getJSONObject(i);
//					long time = update.getLong("timestamp");
//					if (time > lastUpdateTime) {
//						newUpdateTime = time;
//						String type = update.getString("type");
//						if (type.equals("edit"))
//							handleEdit(update);
//						
//					}
//				}
//				
//				mDb.setLastUpdateTime(mConference.getSqlId(), newUpdateTime);
//			} catch (IllegalStateException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (SocketException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (UnsupportedEncodingException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (JSONException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//    		
//    		return 0;
//    	}
//    }
    /*
     * Downloads all of the data about a conference and stores it
     * in SQLite.
     */
    private class CacheConferenceTask extends AsyncTask<Void, Void, Long> {    	
    	private Conference mConference;
    	private Database db;
    	private String mErrorMessage = "";
    	
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
    		Long returnVal = null;
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
    			String offlineMap = "";
    			String offlineMapBounds = "";
    			if (venue.has("offline_map")) {
    				offlineMap = venue.getString("offline_map");
    				offlineMapBounds = venue.getString("offline_map_bounds");
    			}
    			
    			long venueId = db.insertVenue(venue.getString("guid"),
    										  venueName,
    										  venueAddr,
    										  offlineMap,
    										  offlineMapBounds,
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
    			
    			if (venue.has("map_polygons")) {
	    			JSONArray polygons = venue.getJSONArray("map_polygons");
	    			int polygonLen = polygons.length();
	    			for (int j = 0; j < polygonLen; j++) {
	    				JSONObject polygon = polygons.getJSONObject(j);
	    				String name = polygon.getString("name");
	    				String label = polygon.getString("label");
	    				String lineColorStr = polygon.getString("line_color");
	    				String fillColorStr = "#00000000";
	    				if (polygon.has("fill_color"))
	    					fillColorStr = polygon.getString("fill_color");
	    				
	    				List<String> stringList = new ArrayList<String>();
	    				JSONArray points = polygon.getJSONArray("points");
	    				int pointsLen = points.length();
	    				for (int k = 0; k < pointsLen; k++) {
	    					String newPoint = points.getString(k);
	    					stringList.add(newPoint);
	    				}
	    				String joined = TextUtils.join(";", stringList);	    				
	    				int lineColor = Color.parseColor(lineColorStr);
	    				int fillColor = Color.parseColor(fillColorStr);
	    				db.insertVenuePolygon(venueId, name, label, lineColor, fillColor, joined);
	    			}
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
    				if (track.equals("meta")) {
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
    			e.printStackTrace();
    			mErrorMessage = e.getLocalizedMessage();
    			returnVal = Long.valueOf(-1);
    		} catch (SocketException e) {
    			e.printStackTrace();
    			mErrorMessage = e.getLocalizedMessage();
    			returnVal = Long.valueOf(-1);
    		} catch (UnsupportedEncodingException e) {
    			e.printStackTrace();
    			mErrorMessage = e.getLocalizedMessage();
    			returnVal = Long.valueOf(-1);
    		} catch (IOException e) {
    			e.printStackTrace();
    			mErrorMessage = e.getLocalizedMessage();
    			returnVal = Long.valueOf(-1);
    		} catch (JSONException e) {
    			e.printStackTrace();
    			mErrorMessage = e.getLocalizedMessage();
    			returnVal = Long.valueOf(-1);
    		} 
    		
    		if (returnVal == null)
    			returnVal = mConference.getSqlId();
    		return returnVal;
    	}

    	// TODO Don't destroy the database once more than one conference is available
    	protected void onPostExecute(Long id) {
    		if (id == -1) {
    			Log.d("SUSEConferences", "Error!");
    			db.clearDatabase();
    	    	if (mDialog != null)
    	        	mDialog.dismiss();

	    		SharedPreferences settings = getSharedPreferences("SUSEConferences", 0);
	    		SharedPreferences.Editor editor = settings.edit();
	    		editor.putLong("active_conference", -1);
	    		editor.commit();

        		AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
        		builder.setMessage(mErrorMessage);
        		builder.setCancelable(false);
        		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
        		           public void onClick(DialogInterface dialog, int id) {
        		               HomeActivity.this.finish();
        		           }
        		       });
        		builder.show();
    		} else {
	    		SharedPreferences settings = getSharedPreferences("SUSEConferences", 0);
	    		SharedPreferences.Editor editor = settings.edit();
	    		editor.putLong("active_conference", id.longValue());
	    		editor.commit();
	
	    		SUSEConferences app = ((SUSEConferences) getApplicationContext());
	    		app.setActiveId(id.longValue());
	
	        	setView(true);
    		}
    	}
    }
}
