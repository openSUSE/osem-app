/**
 * 
 */
package de.suse.conferenceclient.activities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;

import de.suse.conferenceclient.R;
import de.suse.conferenceclient.SUSEConferences;
import de.suse.conferenceclient.app.Database;
import de.suse.conferenceclient.app.ScheduleViewPager;
import de.suse.conferenceclient.dialogs.ScheduleItemDetailDialog;
import de.suse.conferenceclient.dialogs.ScheduleItemDetailDialog.OnFavoriteListener;
import de.suse.conferenceclient.fragments.NewsFeedFragment;
import de.suse.conferenceclient.fragments.ScheduleDetailsFragment;
import de.suse.conferenceclient.fragments.ScheduleDetailsFragment.OnDetailsListener;
import de.suse.conferenceclient.fragments.ScheduleFragment;
import de.suse.conferenceclient.fragments.ScheduleFragment.OnEventListener;
import de.suse.conferenceclient.models.Event;
import de.suse.conferenceclient.views.ScheduleView;
import de.suse.conferenceclient.views.ScheduleView.OnEventClickListener;

/**
 * @author Matt Barringer <mbarringer@suse.de>
 *
 */
public class ScheduleActivity extends SherlockFragmentActivity implements OnFavoriteListener, OnEventListener, OnPageChangeListener, OnDetailsListener {
	public static int FULL_SCHEDULE = 0;
	public static int MY_SCHEDULE = 1;
	
    private Database db;
    private long conferenceId;
    
    private HashMap<String, List<Event>> dailyEvents;
    private TextView mActiveDay;
    private TextView mAgendaTitle;
    private boolean mMySchedule = false;
    private List<Event> mEventList;
    private ScheduleDetailsFragment mDetailsFragment;
    private ScheduleViewPager mPager;
    private ScheduleFragment mScheduleFragment;
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agenda);
        ActionBar bar = getSupportActionBar();
      	FragmentManager fm = getSupportFragmentManager();

      	this.db = SUSEConferences.getDatabase();
		this.conferenceId = getIntent().getLongExtra("conferenceId", -1);
		int type = getIntent().getIntExtra("type", 0);

		mPager = (ScheduleViewPager) findViewById(R.id.agendaViewPager);
		mPager.setPagingEnabled(false);
		mDetailsFragment = new ScheduleDetailsFragment(this);

		if (type == FULL_SCHEDULE) {
			mEventList = db.getScheduleTitles(conferenceId);
	        bar.setTitle(getString(R.string.fullSchedule));
		} else {
			mEventList = db.getMyScheduleTitles(conferenceId);
	        bar.setTitle(getString(R.string.mySchedule));
	        mMySchedule = true;
		}
		
		mScheduleFragment = new ScheduleFragment(mEventList, this.conferenceId, mMySchedule, this);
		SchedulePagerAdapter adapter = new SchedulePagerAdapter(fm, mScheduleFragment, mDetailsFragment);
		mPager.setAdapter(adapter);
		mPager.setOnPageChangeListener(this);
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

	@Override
	public void onFavoriteToggle(boolean checked, Event event) {
		mScheduleFragment.toggleFavorite(checked, event);
	}

	@Override
	public void eventClicked(Event event) {
		mDetailsFragment.setEvent(event, conferenceId);
		mPager.setPagingEnabled(true);
		mPager.setCurrentItem(1, true);
	}

	@Override
	public void onPageScrollStateChanged(int state) {
		if (state == ViewPager.SCROLL_STATE_IDLE) {
			if (mPager.getCurrentItem() == 0)
				mPager.setPagingEnabled(false);
		}
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// Don't care
	}

	@Override
	public void onPageSelected(int arg0) {
		// Don't care	
	}
	
	public static class SchedulePagerAdapter extends FragmentPagerAdapter {
		private ScheduleFragment mScheduleFragment;
		private ScheduleDetailsFragment mDetailsFragment;
		public SchedulePagerAdapter(FragmentManager fm,
									ScheduleFragment scheduleFragment,
									ScheduleDetailsFragment detailsFragment) {
			super(fm);
			this.mScheduleFragment = scheduleFragment;
			this.mDetailsFragment = detailsFragment;
		}

		@Override
		public Fragment getItem(int index) {
			if (index == 0)
				return this.mScheduleFragment;
			else
				return this.mDetailsFragment;
		}

		@Override
		public int getCount() {
			return 2;
		}
		
	}

}
