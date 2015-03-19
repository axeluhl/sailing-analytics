package com.sap.sailing.android.buoy.positioning.app.valueobjects;

public class MarkPing {
	private String type;
	private String longitude;
	private String lattitude;
	private int unixType;
	
	public MarkPing(String type, String longitude, String lattitude, int unixType){
		setType(type);
		setLongitude(longitude);
		setLattitude(lattitude);
		setUnixType(unixType);
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public String getLattitude() {
		return lattitude;
	}

	public void setLattitude(String lattitude) {
		this.lattitude = lattitude;
	}

	public int getUnixType() {
		return unixType;
	}

	public void setUnixType(int unixType) {
		this.unixType = unixType;
	}
}
