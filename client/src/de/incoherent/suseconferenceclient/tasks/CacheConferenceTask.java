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

import de.incoherent.suseconferenceclient.SUSEConferences;
import de.incoherent.suseconferenceclient.app.ConferenceCacher;
import de.incoherent.suseconferenceclient.app.ConferenceCacher.ConferenceCacherProgressListener;
import de.incoherent.suseconferenceclient.app.Database;
import de.incoherent.suseconferenceclient.models.Conference;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

/*
 * This task calls the ConferenceCacher function to get the data for the conference
 */
public class CacheConferenceTask extends AsyncTask<Void, String, Long> {
	public interface CacheConferenceTaskListener {
		public void conferenceCached(long id, String message);
	}
	
	private Conference mConference;
	private Database db;
	private String mErrorMessage = "";
	private ProgressDialog mDialog = null;
	private CacheConferenceTaskListener mListener;
	
	public CacheConferenceTask(Context context, Conference conference, CacheConferenceTaskListener listener) {
		this.mConference = conference;
		this.db = SUSEConferences.getDatabase();
		this.mListener = listener;
		mDialog = ProgressDialog.show(context, "", 
				"Downloading data for " + mConference.getName(), true);
	}
	
	protected void onProgressUpdate(String... progress) {
		mDialog.setMessage("Loading " + progress[0]);
	}

	@Override
	protected Long doInBackground(Void... params) {
		Log.d("SUSEConferences", "Caching data from " + mConference.getUrl());
		ConferenceCacher cacher = new ConferenceCacher(new ConferenceCacherProgressListener() {
			@Override
			public void progress(String progress) {
				publishProgress(progress);
			}
		});
		
		long val = cacher.cacheConference(mConference, db);
		mErrorMessage = cacher.getLastError();
		return val;
	}

	protected void onPostExecute(Long id) {
		if (id != -1) {
			db.setConferenceAsCached(id, 1);
		}
		
		mDialog.dismiss();
		this.mListener.conferenceCached(id, mErrorMessage);
	}


}
