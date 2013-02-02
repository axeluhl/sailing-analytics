package com.sap.sailing.racecommittee.app.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class CollectionUtils {

	public static <T> ArrayList<T> newArrayList(Iterable<T> iterable) {
		ArrayList<T> list = new ArrayList<T>();
		addAll(list, iterable.iterator());
		return list;
	}
	
	public static <T> void addAll(Collection<T> collection, Iterator<T> iterator) {
	    while (iterator.hasNext()) {
	        collection.add(iterator.next());
	    }
	}

}
