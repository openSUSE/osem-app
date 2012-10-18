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

package de.incoherent.suseconferenceclient.fragments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockDialogFragment;

import de.incoherent.suseconferenceclient.R;
import de.incoherent.suseconferenceclient.SUSEConferences;
import de.incoherent.suseconferenceclient.activities.HomeActivity;
import de.incoherent.suseconferenceclient.app.Database;
import de.incoherent.suseconferenceclient.models.Track;

/*
 * This dialog allows the user to set the languages and
 * tracks they want to list.
 * 
 * It stores the selected items in SharedPreferences, under
 * $CONFERENCENAME_language_filter and $CONFERENCENAME_track_filter.
 */
public class FilterDialogFragment extends SherlockDialogFragment {
	private Database mDb;
	
    public static FilterDialogFragment newInstance(long conferenceId, String conferenceName) {
    	FilterDialogFragment fragment = new FilterDialogFragment();
        Bundle args = new Bundle();
        args.putLong("conferenceId", conferenceId);
        args.putString("conferenceName", conferenceName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.filter_dialog_fragment, null);		
        final LinearLayout languageLayout = (LinearLayout) view.findViewById(R.id.languageLayout);
        final LinearLayout trackLayout = (LinearLayout) view.findViewById(R.id.tracksLayout);

        Bundle args = getArguments();
        final long conferenceId = args.getLong("conferenceId");
        final String conferenceName = args.getString("conferenceName");
        final String languageSetting = conferenceName + "_language_filter";
        final String trackSetting = conferenceName + "_track_filter";
        
		final SharedPreferences settings = getActivity().getSharedPreferences("SUSEConferences", 0);
		Set<String> filteredLanguages = getSetFromString(settings.getString(languageSetting, null));
		Set<String> filteredTrackIds = getSetFromString(settings.getString(trackSetting, null));
		
		this.mDb = SUSEConferences.getDatabase();
		String[] languages = mDb.getUniqueLanguages(conferenceId);
		int len = languages.length;
		for (int i = 0; i < len; i++) {
			String name = languages[i];
			if (name.length() > 0) {
				CheckBox box = new CheckBox(getActivity());
				if (filteredLanguages == null || filteredLanguages.contains(name))
					box.setChecked(true);
				box.setText(name);
				languageLayout.addView(box);
			}
		}
		
		List<Track> trackList = mDb.getUniqueTracks(conferenceId);
		if (trackList.size() == 0) {
			TextView trackView = (TextView) view.findViewById(R.id.tracksTextView);
			trackView.setVisibility(View.GONE);
			trackLayout.setVisibility(View.GONE);
		} else {
			for (Track t : trackList) {
				String id = String.valueOf(t.getId());
				CheckBox box = new CheckBox(getActivity());
				if (filteredTrackIds == null || filteredTrackIds.contains(id))
					box.setChecked(true);
				box.setText(t.getName());
				box.setId((int) t.getId());
				trackLayout.addView(box);
			}
		}
		
        builder.setView(view)
        	   .setTitle("Show only...")
               .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int id) {
                	   List<String> strList = new ArrayList<String>();                	   
                	   SharedPreferences.Editor editor = settings.edit();

                	   int boxcount = languageLayout.getChildCount();
                	   int i;
                	   for (i = 0; i < boxcount; i++){
                	         CheckBox box = (CheckBox) languageLayout.getChildAt(i);
                	         if (box.isChecked()) {
                	        	 strList.add("\"" + box.getText().toString() + "\"");
                	         }
                	   }
                	   editor.putString(languageSetting, TextUtils.join(",", strList));
                	   strList.clear();
                	   boxcount = trackLayout.getChildCount();
                	   for (i = 0; i < boxcount; i++){
                	         CheckBox box = (CheckBox) trackLayout.getChildAt(i);
                	         if (box.isChecked()) {
                	        	 strList.add(String.valueOf(box.getId()));
                	         }
                	   }
                	   editor.putString(trackSetting, TextUtils.join(",", strList));
                	   editor.commit();
                	   ((HomeActivity)getActivity()).filterSet();
                   }
               })
               .setNegativeButton(R.string.cancel, null);
        return builder.create();
    }


    private Set<String> getSetFromString(String str) {
    	if (str == null) return null;
    	String toSplit = str.replace("\"", "");
    	String[] array = toSplit.split(",");
    	Set<String> ret = new HashSet<String>(Arrays.asList(array));
    	return ret;
    }
}
