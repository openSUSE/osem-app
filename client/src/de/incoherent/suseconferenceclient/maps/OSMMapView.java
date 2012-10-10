/**
 * 
 */
package de.incoherent.suseconferenceclient.maps;

import org.osmdroid.ResourceProxy;
import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.MapView;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ZoomButtonsController;
import android.widget.ZoomButtonsController.OnZoomListener;

/**
 * Extend MapView so we can set how many items to display
 * when the map is zoomed out.
 * 
 * @author Matt Barringer <mbarringer@suse.de>
 *
 */


public class OSMMapView extends MapView  {

	public OSMMapView(Context context) {
		super(context, 256);
	}
	public OSMMapView(Context context, int tileSizePixels,
			ResourceProxy resourceProxy, MapTileProviderBase aTileProvider) {
		super(context, tileSizePixels, resourceProxy, aTileProvider);
		
	}
}
