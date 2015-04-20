package com.sap.sse.common.settings;

public class EnumSetting<T extends Enum<T>> implements Setting {
    private final Enum<T> value;

    public EnumSetting(Enum<T> value) {
        super();
        this.value = value;
    }

    public Enum<T> getValue() {
        return value;
    }

    @Override
    public SettingType getType() {
        return SettingType.ENUM;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((value == null) ? 0 : value.hashCode());
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
        EnumSetting<?> other = (EnumSetting<?>) obj;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return value.name();
    }
}
