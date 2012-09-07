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
import de.suse.conferenceclient.activities.ScheduleDetailsActivity;
import de.suse.conferenceclient.adapters.SocialItemAdapter;
import de.suse.conferenceclient.adapters.PhoneScheduleAdapter.ScheduleItem;
import de.suse.conferenceclient.app.SocialWrapper;
import de.suse.conferenceclient.models.Event;
import de.suse.conferenceclient.models.SocialItem;

public class NewsFeedPhoneFragment extends SherlockListFragment {	
	public NewsFeedPhoneFragment() {}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);

		Bundle args = getArguments();
		String tag = args.getString("socialTag");
		
		SocialTask task = new SocialTask();
		task.execute(tag);
	}
	
	private class SocialTask extends AsyncTask<String, Void, List<SocialItem>> {
		@Override
		protected List<SocialItem> doInBackground(String... params) {
			String searchTag = params[0];
			List<SocialItem> twitterItems = SocialWrapper.getTwitterItems(getActivity(), searchTag);
			twitterItems.addAll(SocialWrapper.getGooglePlusItems(getActivity(), searchTag));
			Collections.sort(twitterItems, Collections.reverseOrder());
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
