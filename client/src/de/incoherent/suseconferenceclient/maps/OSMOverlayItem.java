package de.incoherent.suseconferenceclient.maps;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.OverlayItem;

public class OSMOverlayItem extends OverlayItem {

	public OSMOverlayItem(String title, String description, String address, GeoPoint geoPoint) {
		super(title, description, geoPoint);
	}
}
