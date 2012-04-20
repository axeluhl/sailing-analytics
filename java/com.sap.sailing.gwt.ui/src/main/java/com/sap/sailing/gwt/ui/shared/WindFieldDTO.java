package com.sap.sailing.gwt.ui.shared;

import java.io.Serializable;
import java.util.List;

public class WindFieldDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private List<WindDTO> matrix;
	
	public List<WindDTO> getMatrix() {
		return matrix;
	}
	public void setMatrix(List<WindDTO> matrix) {
		this.matrix = matrix;
	}
	
	

}
