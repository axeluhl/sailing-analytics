package com.sap.sailing.domain.base;


public interface Nationality extends Named, WithImage {
	String getThreeLetterAcronym();
}
