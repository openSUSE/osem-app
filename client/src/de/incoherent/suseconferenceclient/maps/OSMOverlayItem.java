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

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.OverlayItem;

import android.graphics.Point;
import android.graphics.drawable.Drawable;

public class OSMOverlayItem extends OverlayItem {
	private String mAddress;

	public OSMOverlayItem(String title, String description, String address, GeoPoint geoPoint) {
		super(title, description, geoPoint);
		this.mAddress = address;
	}
	
	public String getAddress() {
		return mAddress;
	}

    public void showPopup(OSMMapPopup popup, MapView mapView){
        popup.open(this);
        mapView.getController().animateTo(getPoint());
    }

    
}
