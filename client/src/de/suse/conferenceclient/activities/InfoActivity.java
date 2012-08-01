package de.suse.conferenceclient.activities;

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

public class InfoActivity extends MapActivity {
	private MapView mMapView;
	private GeoPoint mConferencePoint;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		long venueId = getIntent().getLongExtra("venueId", -1);
		setContentView(R.layout.activity_info);
		Database db = SUSEConferences.getDatabase();
		Venue venue = db.getVenueInfo(venueId);
		
		mMapView = (MapView) findViewById(R.id.mapView);
		List<Overlay> overlays = mMapView.getOverlays();
		Drawable venueDrawable = this.getResources().getDrawable(R.drawable.venue_marker);
		Drawable foodDrawable = this.getResources().getDrawable(R.drawable.food_marker);
		foodDrawable.setBounds(0, 0, foodDrawable.getIntrinsicWidth(), foodDrawable.getIntrinsicHeight());

		Drawable drinkDrawable = this.getResources().getDrawable(R.drawable.drink_marker);
		drinkDrawable.setBounds(0, 0, drinkDrawable.getIntrinsicWidth(), drinkDrawable.getIntrinsicHeight());

		Drawable elecDrawable = this.getResources().getDrawable(R.drawable.electronics_marker);
		elecDrawable.setBounds(0, 0, elecDrawable.getIntrinsicWidth(), elecDrawable.getIntrinsicHeight());

		MapOverlay mapOverlays = new MapOverlay(InfoActivity.this, venueDrawable);
		
		MapController controller =  mMapView.getController();
		for (MapPoint point : venue.getPoints()) {
			Log.d("SUSEConferences", "Adding point");
			 GeoPoint mapPoint = new GeoPoint(point.getLat(), point.getLon());
			 OverlayItem overlay = new OverlayItem(mapPoint, venue.getName(), venue.getAddress());

			 switch (point.getType()) {
			 case MapPoint.TYPE_VENUE:
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
			 
			 mapOverlays.addOverlay(overlay);
		}
		
		overlays.add(mapOverlays);
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

}
