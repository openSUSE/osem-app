package de.suse.conferenceclient.models;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.graphics.Bitmap;
import android.util.Log;

public class SocialItem implements Comparable<SocialItem> {
	public static final int TWITTER = 0;
	public static final int GOOGLE = 1;
	
	private int mType;
	private String mUserName;
	private Bitmap mUserImage;
	private Bitmap mTypeIcon;
	private String mMessage;
	private String mDatestamp;
	private String mLink;
	private String mTitle;
	private Date mDate;
	
	public SocialItem() {}
	
	public SocialItem (int type, String username, String message, Date date, String datestamp, Bitmap image, Bitmap typeIcon) {
		this.mType = type;
		this.mUserName = username;
		this.mTypeIcon = typeIcon;
		this.mMessage = message;
		this.mDate = date;
		this.mDatestamp = datestamp;
		this.mUserImage = image;
		this.mLink = "";
		Log.d("SUSEConferences", "Social type: " + type + " date: " + date);
	}
	
	public Bitmap getTypeIcon() {
		return mTypeIcon;
	}
	
	public int getType() {
		return mType;
	}

	public Date getDate() {
		return mDate;
	}
	public String getUserName() {
		return mUserName;
	}

	public void setUserName(String userName) {
		mUserName = userName;
	}

	public Bitmap getUserImage() {
		return mUserImage;
	}

	public void setUserImage(Bitmap userImage) {
		mUserImage = userImage;
	}

	public String getMessage() {
		return mMessage;
	}

	public void setMessage(String message) {
		mMessage = message;
	}

	public String getDatestamp() {
		return mDatestamp;
	}

	public void setDatestamp(String datestamp) {
		mDatestamp = datestamp;
	}

	public String getLink() {
		return mLink;
	}

	public void setLink(String link) {
		mLink = link;
	}

	public String getTitle() {
		return mTitle;
	}

	public void setTitle(String title) {
		mTitle = title;
	}

	@Override
	public int compareTo(SocialItem another) {
		Log.d("SUSEConferences", "compareTo: " + mDate + " vs " + another.getDate());
		return mDate.compareTo(another.getDate());
	}
}
