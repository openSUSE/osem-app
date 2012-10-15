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

package de.incoherent.suseconferenceclient.models;

import java.util.ArrayList;
import java.util.List;

public class Venue {
	/*
	 * MapPoint represents the markers on the map
	 */
	public class MapPoint {
		public static final int TYPE_VENUE = 0;
		public static final int TYPE_FOOD = 1;
		public static final int TYPE_DRINK = 2;
		public static final int TYPE_ELECTRONICS = 3;
		public static final int TYPE_PARTY = 4;
		public static final int TYPE_NONE = 5;
		public static final int TYPE_HOTEL = 6;
		
		private int mType;
		private String mName;
		private String mAddress;
		private String mDescription;		
		private int mLat;
		private int mLon;
		
		public MapPoint(int type, int lat, int lon) {
			this.mType = type;
			this.mLat = lat;
			this.mLon = lon;
		}

		public int getType() {
			return mType;
		}

		public void setType(int type) {
			mType = type;
		}

		public String getName() {
			return mName;
		}

		public void setName(String name) {
			mName = name;
		}

		public String getAddress() {
			return mAddress;
		}

		public void setAddress(String address) {
			mAddress = address;
		}

		public String getDescription() {
			return mDescription;
		}

		public void setDescription(String description) {
			mDescription = description;
		}

		public int getLat() {
			return mLat;
		}

		public void setLat(int lat) {
			mLat = lat;
		}

		public int getLon() {
			return mLon;
		}

		public void setLon(int lon) {
			mLon = lon;
		}
	}
	
	/*
	 * MapPolygon represents polygons to be drawn over the map.  Currently
	 * not used.
	 */
	public class MapPolygon {
		private List<MapPoint> mPoints;
		private String mName = "";
		private String mLabel = "";
		private int mLineColor, mFillColor;
		
		public MapPolygon(String name, String label, int lineColor) {
			this(name, label, lineColor, -1);
		}
		
		public MapPolygon(String name, String label, int lineColor, int fillColor) {
			mPoints = new ArrayList<MapPoint>();
			this.mName = name;
			this.mLabel = label;
			this.mLineColor = lineColor;
			this.mFillColor = fillColor;
		}
		
		public void addPoint(MapPoint point) {
			mPoints.add(point);
		}
		
		public List<MapPoint> getPoints() {
			return mPoints;
		}
		
		public int getLineColor() {
			return mLineColor;
		}
		
		public int getFillColor() {
			return mFillColor;
		}
		
		public String getName() {
			return mName;
		}
		
		public String getLabel() {
			return mLabel;
		}
	}
	private String mName;
	private String mAddress;
	private String mInfo;
	private String mOfflineMapUrl;
	private String mOfflineMapBounds;
	private List<MapPoint> mPoints;
	private List<MapPolygon> mPolygons;

	public Venue(String name, String address, String info) {
		this.mName = name;
		this.mAddress = address;
		this.mInfo = info;
		this.mPoints = new ArrayList<MapPoint>();
		this.mPolygons = new ArrayList<MapPolygon>();
	}

	public List<MapPoint> getPoints() {
		return mPoints;
	}
	
	public void addPoint(MapPoint point) {
		mPoints.add(point);
	}
	public List<MapPolygon> getPolygons() {
		return mPolygons;
	}

	public void addPolygon(MapPolygon polygon) {
		mPolygons.add(polygon);
	}
	public String getName() {
		return mName;
	}

	public void setName(String name) {
		mName = name;
	}

	public String getAddress() {
		return mAddress;
	}

	public void setAddress(String address) {
		mAddress = address;
	}

	public String getInfo() {
		return mInfo;
	}

	public void setInfo(String info) {
		mInfo = info;
	}
	public String getOfflineMapUrl() {
		return mOfflineMapUrl;
	}

	public void setOfflineMapUrl(String offlineMapUrl) {
		mOfflineMapUrl = offlineMapUrl;
	}

	public String getOfflineMapBounds() {
		return mOfflineMapBounds;
	}

	public void setOfflineMapBounds(String offlineMapBounds) {
		mOfflineMapBounds = offlineMapBounds;
	}


}
