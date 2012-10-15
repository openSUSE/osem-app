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

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.MapView.Projection;
import org.osmdroid.views.overlay.Overlay;

import android.content.Context;
import android.graphics.Canvas;
import android.view.MotionEvent;

public class OSMEventsOverlay extends Overlay {
    private OSMEventsInterface mReceiver;
    
    public OSMEventsOverlay(Context ctx, OSMEventsInterface receiver) {
    	super(ctx);
        mReceiver = receiver;
    }

    @Override 
    protected void draw(Canvas c, MapView osmv, boolean shadow) {
    }
    
    @Override
    public boolean onSingleTapUp(MotionEvent e, MapView mapView){
            Projection proj = mapView.getProjection();
            IGeoPoint p = proj.fromPixels(e.getX(), e.getY());
            return mReceiver.singleTapUpHelper(p);
    }
    
    @Override
    public boolean onLongPress(MotionEvent e, MapView mapView) {
    	return true;
    }

}
