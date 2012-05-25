package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class WindFieldGenParamsDTO implements IsSerializable {
        
      
	private PositionDTO northWest;
	private PositionDTO southEast;
	
	private int xRes;
	private int yRes;
	
	
	public PositionDTO getNorthWest() {
		return northWest;
	}
	public void setNorthWest(PositionDTO northWest) {
		this.northWest = northWest;
	}
	public PositionDTO getSouthEast() {
		return southEast;
	}
	public void setSouthEast(PositionDTO southEast) {
		this.southEast = southEast;
	}
	public int getxRes() {
		return xRes;
	}
	public void setxRes(int xRes) {
		this.xRes = xRes;
	}
	public int getyRes() {
		return yRes;
	}
	public void setyRes(int yRes) {
		this.yRes = yRes;
	}

}
