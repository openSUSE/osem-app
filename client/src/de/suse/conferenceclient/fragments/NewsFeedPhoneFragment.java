package de.suse.conferenceclient.fragments;

import java.util.Collections;
import java.util.List;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;

import com.actionbarsherlock.app.SherlockListFragment;

import de.suse.conferenceclient.R;
import de.suse.conferenceclient.adapters.SocialItemAdapter;
import de.suse.conferenceclient.app.SocialWrapper;
import de.suse.conferenceclient.models.Event;
import de.suse.conferenceclient.models.SocialItem;

public class NewsFeedPhoneFragment extends SherlockListFragment {	
	public NewsFeedPhoneFragment() {}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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
			Collections.sort(twitterItems);
			return twitterItems;
		}
		
		protected void onPostExecute(List<SocialItem> items) {
			SocialItemAdapter adapter = new SocialItemAdapter(getActivity(), R.layout.social_item, items);
			setListAdapter(adapter);
		}

	}

}
