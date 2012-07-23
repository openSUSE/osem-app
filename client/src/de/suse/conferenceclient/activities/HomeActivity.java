package de.suse.conferenceclient.activities;

import com.actionbarsherlock.app.SherlockActivity;

import de.suse.conferenceclient.R;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class HomeActivity extends SherlockActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
    }

}
