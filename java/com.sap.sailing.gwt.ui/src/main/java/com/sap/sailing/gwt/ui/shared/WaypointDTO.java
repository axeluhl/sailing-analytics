package com.sap.sailing.gwt.ui.shared;

import java.util.List;

public class WaypointDTO extends NamedDTO {
    public List<MarkDTO> buoys;
    
    public int courseIndex;

    public int markPassingsCount;
    
    public WaypointDTO() {}
    
    public WaypointDTO(String name, int courseIndex) {
        super(name);
        this.courseIndex = courseIndex;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + courseIndex;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        WaypointDTO other = (WaypointDTO) obj;
        if (courseIndex != other.courseIndex)
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }
}
