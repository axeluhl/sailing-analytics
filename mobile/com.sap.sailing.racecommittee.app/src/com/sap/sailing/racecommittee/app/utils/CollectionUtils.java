package com.sap.sailing.racecommittee.app.utils;

import java.util.ArrayList;

import com.sap.sailing.domain.common.impl.Util;

public class CollectionUtils {

	public static <T> ArrayList<T> newArrayList(Iterable<T> iterable) {
		ArrayList<T> list = new ArrayList<T>();
		Util.addAll(iterable, list);
		return list;
	}

}
