/*******************************************************************************
 * Copyright (c) 2012 Matt Barringer <matt@incoherent.de>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Matt Barringer <matt@incoherent.de> - initial API and implementation
 ******************************************************************************/

package de.incoherent.suseconferenceclient.maps;

import java.util.ArrayList;

import org.osmdroid.ResourceProxy;
import org.osmdroid.api.IMapView;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;

import android.graphics.Point;

public class OSMMapOverlay extends ItemizedIconOverlay<OSMOverlayItem> {
	private ArrayList<OSMOverlayItem> mOverlays = null;
	private OSMMapPopup mPopup;
	
	public OSMMapOverlay(ArrayList<OSMOverlayItem> overlays,
						 ItemizedIconOverlay.OnItemGestureListener<OSMOverlayItem> listener,
						 ResourceProxy proxy,
						 MapView mapView) {
		super(overlays, listener, proxy);
		this.mOverlays = overlays;
		this.mPopup = new OSMMapPopup(mapView);
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
	
    public void showBubbleOnItem(final int index, final MapView mapView) {
        OSMOverlayItem item = (OSMOverlayItem)(getItem(index)); 
        if (item!= null){
        	item.showPopup(mPopup, mapView);
        }
    }
    
    public void closeBubble() {
    	mPopup.close();
    }

}
