package de.suse.conferenceclient.fragments;

import java.util.Collections;
import java.util.List;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import de.suse.conferenceclient.R;
import de.suse.conferenceclient.adapters.SocialItemAdapter;
import de.suse.conferenceclient.app.SocialWrapper;
import de.suse.conferenceclient.models.SocialItem;

public class NewsFeedFragment extends SherlockListFragment {
	public String mSearchTag = "";
	protected int mFeedNumber = 2;
	
	public void NewsFeedFragment() {}
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	public void setSearch(String searchTag) {
		mSearchTag = searchTag;
		SocialTask task = new SocialTask();
		task.execute(searchTag);
	}
	
	protected class SocialTask extends AsyncTask<String, Void, List<SocialItem>> {
		@Override
		protected List<SocialItem> doInBackground(String... params) {
			String searchTag = params[0];
			List<SocialItem> twitterItems = SocialWrapper.getTwitterItems(getActivity(), searchTag, mFeedNumber);
			twitterItems.addAll(SocialWrapper.getGooglePlusItems(getActivity(), searchTag, mFeedNumber));
			Collections.sort(twitterItems, Collections.reverseOrder());
			if (mFeedNumber > 0)
				if (twitterItems.size() >= mFeedNumber)
					return twitterItems.subList(0, mFeedNumber);
			return twitterItems;
		}
		
		protected void onPostExecute(List<SocialItem> items) {
			SocialItemAdapter adapter = new SocialItemAdapter(getActivity(), R.layout.social_item, items);
			setListAdapter(adapter);
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
