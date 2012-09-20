package com.sap.sailing.gwt.ui.shared;

import java.io.Serializable;

public class PolarDiagram49DTO implements Serializable {
	/** generated ID for serialization */
	private static final long serialVersionUID = -7172465531558621138L;
	private Number[][] series = null;

	public void setNumberSeries(Number[][] series) {
		this.series = series;
	}

	public Number[][] getNumberSeries() {
		return series;
	}
}
