/**
 * 
 */
package de.incoherent.suseconferenceclient.tasks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.incoherent.suseconferenceclient.R;
import de.incoherent.suseconferenceclient.adapters.SocialItemAdapter;
import de.incoherent.suseconferenceclient.app.SocialWrapper;
import de.incoherent.suseconferenceclient.models.SocialItem;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

/*
 * From a UI perspective, this is very clumsy.
 * 
 * TODO: Figure out how to handle the use case where the NewsFeedFragment is paused
 * while the AsyncTask runs in the background.  At the moment, it will result in onPostExecute
 * not being called.
 */
public class GetSocialItemsTask extends AsyncTask<String, Void, ArrayList<SocialItem>> {
	public interface GetSocialItemsListener {
		public void socialItemsLoaded(ArrayList<SocialItem> items);
	}
	
	private GetSocialItemsListener mListener;
	private Context mContext;
	private ProgressDialog mDialog = null;
	public GetSocialItemsTask(Context context, GetSocialItemsListener listener) {
		this.mListener = listener;
		this.mContext = context;
		mDialog = ProgressDialog.show(context, "", 
				"Downloading social feed...", true);
		}
	
	@Override
	protected ArrayList<SocialItem> doInBackground(String... params) {
		ArrayList<SocialItem> twitterItems = null;
		String searchTag = params[0];
		Log.d("SUSEConferences", "Fetching social feed for " + searchTag);

		if (!isCancelled()) {
			twitterItems = SocialWrapper.getTwitterItems(mContext, searchTag, 0);
		}
		
		if (!isCancelled()) {
			twitterItems.addAll(SocialWrapper.getGooglePlusItems(mContext, searchTag, 0));
			Collections.sort(twitterItems, Collections.reverseOrder());
		}
		
		return twitterItems;
	}
	
	protected void onPostExecute(ArrayList<SocialItem> items) {
		mDialog.dismiss();
		if (!isCancelled() && items != null) {
			mListener.socialItemsLoaded(items);
		}
	}

}

