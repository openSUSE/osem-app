package de.incoherent.suseconferenceclient.maps;

import org.osmdroid.util.BoundingBoxE6;

import de.incoherent.suseconferenceclient.models.Venue;
import android.view.View;

public interface MapInterface {

	public View getView();
	public void setupMap(Venue venue);
	public void enableLocation();
	public void disableLocation();
	public void setBoundingBox(BoundingBoxE6 box);
}
