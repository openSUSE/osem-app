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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockDialogFragment;

import de.incoherent.suseconferenceclient.R;

public class ChangeLogDialogFragment extends SherlockDialogFragment {
	public static ChangeLogDialogFragment newInstance() {
		ChangeLogDialogFragment newFrag = new ChangeLogDialogFragment();
        return newFrag;
	}
	
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.change_log_dialog_fragment, null);	
        final TextView changelogView = (TextView) view.findViewById(R.id.changeLogTextView);
        InputStream input = getActivity().getResources().openRawResource(R.raw.changes);
        InputStreamReader inputreader = new InputStreamReader(input);
        BufferedReader reader = new BufferedReader(inputreader);
        StringBuilder text = new StringBuilder();
        String line;

        try {
        	while ((line = reader.readLine()) != null) {
        		text.append(line);
        	}
        } catch (IOException e) {
        	return null;
        }

        changelogView.setText(Html.fromHtml(text.toString()));
        builder.setView(view).setTitle("Changelog");
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
        	@Override
        	public void onClick(DialogInterface dialog, int which) {
        		dismiss();
        	}
        });
        return builder.create();
    }
}
