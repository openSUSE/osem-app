/**
 * 
 */
package de.incoherent.suseconferenceclient.maps;

import microsoft.mappoint.TileSystem;

import org.osmdroid.ResourceProxy;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.views.MapView;
import org.osmdroid.views.util.constants.MapViewConstants;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Scroller;
import android.widget.ZoomButtonsController;
import android.widget.ZoomButtonsController.OnZoomListener;

/**
 * Extend MapView so we can set how many items to display
 * when the map is zoomed out.
 * 
 * @author Matt Barringer <mbarringer@suse.de>
 *
 */

/**
 * 
 * Bounding box functionality taken from:
 * http://code.google.com/p/osmdroid/issues/attachmentText?id=209&aid=2090027000&name=BoundedMapView.java
 *
 */

public class OSMMapView extends MapView  {
    protected Rect mScrollableAreaLimit = null;
    protected BoundingBoxE6 box = null;

	public OSMMapView(Context context) {
		super(context, 256);
	}
	
	public OSMMapView(Context context, int tileSizePixels,
			ResourceProxy resourceProxy, MapTileProviderBase aTileProvider) {
		super(context, tileSizePixels, resourceProxy, aTileProvider);

	}

    public void setScrollableAreaLimit(BoundingBoxE6 box) {
        final int worldSize_2 = TileSystem.MapSize(MapViewConstants.MAXIMUM_ZOOMLEVEL) / 2;
        // Clear scrollable area limit if null passed.
        if (box == null) {
            mScrollableAreaLimit = null;
            return;
        }

        // Get NW/upper-left
        final Point upperLeft = TileSystem.LatLongToPixelXY(box.getLatNorthE6() / 1E6,
                                                            box.getLonWestE6() / 1E6,
                                                            MapViewConstants.MAXIMUM_ZOOMLEVEL,
                                                            null);
        upperLeft.offset(-worldSize_2, -worldSize_2);

        // Get SE/lower-right
        final Point lowerRight = TileSystem.LatLongToPixelXY(box.getLatSouthE6() / 1E6,
                                                             box.getLonEastE6() / 1E6,
                                                             MapViewConstants.MAXIMUM_ZOOMLEVEL,
                                                             null);
        lowerRight.offset(-worldSize_2, -worldSize_2);
        mScrollableAreaLimit = new Rect(upperLeft.x,
                                        upperLeft.y,
                                        lowerRight.x,
                                        lowerRight.y);
    }

    @Override
    public void scrollTo(int x, int y) {
        final int worldSize_2 = TileSystem.MapSize(this.getZoomLevel(true)) / 2;

        while (x < -worldSize_2) {
            x += worldSize_2 * 2;
        }
        while (x > worldSize_2) {
            x -= worldSize_2 * 2;
        }
        if (y < -worldSize_2) {
            y = -worldSize_2;
        }
        if (y > worldSize_2) {
            y = worldSize_2;
        }

        if (mScrollableAreaLimit != null) {
            final int zoomDiff = MapViewConstants.MAXIMUM_ZOOMLEVEL - getZoomLevel();
            final int minX = mScrollableAreaLimit.left >> zoomDiff;
            final int minY = mScrollableAreaLimit.top >> zoomDiff;
            final int maxX = mScrollableAreaLimit.right >> zoomDiff;
            final int maxY = mScrollableAreaLimit.bottom >> zoomDiff;
            if (x < minX)
                x = minX;
            else if (x > maxX)
                x = maxX;
            if (y < minY)
                y = minY;
            else if (y > maxY)
                y = maxY;
        }
        super.scrollTo(x, y);

        // do callback on listener
        if (mListener != null) {
            final ScrollEvent event = new ScrollEvent(this, x, y);
            mListener.onScroll(event);
        }
    }

    @Override
    public void computeScroll() {
        final Scroller mScroller = getScroller();
        final int mZoomLevel = getZoomLevel(false);

        if (mScroller.computeScrollOffset()) {
            if (mScroller.isFinished()) {
                /**
                 * Need to jump through some accessibility hoops here Silly
                 * enough the only thing MapController.setZoom does is call
                 * MapView.setZoomLevel(zoomlevel). But noooo .. if I try that
                 * directly setZoomLevel needs to be set to "protected".
                 * Explanation can be found at
                 * http://docs.oracle.com/javase/tutorial
                 * /java/javaOO/accesscontrol.html
                 * 
                 * This also suggests that if the subclass is made to be part of
                 * the package, this can be replaced by a simple call to
                 * setZoomLevel(mZoomLevel)
                 */
                // This will facilitate snapping-to any Snappable points.
                getController().setZoom(mZoomLevel);
            } else {
                /* correction for double tap */
                int targetZoomLevel = getZoomLevel();
                if (targetZoomLevel == mZoomLevel)
                    scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            }
            postInvalidate(); // Keep on drawing until the animation has
            // finished.
        }
    }

}
