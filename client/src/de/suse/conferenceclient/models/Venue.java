/**
 * 
 */
package de.suse.conferenceclient.models;

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
	private String mName;
	private String mAddress;
	private String mInfo;
	private List<MapPoint> mPoints;
	
	public Venue(String name, String address, String info) {
		this.mName = name;
		this.mAddress = address;
		this.mInfo = info;
		this.mPoints = new ArrayList<MapPoint>();
	}

	public List<MapPoint> getPoints() {
		return mPoints;
	}
	
	public void addPoint(MapPoint point) {
		mPoints.add(point);
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
}
