/**
 * 
 */
package de.suse.conferenceclient.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ZoomButtonsController;
import android.widget.ZoomButtonsController.OnZoomListener;

import com.google.android.maps.MapView;

/**
 * Extend MapView so we can set how many items to display
 * when the map is zoomed out.
 * 
 * @author Matt Barringer <mbarringer@suse.de>
 *
 */

public class AreaMapView extends MapView  implements OnZoomListener {
	public interface AreaZoomListener {
	    public void onZoom(int oldZoom, int newZoom);
	}
	
	private int mZoomLevel;
    private AreaZoomListener mListener;

	public AreaMapView(Context context, String apiKey) {
		super(context, apiKey);
	}
	
	public AreaMapView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public AreaMapView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
    
    public void setZoomLevel(int zoom) {
        this.mZoomLevel = zoom;
        this.getController().setZoom(zoom);
    }

    public void setZoomListener(AreaZoomListener listener) {
        this.mListener = listener;
        ZoomButtonsController controller = getZoomButtonsController();
        if (controller != null)
        	controller.setOnZoomListener(this);
    }
    
    public boolean onTouchEvent(MotionEvent event) {
        boolean result = super.onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_UP) {
            checkForZoomEvent();
        }
        
        return result;
    }
    
    private void checkForZoomEvent() {
        if (mListener != null) {
            int newZoomLevel = getZoomLevel();
            mListener.onZoom(mZoomLevel, newZoomLevel);
            mZoomLevel = newZoomLevel;
        }
    }
    
    public void onZoom(boolean zoomIn) {
        int oldZoomLevel = this.mZoomLevel;
        setZoomLevel(oldZoomLevel + (zoomIn ? 1 : -1));
        if (mListener != null)
            mListener.onZoom(oldZoomLevel, this.mZoomLevel);
    }

    @Override
	public void onVisibilityChanged(boolean visible) {
	}
}
