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

package de.incoherent.suseconferenceclient.fragments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import de.incoherent.suseconferenceclient.adapters.ScheduleAdapter;
import de.incoherent.suseconferenceclient.adapters.SocialItemAdapter;
import de.incoherent.suseconferenceclient.app.SocialWrapper;
import de.incoherent.suseconferenceclient.models.SocialItem;
import de.incoherent.suseconferenceclient.tasks.GetSocialItemsTask;
import de.incoherent.suseconferenceclient.tasks.GetSocialItemsTask.GetSocialItemsListener;
import de.incoherent.suseconferenceclient.R;

// TODO this fragment isn't being reloaded on conference changes
public class NewsFeedFragment extends SherlockListFragment implements GetSocialItemsListener {
	private String mSearchTag = null;
	// In the future, this may be used to present a short list of recent items
	protected int mFeedNumber = 0;
	private SocialItemAdapter mAdapter = null;
	private ArrayList<SocialItem> mItems;
    private int mIndex = -1;
    private int mTop = 0;
    private GetSocialItemsTask mActiveTask = null;
	public NewsFeedFragment() {}
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    setRetainInstance(true);
	    setHasOptionsMenu(true);
		Log.d("SUSEConferences", "NewsFeedFragment onCreate");
		if (savedInstanceState != null) {
			this.mSearchTag = savedInstanceState.getString("searchTag");
			this.mFeedNumber = savedInstanceState.getInt("feedNumber");
			this.mItems = savedInstanceState.getParcelableArrayList("items");
			mAdapter = new SocialItemAdapter(getActivity(), R.layout.social_item, mItems);
			setListAdapter(mAdapter);
		} else {
			Bundle args = getArguments();
			if (mSearchTag == null)
				mSearchTag = args.getString("socialTag");
			setEmptyList();
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		if(mIndex!=-1){
			this.getListView().setSelectionFromTop(mIndex, mTop);
		}
	}
	
	public void loadNewConference(String searchTag) {
		this.mSearchTag = searchTag;
		if (isAdded())
			requery();
	}

	public void requery() {
		Log.d("SUSEConferences", "NewsFeedFragment requery");
		mIndex = -1;
		mTop = 0;
		runTask();
	}

	public void runTask() {
		mActiveTask = new GetSocialItemsTask(getActivity(), this);
		mActiveTask.execute(mSearchTag);
		
		// If the user is on a slow network, or the Twitter/G+ servers
		// are running slowly, we don't want the spinner to spin endlessly,
		// so set a timeout and they can refresh it later.
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				if (mActiveTask.getStatus() == AsyncTask.Status.RUNNING) {
					Log.d("SUSEConferences", "SocialTask timed out");
					mActiveTask.cancel(true);
					setEmptyList();
				}
			}
		}, 30000);
	}

	public void setEmptyList() {
		ArrayList<SocialItem> items = new ArrayList<SocialItem>();
		mAdapter = new SocialItemAdapter(getActivity(), R.layout.social_item, items);
		setListAdapter(mAdapter);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		Log.d("SUSEConferences", "NewsFeedFragment onPause");
		try{
			mIndex = this.getListView().getFirstVisiblePosition();
			View v = this.getListView().getChildAt(0);
			mTop = (v == null) ? 0 : v.getTop();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		  super.onSaveInstanceState(savedInstanceState);
		  savedInstanceState.putString("searchTag", this.mSearchTag);
		  savedInstanceState.putInt("feedNumber", this.mFeedNumber);
		  savedInstanceState.putParcelableArrayList("items", mItems);
			if (mActiveTask != null && mActiveTask.getStatus() != AsyncTask.Status.FINISHED) {
				mActiveTask.cancel(true);
			}
	}
	
	@Override
	public void onListItemClick (ListView l,
								 View v,
								 int position,
								 long id) {
		SocialItem item = (SocialItem) l.getItemAtPosition(position);
		if (item.getLink().equals(""))
			return;

		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(item.getLink()));
		startActivity(intent);
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu,  MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		menu.add(Menu.CATEGORY_SYSTEM, R.id.socialRefreshItem, 1, getString(R.string.refreshNews))
    	 .setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem menuItem) {
		int itemId = menuItem.getItemId();
		if (itemId == R.id.socialRefreshItem) {
			requery();
			return true;
		}
			return super.onOptionsItemSelected(menuItem);
	}
	
	@Override
	public void socialItemsLoaded(ArrayList<SocialItem> items) {
		mAdapter = new SocialItemAdapter(getActivity(), R.layout.social_item, items);
		setListAdapter(mAdapter);
	}
}

