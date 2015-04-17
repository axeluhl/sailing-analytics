package com.sap.sse.common.settings;

public class StringSetting implements Setting {
    private final String string;

    public StringSetting(String string) {
        super();
        this.string = string;
    }

    public String getString() {
        return string;
    }

    @Override
    public SettingType getType() {
        return SettingType.STRING;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((string == null) ? 0 : string.hashCode());
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
        StringSetting other = (StringSetting) obj;
        if (string == null) {
            if (other.string != null)
                return false;
        } else if (!string.equals(other.string))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return string;
    }
}
