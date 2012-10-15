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

import java.util.List;

import de.incoherent.suseconferenceclient.R;
import de.incoherent.suseconferenceclient.SUSEConferences;
import de.incoherent.suseconferenceclient.app.Database;
import de.incoherent.suseconferenceclient.models.Conference;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;


public class ConferenceListActivity extends Activity implements OnClickListener {
	long mActiveId = -1;
	RadioGroup mConferenceGroup;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.conference_list);
		SharedPreferences settings = getSharedPreferences("SUSEConferences", 0);
		mActiveId = settings.getLong("active_conference", -1);
		
		mConferenceGroup = (RadioGroup) findViewById(R.id.conferencesGroup);
        Database db = SUSEConferences.getDatabase();
		List<Conference> conferenceList = db.getConferenceList();
		for (Conference conference : conferenceList) {
			RadioButton newButton = new RadioButton(this);
			newButton.setId((int) conference.getSqlId());
			String text = conference.getName() + " - " + conference.getYear();
			if (conference.isCached())
				text += " (cached)";
			else
				text += " (not cached)";
			newButton.setText(text);
			mConferenceGroup.addView(newButton);
			if (mActiveId == conference.getSqlId())
				newButton.setChecked(true);
		}
		
		Button okButton = (Button) findViewById(R.id.okButton);
		okButton.setOnClickListener(this);
    }
    
    @Override
    public void finish() {
      Intent data = new Intent();
      data.putExtra("selected_conference", mConferenceGroup.getCheckedRadioButtonId());
      setResult(RESULT_OK, data);
      super.finish();
    }

	@Override
	public void onClick(View v) {
		this.finish();
	} 
}
