/**
 * 
 */
package de.incoherent.suseconferenceclient.tasks;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.incoherent.suseconferenceclient.SUSEConferences;
import de.incoherent.suseconferenceclient.app.Database;
import de.incoherent.suseconferenceclient.app.HTTPWrapper;
import de.incoherent.suseconferenceclient.models.Conference;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

/**
 * @author Matt Barringer <mbarringer@suse.de>
 *
 */
public class CheckForUpdatesTask extends AsyncTask<Void, Void, Integer> {
	public interface CheckForUpdatesListener {
		public void updatesChecked(int foundSome);
	}
	
	private Conference mConference;
	private ProgressDialog mDialog = null;
	private CheckForUpdatesListener mListener = null;
	private Database mDb;
	
	public CheckForUpdatesTask(Context context, Conference conference, CheckForUpdatesListener listener) {
		this.mConference = conference;
		this.mListener = listener;
		this.mDb = SUSEConferences.getDatabase();
		mDialog = ProgressDialog.show(context, "", 
				"Checking for schedule updates...", true);
	}
	
	@Override
	protected Integer doInBackground(Void... params) {
		String updatesUrl = mConference.getUrl() + "/updates.json";
		int lastUpdateRevision = mDb.getLastUpdateValue(mConference.getSqlId());
		int revisionLevel = lastUpdateRevision;
		
		try {
			JSONObject updateReply = HTTPWrapper.get(updatesUrl);
			if (updateReply == null)
				return 0;
			JSONArray updateArray = updateReply.getJSONArray("updates");
			int updateLen = updateArray.length();
			for (int i = 0; i < updateLen; i++) {
				JSONObject update = updateArray.getJSONObject(i);
				int revision = update.getInt("revision");
				if (revision <= lastUpdateRevision)
					continue;
				if (revision > revisionLevel)
					revisionLevel = revision;
				
				String guid = update.getString("guid");
				String type = update.getString("type");
				String field = update.getString("field");
				String value = update.getString("value");
				
				
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
		return null;
	}

}
