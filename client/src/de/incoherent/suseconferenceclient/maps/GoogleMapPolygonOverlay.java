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

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

public class GoogleMapPolygonOverlay extends Overlay {
	private GeoPoint[] mPointList;
	private int mLineColor;
	private int mFillColor = 0;
	private Paint mLinePainter;
	private Paint mFillPainter = null;

	private Paint mLabelPainter;
	
	public GoogleMapPolygonOverlay(GeoPoint[] pathPoints, int lineColor, int fillColor) {
		this.mPointList = pathPoints;
		this.mLineColor = lineColor;
		this.mFillColor = fillColor;

		mLinePainter = new Paint();
		mLinePainter.setColor(mLineColor);
		mLinePainter.setStrokeWidth(2);
		mLinePainter.setStyle(Paint.Style.STROKE);
		mLinePainter.setAntiAlias(true);
		
		mLabelPainter = new Paint();
		mLabelPainter.setTextSize(10);
		mLabelPainter.setColor(Color.BLACK);
		mLabelPainter.setAntiAlias(true);

		mFillPainter = new Paint();
		mFillPainter.setColor(mFillColor);
		mFillPainter.setAntiAlias(true);
		mFillPainter.setStyle(Paint.Style.FILL);
	}
	

	@Override
	public boolean draw (Canvas canvas, MapView mapView, boolean shadow, long when) {
        Projection projection = mapView.getProjection();
        Path path = new Path();
    	Point labelP = null;

        if (!shadow) {
        	Point p = new Point();
        	projection.toPixels(mPointList[0], p);
        	
        	labelP = new Point();
        	labelP.set(p.x +10, p.y + 10);
        	path.moveTo(p.x, p.y);
        	for (int i = 1; i < mPointList.length; i++) {
        		projection.toPixels(mPointList[i], p);
        		path.lineTo(p.x, p.y);
        	}
        	path.close();
        }
        
        if (mFillPainter != null)
        	canvas.drawPath(path, mFillPainter);
        canvas.drawPath(path, mLinePainter);
        mLabelPainter.setTextSize(mapView.getZoomLevel() - 10 );
        if (labelP != null)
        	canvas.drawText("Some label", labelP.x, labelP.y, mLabelPainter);
        return super.draw(canvas, mapView, shadow, when);
	}
}
