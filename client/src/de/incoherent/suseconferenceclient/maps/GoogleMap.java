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

package de.incoherent.suseconferenceclient.maps;

import java.util.ArrayList;
import java.util.List;

import org.osmdroid.util.BoundingBoxE6;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

import de.incoherent.suseconferenceclient.Config;
import de.incoherent.suseconferenceclient.R;
import de.incoherent.suseconferenceclient.activities.MapsActivity;
import de.incoherent.suseconferenceclient.maps.GoogleMapView.AreaZoomListener;
import de.incoherent.suseconferenceclient.models.Venue;
import de.incoherent.suseconferenceclient.models.Venue.MapPoint;
import de.incoherent.suseconferenceclient.models.Venue.MapPolygon;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

public class GoogleMap implements AreaZoomListener, MapInterface, GoogleMapEventsInterface {
	private GoogleMapView mMapView = null;
	private Context mContext;
	private boolean mShowingAll = true;
	private GoogleMapOverlayItem mConferenceOverlay = null;
	private GoogleMapOverlay mMapOverlays;
	private ArrayList<GoogleMapOverlayItem> mOverlays;
	private MyLocationOverlay mLocationOverlay;

	public GoogleMap(Context context)  {
		mContext = context;
		mOverlays = new ArrayList<GoogleMapOverlayItem>();
	}
	
	@Override
	public View getView() {
		return mMapView;
	}
	
	@Override
	public void setupMap(Venue venue) {
		if (mMapView != null)
			return;
		
		mMapView = new GoogleMapView(mContext, Config.MAPS_KEY);
		mMapView.setBuiltInZoomControls(true);
		mMapView.setZoomListener(this);
		mMapView.setSatellite(false);
		mMapView.setClickable(true);

		List<Overlay> overlays = mMapView.getOverlays();
		GoogleMapEventsOverlay eventOverlay = new GoogleMapEventsOverlay(this);
		overlays.add(eventOverlay);
		
		Drawable venueDrawable = GoogleMapOverlay.boundDrawable(mContext.getResources().getDrawable(R.drawable.venue_marker));
		Drawable foodDrawable = GoogleMapOverlay.boundDrawable(mContext.getResources().getDrawable(R.drawable.food_marker));
		Drawable drinkDrawable = GoogleMapOverlay.boundDrawable(mContext.getResources().getDrawable(R.drawable.drink_marker));
		Drawable elecDrawable = GoogleMapOverlay.boundDrawable(mContext.getResources().getDrawable(R.drawable.electronics_marker));
		Drawable partyDrawable = GoogleMapOverlay.boundDrawable(mContext.getResources().getDrawable(R.drawable.party_marker));
		mMapOverlays = new GoogleMapOverlay(venueDrawable, mMapView);

		if (venue == null)
			return;
		
		MapController controller =  mMapView.getController();
		for (MapPoint point : venue.getPoints()) {
			GeoPoint mapPoint = new GeoPoint(point.getLat(), point.getLon());
			GoogleMapOverlayItem overlay = null;
			overlay = new GoogleMapOverlayItem(point.getName(), point.getDescription(), point.getAddress(), mapPoint);
			
			switch (point.getType()) {
			case MapPoint.TYPE_VENUE:
				if (mConferenceOverlay == null)
					mConferenceOverlay = overlay;
				controller.setCenter(mapPoint);
				controller.setZoom(18);
				break;
			case MapPoint.TYPE_FOOD:
				overlay.setMarker(foodDrawable);
				break;
			case MapPoint.TYPE_DRINK:
				overlay.setMarker(drinkDrawable);
				break;
			case MapPoint.TYPE_PARTY:
				overlay.setMarker(partyDrawable);
				break;
			case MapPoint.TYPE_ELECTRONICS:
				overlay.setMarker(elecDrawable);
				break;
			}

			mOverlays.add(overlay);
			mMapOverlays.addOverlay(overlay);
			mMapOverlays.doPopulate();
		}

		for (MapPolygon polygon : venue.getPolygons()) {
			List<MapPoint> points = polygon.getPoints();
			GeoPoint[] pathPoints = new GeoPoint[points.size()];
			for (int i = 0; i < points.size(); i++) {
				MapPoint point = points.get(i);
				pathPoints[i] = new GeoPoint(point.getLat(), point.getLon());
			}
			GoogleMapPolygonOverlay newOverlay;
			newOverlay = new GoogleMapPolygonOverlay(pathPoints, polygon.getLineColor(), polygon.getFillColor());
			overlays.add(newOverlay);
		}
		overlays.add(mMapOverlays);
		mLocationOverlay = new MyLocationOverlay(mContext, mMapView);
		mLocationOverlay.enableMyLocation();
		mLocationOverlay.enableCompass();
		overlays.add(mLocationOverlay);
		mMapView.postInvalidate();
	}

	@Override
	public void onZoom(int oldZoom, int newZoom) {
		if (newZoom <= Config.MAGIC_ZOOM_LEVEL && mShowingAll) {
			// Only show the conference venue
			mShowingAll = false;
			mMapOverlays.clearOverlays();
			mMapOverlays.addOverlay(mConferenceOverlay);
			mMapOverlays.doPopulate();
			mMapView.invalidate();
		} else if (newZoom > Config.MAGIC_ZOOM_LEVEL && !mShowingAll) {
			// Show everything
			mShowingAll = true;
			mMapOverlays.clearOverlays();
			mMapOverlays.addOverlays(mOverlays);
			mMapOverlays.doPopulate();
			mMapView.invalidate();
		}
		
	}

	@Override
	public void enableLocation() {
		Log.d("SUSEConferences", "GoogleMap: enableLocation");
		mLocationOverlay.enableMyLocation();
		mLocationOverlay.enableCompass();
	}

	@Override
	public void disableLocation() {
		Log.d("SUSEConferences", "GoogleMap: disableLocation");
		mLocationOverlay.disableMyLocation();
		mLocationOverlay.disableCompass();
	}

	@Override
	public void setBoundingBox(BoundingBoxE6 box) {
		// Not useful ATM
	}

	@Override
	public boolean singleTapUpHelper(GeoPoint p) {
		mMapOverlays.closePopup();
		return false;
	}
}
