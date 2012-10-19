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

package de.incoherent.suseconferenceclient.activities;

import java.util.ArrayList;
import java.util.List;

import de.incoherent.suseconferenceclient.Config;
import de.incoherent.suseconferenceclient.R;
import de.incoherent.suseconferenceclient.SUSEConferences;
import de.incoherent.suseconferenceclient.app.Database;
import de.incoherent.suseconferenceclient.models.Conference;
import de.incoherent.suseconferenceclient.tasks.CacheConferenceTask;
import de.incoherent.suseconferenceclient.tasks.CacheConferenceTask.CacheConferenceTaskListener;
import de.incoherent.suseconferenceclient.tasks.GetConferencesTask;
import de.incoherent.suseconferenceclient.tasks.GetConferencesTask.ConferenceListListener;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class ConferenceListActivity extends Activity implements OnClickListener, ConferenceListListener, CacheConferenceTaskListener {
	private long mActiveId = -1;
	private RadioGroup mConferenceGroup;
	private ProgressDialog mDialog = null;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.conference_list);
		SharedPreferences settings = getSharedPreferences("SUSEConferences", 0);
		mActiveId = settings.getLong("active_conference", -1);
		Log.d("SUSEConferences", "Active ID: " + mActiveId);
		mConferenceGroup = (RadioGroup) findViewById(R.id.conferencesGroup);
		showConferences();

		Button okButton = (Button) findViewById(R.id.okButton);
		okButton.setOnClickListener(this);
		Button refreshButton = (Button) findViewById(R.id.refreshButton);
		refreshButton.setOnClickListener(this);
    }
    
    @Override
    public void onBackPressed() {
        Intent data = new Intent();
        data.putExtra("selected_conference", -1);
        setResult(RESULT_CANCELED, data);
        finish();
    }
    
    private void showConferences() {
		mConferenceGroup = (RadioGroup) findViewById(R.id.conferencesGroup);
		mConferenceGroup.clearCheck();
		mConferenceGroup.removeAllViews();
		
        Database db = SUSEConferences.getDatabase();
		List<Conference> conferenceList = db.getConferenceList();

		for (Conference conference : conferenceList) {
			RadioButton newButton = new RadioButton(this);
			newButton.setTag(conference);
			newButton.setId((int) conference.getSqlId());
			String text = conference.getName() + " - " + conference.getYear();
			if (conference.isCached())
				text += " (cached)";
			else
				text += " (not cached)";
			newButton.setText(text);
			mConferenceGroup.addView(newButton);
			Log.d("SUSEConferences", "ActiveId: " + mActiveId + " sqlId: " + conference.getSqlId());
			if (mActiveId == conference.getSqlId())
				newButton.setChecked(true);
		}
    }
    
    public void done(long id) {
        Intent data = new Intent();
        data.putExtra("selected_conference", id);
        setResult(RESULT_OK, data);
        finish();
    }
    
	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.okButton) {
			int id = mConferenceGroup.getCheckedRadioButtonId();
			for(int i = 0; i < mConferenceGroup.getChildCount(); i++) {
				RadioButton button = (RadioButton) mConferenceGroup.getChildAt(i);
				if(button.getId() == id) {
					Conference conference = (Conference) button.getTag();
					if (!conference.isCached()) {
						CacheConferenceTask task = new CacheConferenceTask(this, conference, this);
						task.execute();
					} else {
						done(id);
					}
				}
			}
		} else if (v.getId() == R.id.refreshButton) {
			mDialog = ProgressDialog.show(ConferenceListActivity.this, "", 
					"Downloading conference list, please wait...", true);
			GetConferencesTask task = new GetConferencesTask(Config.BASE_URL, this);
			task.execute();
		}
	}

	@Override
	public void conferencesDownloaded(ArrayList<Conference> conferences) {
		if (mDialog != null)
			mDialog.dismiss();
		showConferences();
	}


	@Override
	public void conferenceCached(long id, String message) {
		Log.d("SUSEConferences", "ConferenceListActivity: conferenceCached: " + id);
		if (id == -1) {
			done(mActiveId);
		} else {
			done(id);
		}
	} 
}
