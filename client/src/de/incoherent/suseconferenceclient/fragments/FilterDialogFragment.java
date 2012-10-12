/**
 * 
 */
package de.incoherent.suseconferenceclient.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import com.actionbarsherlock.app.SherlockDialogFragment;

import de.incoherent.suseconferenceclient.R;
import de.incoherent.suseconferenceclient.SUSEConferences;
import de.incoherent.suseconferenceclient.app.Database;

/**
 * @author Matt Barringer <mbarringer@suse.de>
 *
 */
public class FilterDialogFragment extends SherlockDialogFragment {
	private Database mDb;
    public static FilterDialogFragment newInstance(long conferenceId) {
    	FilterDialogFragment fragment = new FilterDialogFragment();
        Bundle args = new Bundle();
        args.putLong("conferenceId", conferenceId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.filter_dialog_fragment, null);		
        LinearLayout languageLayout = (LinearLayout) view.findViewById(R.id.languageLayout);
        
        long conferenceId = getArguments().getLong("conferenceId");
        
		this.mDb = SUSEConferences.getDatabase();
		String[] languages = mDb.getUniqueLanguages(conferenceId);
		int len = languages.length;
		for (int i = 0; i < len; i++) {
			CheckBox box = new CheckBox(getActivity());
			box.setText(languages[i]);
			languageLayout.addView(box);
		}
        builder.setView(view)
               .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int id) {
                	   // TODO ...
                   }
               });
        return builder.create();
    }


}
