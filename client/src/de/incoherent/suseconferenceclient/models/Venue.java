/**
 * 
 */
package de.incoherent.suseconferenceclient.models;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Matt Barringer <mbarringer@suse.de>
 *
 */
public class Venue {
	public class MapPoint {
		public static final int TYPE_VENUE = 0;
		public static final int TYPE_FOOD = 1;
		public static final int TYPE_DRINK = 2;
		public static final int TYPE_ELECTRONICS = 3;
		public static final int TYPE_PARTY = 4;
		public static final int TYPE_NONE = 5;
		
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


}
