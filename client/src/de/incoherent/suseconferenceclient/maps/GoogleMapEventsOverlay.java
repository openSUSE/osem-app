/**
 * 
 */
package de.incoherent.suseconferenceclient.maps;

import android.graphics.Canvas;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

/**
 * This overlay is used to grab any clicks that are outside of the 
 * popups, so popups can be closed by clicking on the map.
 * 
 * @author Matt Barringer <mbarringer@suse.de>
 *
 */
public class GoogleMapEventsOverlay extends Overlay {
    private GoogleMapEventsInterface mReceiver;
    
    public GoogleMapEventsOverlay(GoogleMapEventsInterface receiver) {
    	super();
        mReceiver = receiver;
    }

    @Override 
    public void draw(Canvas c, MapView osmv, boolean shadow) {
    }
    
    @Override
    public boolean onTap(GeoPoint p, MapView mapView){
            return mReceiver.singleTapUpHelper(p);
    }
}
