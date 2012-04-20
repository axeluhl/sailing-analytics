package com.sap.sailing.gwt.ui.shared;

import java.io.Serializable;

public class WindFieldDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private WindDTO[] matrix;
	
	public WindDTO[] getMatrix() {
		return matrix;
	}
	public void setMatrix(WindDTO[] matrix) {
		this.matrix = matrix;
	}
	
	

}
