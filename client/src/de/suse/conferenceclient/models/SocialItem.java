package de.suse.conferenceclient.models;

import android.graphics.Bitmap;

public class SocialItem {
	private String mUserName;
	private Bitmap mUserImage;
	private String mMessage;
	private String mDatestamp;
	
	public SocialItem (String username, String message, String datestamp, Bitmap image) {
		this.mUserName = username;
		this.mMessage = message;
		this.mDatestamp = datestamp;
		this.mUserImage = image;
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
}
