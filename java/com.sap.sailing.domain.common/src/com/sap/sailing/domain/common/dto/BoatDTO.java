package com.sap.sailing.domain.common.dto;

import java.io.Serializable;

import com.sap.sse.common.Color;

public class BoatDTO extends NamedDTO implements Serializable {
    private static final long serialVersionUID = -4076992788294272162L;

    private String sailId;
    private Color color;

    // for GWT
    BoatDTO() {}

    public BoatDTO(String name, String sailId) {
        this(name, sailId, null);
    }

    public BoatDTO(String name, String sailId, Color color) {
        super(name);
        this.sailId = sailId;
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    public String getSailId() {
        return sailId;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((color == null) ? 0 : color.hashCode());
        result = prime * result + ((sailId == null) ? 0 : sailId.hashCode());
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
        BoatDTO other = (BoatDTO) obj;
        if (color == null) {
            if (other.color != null)
                return false;
        } else if (!color.equals(other.color))
            return false;
        if (sailId == null) {
            if (other.sailId != null)
                return false;
        } else if (!sailId.equals(other.sailId))
            return false;
        return true;
    }
}
