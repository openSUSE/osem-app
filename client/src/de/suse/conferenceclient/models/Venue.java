/**
 * 
 */
package de.suse.conferenceclient.models;

/**
 * @author Matt Barringer <mbarringer@suse.de>
 *
 */
public class Venue {
	private String mName = "";
	private String mGuid = "";
	private String mAddress = "";
	
	public String getName() {
		return mName;
	}
	public void setName(String name) {
		this.mName = name;
	}
	public String getGuid() {
		return mGuid;
	}
	public void setGuid(String guid) {
		this.mGuid = guid;
	}
	public String getAddress() {
		return mAddress;
	}
	public void setAddress(String address) {
		this.mAddress = address;
	}
	
}
