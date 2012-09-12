/**
 * 
 */
package de.suse.conferenceclient.fragments;

import java.util.List;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockListFragment;

import de.suse.conferenceclient.R;
import de.suse.conferenceclient.adapters.SocialItemAdapter;
import de.suse.conferenceclient.app.SocialWrapper;
import de.suse.conferenceclient.models.SocialItem;

/**
 * @author Matt Barringer <mbarringer@suse.de>
 *
 */
@SuppressLint("NewApi")
public class TwitterFragment extends SherlockListFragment {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	public void setSearch(String searchTag) {
		GetSearchTask task = new GetSearchTask();
		task.execute(searchTag);
	}
	
	private class GetSearchTask extends AsyncTask<String, Void, List<SocialItem>> {
		@Override
		protected List<SocialItem> doInBackground(String... params) {
			String searchTag = params[0];
			return SocialWrapper.getTwitterItems(getActivity(), searchTag, 0);
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
		if (item.getLink().isEmpty())
			return;

		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(item.getLink()));
		startActivity(intent);
	}


}
