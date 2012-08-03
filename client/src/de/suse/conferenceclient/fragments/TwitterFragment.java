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

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockListFragment;

import de.suse.conferenceclient.R;
import de.suse.conferenceclient.adapters.SocialItemAdapter;
import de.suse.conferenceclient.app.HTTPWrapper;
import de.suse.conferenceclient.models.SocialItem;

/**
 * @author Matt Barringer <mbarringer@suse.de>
 *
 */
public class TwitterFragment extends SherlockListFragment {
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
			String twitterSearch = "http://search.twitter.com/search.json?q=" + searchTag;
			List<SocialItem> socialItems = new ArrayList<SocialItem>();
			
			try {
				JSONObject result = HTTPWrapper.get(twitterSearch);
				JSONArray items = result.getJSONArray("results");
				int len = items.length();
				for (int i = 0; i < len; i++) {
					JSONObject jsonItem = items.getJSONObject(i);
					Bitmap image = HTTPWrapper.getImage(jsonItem.getString("profile_image_url"));
					SocialItem newItem = new SocialItem(jsonItem.getString("from_user"),
														jsonItem.getString("text"),
														jsonItem.getString("created_at"),
														image);

					socialItems.add(newItem);
				}
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return socialItems;
		}
		
		protected void onPostExecute(List<SocialItem> items) {
			SocialItemAdapter adapter = new SocialItemAdapter(getActivity(), R.layout.social_item, items);
			setListAdapter(adapter);
		}

	}

}
