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

import java.io.File;

import com.actionbarsherlock.app.SherlockMapActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;

import de.incoherent.suseconferenceclient.SUSEConferences;
import de.incoherent.suseconferenceclient.app.Database;
import de.incoherent.suseconferenceclient.maps.GoogleMap;
import de.incoherent.suseconferenceclient.maps.MapInterface;
import de.incoherent.suseconferenceclient.maps.OSMMap;
import de.incoherent.suseconferenceclient.maps.OSMMapView;
import de.incoherent.suseconferenceclient.models.Venue;
import de.incoherent.suseconferenceclient.R;

// TODO This probably still doesn't work on the kindle
public class VenueMapsActivity extends SherlockMapActivity {
	private File mOfflineMap = null;
	private Venue mVenue;
	private boolean mHasOfflineMap = false;
	private MapInterface mMap;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		long venueId = getIntent().getLongExtra("venueId", -1);
		setContentView(R.layout.activity_info);
		Database db = SUSEConferences.getDatabase();
		mVenue = db.getVenueInfo(venueId);
		String offlineMapUrl = mVenue.getOfflineMapUrl();
		String offlineMapFilename = "";
		if (offlineMapUrl.length() > 0) {
			offlineMapFilename = offlineMapUrl.substring(offlineMapUrl.lastIndexOf('/')+1, offlineMapUrl.length());
			mOfflineMap = new File(getExternalFilesDir(null), offlineMapFilename);
			Log.d("SUSEConferences", "Offline map path: " + mOfflineMap.getAbsolutePath());
		}

		if (mOfflineMap != null && mOfflineMap.canRead()) {
			useOfflineMaps();
			mHasOfflineMap = true;
		} else {
			Log.d("SUSEConferences", "Can't find offline map");
			useOnlineMaps();
		}
		
		mMap.setupMap(mVenue);
		View view = mMap.getView();
		LinearLayout layout = (LinearLayout) findViewById(R.id.mapsLinearLayout);
		if (view != null)
			layout.addView(view, new LayoutParams(
					LayoutParams.MATCH_PARENT,
					LayoutParams.MATCH_PARENT));
	}

    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	private void useOfflineMaps() {
		mMap = new OSMMap(this, mOfflineMap);
	}

	private void useOnlineMaps() {
		if (hasGoogleMaps())
			mMap = new GoogleMap(this);
		else
			mMap = new OSMMap(this, null);
	}
	
    // Google Maps don't work on Kindle devices, so use osmdroid in that case
    private boolean hasGoogleMaps() {
    	try {
    		Class.forName("com.google.android.maps.MapView");
    		return true;
    	} catch (ClassNotFoundException e) {
    		return false;
    	}
    }

}
