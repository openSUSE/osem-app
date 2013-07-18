/*******************************************************************************
 * Copyright (c) 2012 Matt Barringer <matt@incoherent.de>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Matt Barringer <matt@incoherent.de> - initial API and implementation
 ******************************************************************************/
package de.incoherent.suseconferenceclient.tasks;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import de.incoherent.suseconferenceclient.SUSEConferences;
import de.incoherent.suseconferenceclient.activities.ScheduleDetailsActivity;
import de.incoherent.suseconferenceclient.app.AlarmReceiver;
import de.incoherent.suseconferenceclient.app.ConferenceCacher;
import de.incoherent.suseconferenceclient.app.Database;
import de.incoherent.suseconferenceclient.app.HTTPWrapper;
import de.incoherent.suseconferenceclient.app.ConferenceCacher.ConferenceCacherProgressListener;
import de.incoherent.suseconferenceclient.models.Conference;
import de.incoherent.suseconferenceclient.models.Event;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;


public class CheckForUpdatesTask extends AsyncTask<Void, String, Long> {
	public interface CheckForUpdatesListener {
		public void updatesChecked(long id, String error);
	}
	
	private Conference mConference;
	private ProgressDialog mDialog = null;
	private CheckForUpdatesListener mListener = null;
	private Database mDb;
	private String mErrorMessage = "";
	private Context mContext;
	
	public CheckForUpdatesTask(Context context, Conference conference, CheckForUpdatesListener listener) {
		this.mContext = context;
		this.mConference = conference;
		this.mListener = listener;
		this.mDb = SUSEConferences.getDatabase();
		mDialog = ProgressDialog.show(context, "", 
				"Checking for schedule updates...", true);
	}
	protected void onProgressUpdate(String... progress) {
		mDialog.setMessage("Loading " + progress[0]);
	}

	@Override
	protected Long doInBackground(Void... params) {
		String kUrl = "https://conference.opensuse.org/osem/api/v1/conferences/gRNyOIsTbvCfJY5ENYovBA";
		if (kUrl.length() <= 0)
			return 0l;
		
		String updatesUrl = mConference.getUrl() + "/updates.json";
		int lastUpdateRevision = mDb.getLastUpdateValue(mConference.getSqlId());
		int revisionLevel = lastUpdateRevision;
		
		try {
			JSONObject updateReply = HTTPWrapper.get(updatesUrl);
			if (updateReply == null)
				return 0l;
			int newLevel = updateReply.getInt("revision");
			if (newLevel > revisionLevel) {
				long id = mConference.getSqlId();
				// Cache favorites and alerts
				List<String> favoriteGuids = mDb.getFavoriteGuids(id);
				List<Event> alerts = mDb.getAlertEvents(id);
				List<String> alertGuids = new ArrayList<String>();
				// Now cancel all of the outstanding alerts, in case
				// a talk has been moved
				AlarmManager manager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
				for (Event e : alerts) {
					alertGuids.add("\"" + e.getGuid() + "\"");
					Log.d("SUSEConferences", "Removing an alert for " + e.getTitle());

					Intent intent = new Intent(mContext, AlarmReceiver.class);
					intent.putExtras(ScheduleDetailsActivity.generateAlarmIntentBundle(mContext, e));
					PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext,
												intent.getStringExtra("intentId").hashCode(),
												intent,
												PendingIntent.FLAG_UPDATE_CURRENT);
					manager.cancel(pendingIntent);
					pendingIntent.cancel();
				}
				
				// Now clear the DB
				mDb.clearDatabase(id);
				// Download schedule
				ConferenceCacher cacher = new ConferenceCacher(new ConferenceCacherProgressListener() {
					@Override
					public void progress(String progress) {
						publishProgress(progress);
					}
				});
				
				long val = cacher.cacheConference(mConference, mDb);
				mErrorMessage = cacher.getLastError();
				if (val == -1) {
					mDb.setConferenceAsCached(id, 0);
				} else {
					mDb.setLastUpdateValue(id, newLevel);
					mDb.toggleEventsInMySchedule(favoriteGuids);
					mDb.toggleEventAlerts(alertGuids);
					alerts = mDb.getAlertEvents(id);
					// ... And re-create the alerts, if they are in the future
					Date currentDate = new Date();
					for (Event e : alerts) {
						if (currentDate.after(e.getDate()))
							continue;
						Log.d("SUSEConferences", "Adding an alert for " + e.getTitle());
						alertGuids.add("\"" + e.getGuid() + "\"");
						Intent intent = new Intent(mContext, AlarmReceiver.class);
						intent.putExtras(ScheduleDetailsActivity.generateAlarmIntentBundle(mContext, e));
						PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext,
									intent.getStringExtra("intentId").hashCode(),
									intent, PendingIntent.FLAG_UPDATE_CURRENT);
						manager.set(AlarmManager.RTC_WAKEUP, e.getDate().getTime() - 300000 , pendingIntent);
					}

				}
				return val;
			} else {
				return 0l;
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

	protected void onPostExecute(Long id) {
		mDialog.dismiss();
		//this.mListener.updatesChecked(id, mErrorMessage);
	}
}
