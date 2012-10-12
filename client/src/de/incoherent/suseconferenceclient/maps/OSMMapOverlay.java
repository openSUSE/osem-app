package de.incoherent.suseconferenceclient.maps;

import java.util.ArrayList;
import java.util.List;

import org.osmdroid.ResourceProxy;
import org.osmdroid.api.IMapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.OverlayItem;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.Drawable;

public class OSMMapOverlay extends ItemizedOverlayWithFocus<OSMOverlayItem> {
	private ArrayList<OSMOverlayItem> mOverlays = null;
//	private Context mContext;
	
	public OSMMapOverlay(ArrayList<OSMOverlayItem> overlays,
						 ItemizedIconOverlay.OnItemGestureListener<OSMOverlayItem> listener,
						 ResourceProxy proxy) {
		super(overlays, listener, proxy);
		mOverlays = overlays;
	}
		
	@Override
	protected OSMOverlayItem createItem(int i) {
		return mOverlays.get(i);
	}
	
	public void addOverlay(OSMOverlayItem overlayItem) {
		mOverlays.add(overlayItem);
		populate();
	}
	
	public void addOverlays(ArrayList<OSMOverlayItem> overlays) {
		mOverlays.addAll(overlays);
		populate();
	}
	
	public void removeOverlay(OSMOverlayItem overlayItem) {
		mOverlays.remove(overlayItem);
		populate();
	}
	
	public void clearOverlays() {
		mOverlays.clear();
	}
	
	public void doPopulate() {
		populate();
	}
	
	@Override
	public int size() {
		if (mOverlays != null)
			return mOverlays.size();
		else
			return 0;
	}

	@Override
	public boolean onSnapToItem(int arg0, int arg1, Point arg2, IMapView arg3) {
		return false;
	}
}
