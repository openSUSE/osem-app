package de.incoherent.suseconferenceclient.maps;

import java.util.ArrayList;
import java.util.List;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

import de.incoherent.suseconferenceclient.Config;
import de.incoherent.suseconferenceclient.R;
import de.incoherent.suseconferenceclient.activities.VenueMapsActivity;
import de.incoherent.suseconferenceclient.maps.GoogleMapView.AreaZoomListener;
import de.incoherent.suseconferenceclient.models.Venue;
import de.incoherent.suseconferenceclient.models.Venue.MapPoint;
import de.incoherent.suseconferenceclient.models.Venue.MapPolygon;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.LinearLayout;

public class GoogleMap implements AreaZoomListener, MapInterface {
	private GoogleMapView mMapView = null;
	private Context mContext;
	private boolean mShowingAll = true;
	private OverlayItem mConferenceOverlay;
	private GoogleMapOverlay mMapOverlays;
	private ArrayList<OverlayItem> mOverlays;
	private MyLocationOverlay mLocationOverlay;

	public GoogleMap(Context context)  {
		mContext = context;
		mOverlays = new ArrayList<OverlayItem>();
	}
	
	@Override
	public View getView() {
		return mMapView;
	}
	
	@Override
	public void setupMap(Venue venue) {
		mMapView = new GoogleMapView(mContext, Config.MAPS_KEY);
		mMapView.setBuiltInZoomControls(true);
		mMapView.setZoomListener(this);
		mMapView.setSatellite(false);
		mMapView.setClickable(true);

		List<Overlay> overlays = mMapView.getOverlays();
		
		Drawable venueDrawable = GoogleMapOverlay.boundDrawable(mContext.getResources().getDrawable(R.drawable.venue_marker));
		Drawable foodDrawable = GoogleMapOverlay.boundDrawable(mContext.getResources().getDrawable(R.drawable.food_marker));
		Drawable drinkDrawable = GoogleMapOverlay.boundDrawable(mContext.getResources().getDrawable(R.drawable.drink_marker));
		Drawable elecDrawable = GoogleMapOverlay.boundDrawable(mContext.getResources().getDrawable(R.drawable.electronics_marker));
		Drawable partyDrawable = GoogleMapOverlay.boundDrawable(mContext.getResources().getDrawable(R.drawable.party_marker));
		mMapOverlays = new GoogleMapOverlay(mContext, venueDrawable);

		if (venue == null)
			return;
		
		MapController controller =  mMapView.getController();
		for (MapPoint point : venue.getPoints()) {
			GeoPoint mapPoint = new GeoPoint(point.getLat(), point.getLon());
			OverlayItem overlay = null;
			if (point.getType() == MapPoint.TYPE_VENUE)
				 overlay = new OverlayItem(mapPoint, point.getName(), point.getAddress());
			else
				overlay = new OverlayItem(mapPoint, point.getName(), point.getDescription());
			
			switch (point.getType()) {
			case MapPoint.TYPE_VENUE:
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
		// TODO Auto-generated method stub
		mLocationOverlay.enableMyLocation();
	}

	@Override
	public void disableLocation() {
		mLocationOverlay.disableMyLocation();
	}

}
