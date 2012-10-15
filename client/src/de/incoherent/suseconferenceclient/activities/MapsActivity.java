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
import com.google.android.maps.MapActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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

public class MapsActivity extends SherlockMapActivity {
	private File mOfflineMap = null;
	private Venue mVenue;
	// Is the map available for downloading?
	private boolean mHasOfflineMap = false;
	// Do we have it on disk?
	private boolean mDownloadedOfflineMap = false;
	private boolean mUsingOfflineMap = false;
	private String mOfflineMapUrl;
	private String mOfflineMapFilename;
	private ProgressDialog mDownloaderProgressDialog;
	private MapInterface mMap = null;
	// Keep hold of instantiated map classes,
	// otherwise the app will crash when switching
	// due to MapActivity only allowing one Google MapView
	// at a time
	private GoogleMap mGoogleMap = null;
	private OSMMap mOSMMap = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		long venueId = getIntent().getLongExtra("venueId", -1);
		setContentView(R.layout.activity_info);
		Database db = SUSEConferences.getDatabase();
		mVenue = db.getVenueInfo(venueId);
		if (mVenue == null) {
			Log.d("SUSEConferences", "mVenue is null!  I tried " + venueId);
			return;
		}
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
    		menu.add(Menu.NONE, R.id.downloadMap, Menu.NONE, getString(R.string.downloadMap))
    		.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
    	} else if (mUsingOfflineMap) {
			menu.add(Menu.NONE, R.id.switchToOnlineMap, Menu.NONE, getString(R.string.switchToOnlineMap))
			.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
    	} else if (!mUsingOfflineMap && mDownloadedOfflineMap) {
			menu.add(Menu.NONE, R.id.switchToOfflineMap, Menu.NONE, getString(R.string.switchToOfflineMap))
			.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
    	}
    	
    	if (mDownloadedOfflineMap) {
			menu.add(Menu.NONE, R.id.deleteMap, Menu.NONE, getString(R.string.deleteMap))
			.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
    	}
    	
        return super.onPrepareOptionsMenu(menu);
    }
    
    
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
    	switch (menuItem.getItemId()) {
    	case R.id.downloadMap:
    		final DownloadMapTask downloader = new DownloadMapTask();
    		mDownloaderProgressDialog = new ProgressDialog(this);
    		mDownloaderProgressDialog.setTitle("Downloading Map");
    		mDownloaderProgressDialog.setMessage("Preparing downloader...");
    		mDownloaderProgressDialog.setCanceledOnTouchOutside(false);
    		mDownloaderProgressDialog.setCancelable(false);
    		mDownloaderProgressDialog.setMax(100);
    		mDownloaderProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() 
    	    {
    	        public void onClick(DialogInterface dialog, int which) 
    	        {
    	        	Log.d("SUSEConferences", "Cancelling map download");
    	            downloader.cancel(true);
    	    		mDownloadedOfflineMap = false;
    	            return;
    	        }
    	    });
    		downloader.execute(mOfflineMapUrl, mOfflineMap.getAbsolutePath());
    		mDownloaderProgressDialog.show();
            return true;
    	case R.id.switchToOfflineMap:
    		useOfflineMaps();
    		attachMap();
    		return true;
    	case R.id.switchToOnlineMap:
    		useOnlineMaps();
    		attachMap();
    		return true;
    	case R.id.deleteMap:
    		if (mOfflineMap != null) {
    			AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	        builder.setMessage("Are you sure you want to delete this map?")
    	               .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
    	                   public void onClick(DialogInterface dialog, int id) {
    	                	   mOfflineMap.delete();
    	                	   mDownloadedOfflineMap = false;
    	                	   useOnlineMaps();
    	                	   attachMap();
    	                   }
    	               })
    	               .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
    	                   public void onClick(DialogInterface dialog, int id) {
    	                   }
    	               });
    	        builder.create();
    	        builder.show();
    		}
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
		LinearLayout layout = (LinearLayout) findViewById(R.id.mapsLinearLayout);
		layout.removeAllViews();

		mMap.setupMap(mVenue);
		mMap.enableLocation();
		View view = mMap.getView();
		if (view != null) {
			layout.addView(view, new LayoutParams(
					LayoutParams.MATCH_PARENT,
					LayoutParams.MATCH_PARENT));
		}
	}
	
	private void useOfflineMaps() {
		if (mMap != null)
			mMap.disableLocation();

		mMap = null;
		if (mOSMMap != null) {
			mMap = mOSMMap;
		} else {
			mMap = mOSMMap = new OSMMap(this, mOfflineMap);
			BoundingBoxE6 box = getBoundingBox(mVenue.getOfflineMapBounds());
			mMap.setBoundingBox(box);
		}
		mUsingOfflineMap = true;
	}

	private void useOnlineMaps() {
		if (mMap != null)
			mMap.disableLocation();

		mMap = null;
		if (mGoogleMap != null)
			mMap = mGoogleMap;
		else
			mMap = mGoogleMap = new GoogleMap(this);
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
	
    private class DownloadMapTask extends AsyncTask<String, Integer, Void> {
        protected void onProgressUpdate(Integer... progress) {
        	mDownloaderProgressDialog.setProgress(progress[0]);
    		mDownloaderProgressDialog.setMessage("Downloaded %" + progress[0]);
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
	            while ((count = input.read(data)) != -1 && !isCancelled()) {
	                total += count;
	                publishProgress((int) (total * 100 / mapLength));
	                output.write(data, 0, count);
	            }

	            output.flush();
	            output.close();
	            input.close();

	            if (isCancelled()) {
	            	File file = new File(path);
	            	file.delete();
	            }
	            
	        } catch (Exception e) {
	        	
	        }
	        return null;
		}
		
    	protected void onPostExecute(Void param) {
    		mDownloadedOfflineMap = true;
    		mDownloaderProgressDialog.dismiss();
    		useOfflineMaps();
    		attachMap();
    	}
    }
}
