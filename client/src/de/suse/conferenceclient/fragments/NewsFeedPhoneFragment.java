package de.suse.conferenceclient.fragments;

import android.os.Bundle;
import android.util.Log;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import de.suse.conferenceclient.R;

public class NewsFeedPhoneFragment extends NewsFeedFragment {
	public NewsFeedPhoneFragment() {}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		mFeedNumber = 0;

		Bundle args = getArguments();
		String tag = args.getString("socialTag");
		Log.d("SUSEConferences", "NewsFeedPhoneFragment searching for " + tag);
		SocialTask task = new SocialTask();
		task.execute(tag);
		
	}
}
