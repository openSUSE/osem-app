package de.suse.conferenceclient.activities;

import java.util.ArrayList;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.SherlockFragmentActivity;

import de.suse.conferenceclient.R;
import de.suse.conferenceclient.SUSEConferences;
import de.suse.conferenceclient.adapters.TabAdapter;
import de.suse.conferenceclient.app.Database;
import de.suse.conferenceclient.fragments.MyScheduleFragment;
import de.suse.conferenceclient.fragments.NewsFeedFragment;
import de.suse.conferenceclient.fragments.WhatsOnFragment;
import de.suse.conferenceclient.models.Conference;
import de.suse.conferenceclient.tasks.GetConferencesTask;

import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;

public class HomeActivity extends SherlockFragmentActivity implements GetConferencesTask.ConferenceListListener {
	private ViewPager mViewPager;
	private TabAdapter mTabsAdapter;
	private MyScheduleFragment mMyScheduleFragment;
	private NewsFeedFragment mNewsFeedFragment;
	private WhatsOnFragment mWhatsOnFragment;
	private long mConferenceId = -1;
	private ProgressDialog mDialog;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
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
      	mMyScheduleFragment = (MyScheduleFragment) fm.findFragmentById(R.id.myScheduleFragment); 
      	mNewsFeedFragment = (NewsFeedFragment) fm.findFragmentById(R.id.newsFeedFragment);
      	mWhatsOnFragment = (WhatsOnFragment) fm.findFragmentById(R.id.whatsOnFragment);
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
    	mDialog.dismiss();
    	if (conferences.size() == 1) {
    		conferenceChosen(conferences.get(0));
    	} else if (conferences.size() > 1) {
    		// Show the list
    	}
    	
    	setView();
    }
    
    private void conferenceChosen(Conference conference) {
    	Database db = SUSEConferences.getDatabase();
    	long id = db.getConferenceIdFromGuid(conference.getGuid());
    	if (id == -1) {
    		mConferenceId = db.addConference(conference);
    		
    	} else {
    		mConferenceId = id;
    	}

    	SharedPreferences settings = getPreferences(0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong("active_conference", mConferenceId);
        
        SUSEConferences app = ((SUSEConferences) getApplicationContext());
        app.setActiveId(mConferenceId);
    }
}
