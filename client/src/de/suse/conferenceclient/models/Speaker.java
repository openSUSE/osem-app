/**
 * 
 */
package de.suse.conferenceclient.models;

import android.graphics.Bitmap;

/**
 * @author Matt Barringer <mbarringer@suse.de>
 *
 */
public class Speaker {
	private String mName, mCompany, mBio;
	private Bitmap mPhoto;
	public Speaker(String name, String company, String bio, Bitmap photo) {
		this.mName = name;
		this.mCompany = company;
		this.mBio = bio;
		this.mPhoto = photo;
	}
	
	public String getName() {
		return mName;
	}
	public void setName(String name) {
		mName = name;
	}
	public String getCompany() {
		return mCompany;
	}
	public void setCompany(String company) {
		mCompany = company;
	}
	public String getBio() {
		return mBio;
	}
	public void setBio(String bio) {
		mBio = bio;
	}
	public Bitmap getPhoto() {
		return mPhoto;
	}
	public void setPhoto(Bitmap photo) {
		mPhoto = photo;
	}
}
