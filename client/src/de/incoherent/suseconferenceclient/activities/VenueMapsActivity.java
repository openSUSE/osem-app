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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import org.osmdroid.util.BoundingBoxE6;

import com.actionbarsherlock.app.SherlockMapActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;

import de.incoherent.suseconferenceclient.SUSEConferences;
import de.incoherent.suseconferenceclient.app.AboutDialog;
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
	private boolean mDownloadedOfflineMap = false;
	private boolean mUsingOfflineMap = false;
	private String mOfflineMapUrl;
	private String mOfflineMapFilename;
	private int mDownloadPercent = -1;
	private MapInterface mMap = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		long venueId = getIntent().getLongExtra("venueId", -1);
		setContentView(R.layout.activity_info);
		Database db = SUSEConferences.getDatabase();
		mVenue = db.getVenueInfo(venueId);
		mOfflineMapUrl = mVenue.getOfflineMapUrl();
		mOfflineMapFilename = "";
		if (mOfflineMapUrl.length() > 0) {
			mHasOfflineMap = true;
			mOfflineMapFilename = mOfflineMapUrl.substring(mOfflineMapUrl.lastIndexOf('/')+1, mOfflineMapUrl.length());
			mOfflineMap = new File(getExternalFilesDir(null), mOfflineMapFilename);
			Log.d("SUSEConferences", "Offline map path: " + mOfflineMap.getAbsolutePath());
		}

		if (mOfflineMap != null && mOfflineMap.canRead()) {
			mDownloadedOfflineMap = true;
			useOfflineMaps();
		} else {
			Log.d("SUSEConferences", "Can't find offline map");
			useOnlineMaps();
		}
		attachMap();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if (mMap != null)
			mMap.enableLocation();
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mMap != null)
			mMap.disableLocation();
	}
	
    public boolean onPrepareOptionsMenu(Menu menu) {
    	menu.clear();
    	if (mHasOfflineMap && hasInternet() && !mDownloadedOfflineMap) {
    		if (mDownloadPercent > 0) {
    			String percent = "Downloading map (" + mDownloadPercent + "%)";
    			menu.add(Menu.NONE, R.id.downloadMap, Menu.NONE, percent)
    			.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
    		} else {
    			menu.add(Menu.NONE, R.id.downloadMap, Menu.NONE, getString(R.string.downloadMap))
    			.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
    		}
    	} else if (mUsingOfflineMap) {
			menu.add(Menu.NONE, R.id.switchToOnlineMap, Menu.NONE, getString(R.string.switchToOnlineMap))
			.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
    	} else if (!mUsingOfflineMap && mDownloadedOfflineMap) {
			menu.add(Menu.NONE, R.id.switchToOfflineMap, Menu.NONE, getString(R.string.switchToOfflineMap))
			.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
    	}
    	
        return super.onPrepareOptionsMenu(menu);
    }
    
    
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
    	switch (menuItem.getItemId()) {
    	case R.id.downloadMap:
    		if (mDownloadPercent < 0)
    			new DownloadMapTask().execute(mOfflineMapUrl, mOfflineMap.getAbsolutePath());
            return true;
    	case R.id.switchToOfflineMap:
    		useOfflineMaps();
    		attachMap();
    		return true;
    	case R.id.switchToOnlineMap:
    		useOnlineMaps();
    		attachMap();
    		return true;
    	}
    	return super.onOptionsItemSelected(menuItem);
    }

    private boolean hasInternet() {
    	boolean ret = true;
    	ConnectivityManager manager =  (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    	NetworkInfo info = manager.getActiveNetworkInfo();
    	if (info == null || !info.isConnected())
    		ret = false;
    		
    	return ret;
    }

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	private void attachMap() {
		mMap.setupMap(mVenue);
		View view = mMap.getView();
		if (view != null) {
			LinearLayout layout = (LinearLayout) findViewById(R.id.mapsLinearLayout);
			layout.removeAllViews();
			layout.addView(view, new LayoutParams(
					LayoutParams.MATCH_PARENT,
					LayoutParams.MATCH_PARENT));
		}
	}
	
	private void useOfflineMaps() {
		if (mMap != null)
			mMap.disableLocation();

		mMap = null;
		mMap = new OSMMap(this, mOfflineMap);
		BoundingBoxE6 box = getBoundingBox(mVenue.getOfflineMapBounds());
		Log.d("SUSEConferences", "Setting bounding box: " + box);
		mMap.setBoundingBox(box);
		mUsingOfflineMap = true;
	}

	private void useOnlineMaps() {
		if (mMap != null)
			mMap.disableLocation();

		mMap = null;
		if (hasGoogleMaps())
			mMap = new GoogleMap(this);
		else
			mMap = new OSMMap(this, null);
		mUsingOfflineMap = false;
	}
	
	// The string is in the format of N,W,S,E
	private BoundingBoxE6 getBoundingBox(String boxCoords) {
		String[] strCoords = boxCoords.split(",");
		double north = Double.parseDouble(strCoords[0]);
		double west = Double.parseDouble(strCoords[1]);
		double south = Double.parseDouble(strCoords[2]);
		double east = Double.parseDouble(strCoords[3]);

		BoundingBoxE6 box = new BoundingBoxE6(north, east, south, west);
		return box;
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

    private class DownloadMapTask extends AsyncTask<String, Integer, Void> {
        protected void onProgressUpdate(Integer... progress) {
        	mDownloadPercent = progress[0];
        	invalidateOptionsMenu();
        }

		@Override
		protected Void doInBackground(String... params) {
			String mapUrl = params[0];
			String path = params[1];
			try {
	            URL url = new URL(mapUrl);
	            URLConnection connection = url.openConnection();
	            connection.connect();
	            int mapLength = connection.getContentLength();

	            InputStream input = new BufferedInputStream(url.openStream());
	            OutputStream output = new FileOutputStream(path);

	            byte data[] = new byte[1024];
	            long total = 0;
	            int count;
	            while ((count = input.read(data)) != -1) {
	                total += count;
	                publishProgress((int) (total * 100 / mapLength));
	                output.write(data, 0, count);
	            }

	            output.flush();
	            output.close();
	            input.close();
	        } catch (Exception e) {
	        	
	        }
	        return null;
		}
		
    	protected void onPostExecute(Void param) {
    		mDownloadPercent = -1;
    		mDownloadedOfflineMap = true;
    		invalidateOptionsMenu();
    		useOfflineMaps();
    		attachMap();
    	}
    }
}
