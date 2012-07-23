/**
 * 
 */
package de.suse.conferenceclient.models;

/**
 * @author Matt Barringer <mbarringer@suse.de>
 *
 */
public class Speaker {
	private String mGuid = "";
	private String mName = "";
	private String mCompany = "";
	private String mBiography = "";
	private String mPhotoGuid = "";
	
	public String getGuid() {
		return mGuid;
	}
	public void setGuid(String guid) {
		mGuid = guid;
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
	public String getBiography() {
		return mBiography;
	}
	public void setBiography(String biography) {
		mBiography = biography;
	}
	public String getPhotoGuid() {
		return mPhotoGuid;
	}
	public void setPhotoGuid(String photoGuid) {
		mPhotoGuid = photoGuid;
	}
}
