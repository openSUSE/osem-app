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
import de.incoherent.suseconferenceclient.R;

public class NewsFeedFragment extends SherlockListFragment {
	private String mSearchTag = "";
	// In the future, this may be used to present a short list of recent items
	protected int mFeedNumber = 0;
	private SocialItemAdapter mAdapter = null;
	private ArrayList<SocialItem> mItems;
    private int mIndex = -1;
    private int mTop = 0;

	public void NewsFeedFragment() {}
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			this.mSearchTag = savedInstanceState.getString("searchTag");
			this.mFeedNumber = savedInstanceState.getInt("feedNumber");
			this.mItems = savedInstanceState.getParcelableArrayList("items");
			mAdapter = new SocialItemAdapter(getActivity(), R.layout.social_item, mItems);
			setListAdapter(mAdapter);
		} else {
			Bundle args = getArguments();
			mSearchTag = args.getString("socialTag");
			SocialTask task = new SocialTask();
			task.execute(mSearchTag);
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
		requery();
	}
	
	public void requery() {
		mIndex = -1;
		mTop = 0;
		if (mAdapter != null)
			mAdapter.clear();
		Bundle args = getArguments();
		mSearchTag = args.getString("socialTag");
		SocialTask task = new SocialTask();
		task.execute(mSearchTag);
	}

	@Override
	public void onPause() {
		super.onPause();
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
	}
	
	protected class SocialTask extends AsyncTask<String, Void, ArrayList<SocialItem>> {
		@Override
		protected ArrayList<SocialItem> doInBackground(String... params) {
			Log.d("SUSEConferences", "Fetching social feed");
			String searchTag = params[0];
			ArrayList<SocialItem> twitterItems = SocialWrapper.getTwitterItems(getActivity(), searchTag, mFeedNumber);
			twitterItems.addAll(SocialWrapper.getGooglePlusItems(getActivity(), searchTag, mFeedNumber));
			Collections.sort(twitterItems, Collections.reverseOrder());
			return twitterItems;
		}
		
		@SuppressWarnings("ucd")
		protected void onPostExecute(ArrayList<SocialItem> items) {
			mItems = items;
			mAdapter = new SocialItemAdapter(getActivity(), R.layout.social_item, items);
			setListAdapter(mAdapter);
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
		menu.add(Menu.NONE, R.id.socialRefreshItem, Menu.NONE, getString(R.string.refresh))
		 .setIcon(R.drawable.refresh)
    	 .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
	}

}
