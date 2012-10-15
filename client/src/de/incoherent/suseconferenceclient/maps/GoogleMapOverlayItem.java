/**
 * 
 */
package de.incoherent.suseconferenceclient.maps;

import android.graphics.Point;
import android.graphics.drawable.Drawable;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

/**
 * @author Matt Barringer <mbarringer@suse.de>
 *
 */
public class GoogleMapOverlayItem extends OverlayItem {
	private String mAddress;

	public GoogleMapOverlayItem(String title, String description, String address, GeoPoint geoPoint) {
		super(geoPoint, title, description);
		this.mAddress = address;
	}
	
	public String getAddress() {
		return mAddress;
	}
    public void showPopup(GoogleMapPopup popup, MapView mapView){
        popup.open(this);
        mapView.getController().animateTo(getPoint());
    }

}
