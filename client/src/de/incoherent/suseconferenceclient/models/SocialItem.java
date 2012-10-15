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

import java.util.Date;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class SocialItem implements Comparable<SocialItem>, Parcelable {
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

    public int describeContents() {
        return 0;
    }

    /*
     * 	private int mType;
	private String mUserName;
	private Bitmap mUserImage;
	private Bitmap mTypeIcon;
	private String mMessage;
	private String mDatestamp;
	private String mLink;
	private String mTitle;
	private Date mDate;
(non-Javadoc)
     * @see android.os.Parcelable#writeToParcel(android.os.Parcel, int)
     */
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.mType);
        out.writeString(this.mUserName);
        out.writeString(this.mMessage);
        out.writeString(this.mDatestamp);
        out.writeString(this.mLink);
        out.writeString(this.mTitle);
        mUserImage.writeToParcel(out, flags);
        mTypeIcon.writeToParcel(out, flags);
        out.writeLong(this.mDate.getTime());
    }

    public static final Parcelable.Creator<SocialItem> CREATOR
            = new Parcelable.Creator<SocialItem>() {
        public SocialItem createFromParcel(Parcel in) {
            return new SocialItem(in);
        }

        public SocialItem[] newArray(int size) {
            return new SocialItem[size];
        }
    };
    
    private SocialItem(Parcel in) {
        this.mType = in.readInt();
        this.mUserName = in.readString();
        this.mMessage = in.readString();
        this.mDatestamp = in.readString();
        this.mLink = in.readString();
        this.mTitle = in.readString();
        this.mUserImage = Bitmap.CREATOR.createFromParcel(in);
        this.mTypeIcon = Bitmap.CREATOR.createFromParcel(in);
        this.mDate = new Date(in.readLong());
    }

}
