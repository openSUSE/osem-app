/**
 * 
 */
package de.suse.conferenceclient.models;

/**
 * @author Matt Barringer <mbarringer@suse.de>
 *
 */

public class Conference {
	private String mGuid = "";
	private long mSqlId = -1;
	private String mName = "";
	private String mDescription = "";
	private int mYear = 2012;
	private String mDateRange = "";
	private String mUrl = "";
	private String mSocialTag = "";
	
	public Conference() {
	}

	public String getGuid() {
		return mGuid;
	}

	public void setGuid(String guid) {
		this.mGuid = guid;
	}

	public long getSqlId() {
		return mSqlId;
	}

	public void setSqlId(long sqlId) {
		this.mSqlId = sqlId;
	}

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		this.mName = name;
	}

	public String getDescription() {
		return mDescription;
	}

	public void setDescription(String description) {
		this.mDescription = description;
	}

	public int getYear() {
		return mYear;
	}

	public void setYear(int year) {
		this.mYear = year;
	}

	public String getDateRange() {
		return mDateRange;
	}

	public void setDateRange(String dateRange) {
		this.mDateRange = dateRange;
	}
	
	public String getUrl() {
		return mUrl;
	}
	
	public void setUrl(String url) {
		this.mUrl = url;
	}
	public String getSocialTag() {
		return mSocialTag;
	}

	public void setSocialTag(String socialTag) {
		mSocialTag = socialTag;
	}

}
