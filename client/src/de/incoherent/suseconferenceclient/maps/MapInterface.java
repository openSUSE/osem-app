package de.incoherent.suseconferenceclient.maps;

import de.incoherent.suseconferenceclient.models.Venue;
import android.view.View;

public interface MapInterface {

	public View getView();
	public void setupMap(Venue venue);
	public void enableLocation();
	public void disableLocation();
}
