package com.sap.sse.common.settings;

public class StringSetting implements Setting {
    private final String string;

    public StringSetting(String string) {
        this.string = string;
    }
    
    public StringSetting(Enum<?> literal) {
        this.string = literal.name();
    }

    public String getString() {
        return string;
    }
    
    public <T extends Enum<T>> Enum<T> getEnum(Class<T> enumClass) {
        Enum<T> value = null;
        if (getString() != null) {
            boolean found = false;
            for (Enum<T> literal : enumClass.getEnumConstants()) {
                if (literal.name().equals(getString())) {
                    value = literal;
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new IllegalArgumentException("Couldn't find literal "+getString()+" in enum "+enumClass.getName());
            }
        }
        return value;
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
        return "\""+string+"\"";
    }
}
