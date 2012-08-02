package de.suse.conferenceclient.activities;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ScrollView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

import de.suse.conferenceclient.R;
import de.suse.conferenceclient.SUSEConferences;
import de.suse.conferenceclient.app.Database;
import de.suse.conferenceclient.app.MapOverlay;
import de.suse.conferenceclient.models.Venue;
import de.suse.conferenceclient.models.Venue.MapPoint;
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

		List<Overlay> overlays = mMapView.getOverlays();

		Drawable venueDrawable = MapOverlay.boundDrawable(this.getResources().getDrawable(R.drawable.venue_marker));
		Drawable foodDrawable = MapOverlay.boundDrawable(this.getResources().getDrawable(R.drawable.food_marker));
		Drawable drinkDrawable = MapOverlay.boundDrawable(this.getResources().getDrawable(R.drawable.drink_marker));
		Drawable elecDrawable = MapOverlay.boundDrawable(this.getResources().getDrawable(R.drawable.electronics_marker));
		mMapOverlays = new MapOverlay(VenueMapsActivity.this, venueDrawable);

		MapController controller =  mMapView.getController();
		for (MapPoint point : venue.getPoints()) {
			GeoPoint mapPoint = new GeoPoint(point.getLat(), point.getLon());
			OverlayItem overlay = new OverlayItem(mapPoint, point.getName(), point.getAddress());

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
			case MapPoint.TYPE_ELECTRONICS:
				overlay.setMarker(elecDrawable);
				break;
			}

			mOverlays.add(overlay);
			mMapOverlays.addOverlay(overlay);
			mMapOverlays.doPopulate();
		}

		overlays.add(mMapOverlays);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
	}

	@Override
	public void startActivity(Intent intent) {
		super.startActivity(intent);
		overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
	}

	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
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
			Log.d("SUSEConferences", "Only showing venue");
			mMapOverlays.clearOverlays();
			mMapOverlays.addOverlay(mConferenceOverlay);
			mMapOverlays.doPopulate();
			mMapView.invalidate();
		} else if (newZoom > MAGIC_ZOOM_LEVEL && !mShowingAll) {
			// Show everything
			mShowingAll = true;
			Log.d("SUSEConferences", "Showing everything");
			mMapOverlays.clearOverlays();
			mMapOverlays.addOverlays(mOverlays);
			mMapOverlays.doPopulate();
			mMapView.invalidate();
		}
	}

}
