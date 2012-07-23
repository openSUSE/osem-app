/**
 * 
 */
package de.suse.conferenceclient.models;

/**
 * @author Matt Barringer <mbarringer@suse.de>
 *
 */
public class Room {
	private String mName = "";
	private String mGuid = "";
	private String mDescription = "";
	
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
	public String getDescription() {
		return mDescription;
	}
	public void setDescription(String description) {
		this.mDescription = description;
	}

}
