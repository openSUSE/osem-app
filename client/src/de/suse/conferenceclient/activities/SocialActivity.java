/**
 * 
 */
package de.suse.conferenceclient.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;

import com.actionbarsherlock.app.SherlockFragmentActivity;

import de.suse.conferenceclient.R;
import de.suse.conferenceclient.fragments.GooglePlusFragment;
import de.suse.conferenceclient.fragments.TwitterFragment;

/**
 * @author Matt Barringer <mbarringer@suse.de>
 *
 */
public class SocialActivity extends SherlockFragmentActivity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_social);
      	FragmentManager fm = getSupportFragmentManager();
      	TwitterFragment twitter = (TwitterFragment) fm.findFragmentById(R.id.twitterFragment);
      	GooglePlusFragment plus = (GooglePlusFragment) fm.findFragmentById(R.id.gPlusFragment);
      	twitter.setSearch("opensuse");
      	plus.setSearch("ubuntu");
    }
}
