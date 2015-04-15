package com.sap.sse.common.settings;

import java.util.Iterator;

public class ListSetting<T extends Setting> implements Iterable<T>, Setting {
    private final Iterable<T> list;

    public ListSetting(Iterable<T> list) {
        super();
        this.list = list;
    }

    @Override
    public Iterator<T> iterator() {
        return list.iterator();
    }
}
