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

import android.graphics.drawable.Drawable;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;

public class GoogleMapOverlay extends ItemizedOverlay<GoogleMapOverlayItem> {
	private ArrayList<GoogleMapOverlayItem> mOverlays = new ArrayList<GoogleMapOverlayItem>();
	private GoogleMapPopup mPopup = null;
	private MapView mMapView;
	
	public GoogleMapOverlay(Drawable defaultMarker, MapView mapView) {
		super(boundCenterBottom(defaultMarker));
		this.mPopup = new GoogleMapPopup(mapView);
		this.mMapView = mapView;
	}
		
	@Override
	protected GoogleMapOverlayItem createItem(int i) {
		return mOverlays.get(i);
	}
	
	public static Drawable boundDrawable(Drawable d) {
		return boundCenterBottom(d);
	}
	
	public void addOverlay(GoogleMapOverlayItem overlayItem) {
		mOverlays.add(overlayItem);
	}
	
	public void addOverlays(ArrayList<GoogleMapOverlayItem> overlays) {
		mOverlays.addAll(overlays);
	}
	
	public void removeOverlay(GoogleMapOverlayItem overlayItem) {
		mOverlays.remove(overlayItem);
	}
	
	public void clearOverlays() {
		mOverlays.clear();
	}
	
	public void doPopulate() {
		populate();
		setLastFocusedIndex(-1);
	}
	
	@Override
	public int size() {
		return mOverlays.size();
	}
	
	@Override
	protected boolean onTap(int index) {
	  GoogleMapOverlayItem item = mOverlays.get(index);
	  item.showPopup(mPopup, mMapView);
	  return true;
	}
	
    public void closePopup() {
    	mPopup.close();
    }
}
