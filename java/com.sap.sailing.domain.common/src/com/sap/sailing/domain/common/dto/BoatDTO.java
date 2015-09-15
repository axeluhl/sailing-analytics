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
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    public String getSailId() {
        return sailId;
    }
}
