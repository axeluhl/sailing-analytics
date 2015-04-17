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

    @Override
    public SettingType getType() {
        return SettingType.LIST;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((list == null) ? 0 : list.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ListSetting<?> other = (ListSetting<?>) obj;
        if (list == null) {
            if (other.list != null)
                return false;
        } else if (!list.equals(other.list))
            return false;
        return true;
    }
    
    @Override
    public String toString() {
        return list.toString();
    }
}
