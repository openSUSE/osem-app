package de.suse.conferenceclient.fragments;

import android.os.Bundle;
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
		
		SocialTask task = new SocialTask();
		task.execute(tag);
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu,  MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		menu.add(Menu.NONE, R.id.socialRefreshItem, Menu.NONE, getString(R.string.refresh))
		 .setIcon(R.drawable.refresh)
    	 .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
	}
}
