/**
 * 
 */
package de.suse.conferenceclient.fragments;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockListFragment;

import de.suse.conferenceclient.Config;
import de.suse.conferenceclient.R;
import de.suse.conferenceclient.adapters.SocialItemAdapter;
import de.suse.conferenceclient.app.HTTPWrapper;
import de.suse.conferenceclient.app.SocialWrapper;
import de.suse.conferenceclient.models.SocialItem;

/**
 * @author Matt Barringer <mbarringer@suse.de>
 *
 */
public class GooglePlusFragment extends SherlockListFragment {
	private String mSearchTag;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	public void setSearch(String searchTag) {
		mSearchTag = searchTag;
		GetSearchTask task = new GetSearchTask();
		task.execute(searchTag);
	}
	
	private class GetSearchTask extends AsyncTask<String, Void, List<SocialItem>> {
		@Override
		protected List<SocialItem> doInBackground(String... params) {
			String searchTag = params[0];
			return SocialWrapper.getGooglePlusItems(getActivity(), searchTag);
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
