package de.suse.conferenceclient.activities;

import java.util.ArrayList;
import java.util.List;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

import de.suse.conferenceclient.R;
import de.suse.conferenceclient.SUSEConferences;
import de.suse.conferenceclient.app.Database;
import de.suse.conferenceclient.app.MapOverlay;
import de.suse.conferenceclient.app.MapPolygonOverlay;
import de.suse.conferenceclient.models.Venue;
import de.suse.conferenceclient.models.Venue.MapPoint;
import de.suse.conferenceclient.models.Venue.MapPolygon;
import de.suse.conferenceclient.views.AreaMapView;
import de.suse.conferenceclient.views.AreaMapView.AreaZoomListener;

public class VenueMapsActivity extends MapActivity implements AreaZoomListener {
	private AreaMapView mMapView;
	private OverlayItem mConferenceOverlay;
	private MapOverlay mMapOverlays;
	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();

	// The level at which the map only displays the marker for the conference
	// venue, and not food/drinks/etc.  The greater the number, the more
	// zoomed in it is.
	private static final int MAGIC_ZOOM_LEVEL = 14;
	private boolean mShowingAll = true;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		long venueId = getIntent().getLongExtra("venueId", -1);
		setContentView(R.layout.activity_info);
		Database db = SUSEConferences.getDatabase();
		Venue venue = db.getVenueInfo(venueId);

		mMapView = (AreaMapView) findViewById(R.id.mapView);
		mMapView.setBuiltInZoomControls(true);
		mMapView.setZoomListener(this);
		mMapView.setSatellite(true);
		List<Overlay> overlays = mMapView.getOverlays();

		
		Drawable venueDrawable = MapOverlay.boundDrawable(this.getResources().getDrawable(R.drawable.venue_marker));
		Drawable foodDrawable = MapOverlay.boundDrawable(this.getResources().getDrawable(R.drawable.food_marker));
		Drawable drinkDrawable = MapOverlay.boundDrawable(this.getResources().getDrawable(R.drawable.drink_marker));
		Drawable elecDrawable = MapOverlay.boundDrawable(this.getResources().getDrawable(R.drawable.electronics_marker));
		Drawable partyDrawable = MapOverlay.boundDrawable(this.getResources().getDrawable(R.drawable.party_marker));
		mMapOverlays = new MapOverlay(VenueMapsActivity.this, venueDrawable);

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
			MapPolygonOverlay newOverlay;
			newOverlay = new MapPolygonOverlay(pathPoints, polygon.getLineColor(), polygon.getFillColor());
			overlays.add(newOverlay);
		}
		overlays.add(mMapOverlays);
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	@Override
	public void onZoom(int oldZoom, int newZoom) {
		if (newZoom <= MAGIC_ZOOM_LEVEL && mShowingAll) {
			// Only show the conference venue
			mShowingAll = false;
			mMapOverlays.clearOverlays();
			mMapOverlays.addOverlay(mConferenceOverlay);
			mMapOverlays.doPopulate();
			mMapView.invalidate();
		} else if (newZoom > MAGIC_ZOOM_LEVEL && !mShowingAll) {
			// Show everything
			mShowingAll = true;
			mMapOverlays.clearOverlays();
			mMapOverlays.addOverlays(mOverlays);
			mMapOverlays.doPopulate();
			mMapView.invalidate();
		}
	}

}
