package de.incoherent.suseconferenceclient.maps;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.OverlayItem;

public class OSMOverlayItem extends OverlayItem {

	public OSMOverlayItem(String aTitle, String aDescription, GeoPoint aGeoPoint) {
		super(aTitle, aDescription, aGeoPoint);
	}

}
