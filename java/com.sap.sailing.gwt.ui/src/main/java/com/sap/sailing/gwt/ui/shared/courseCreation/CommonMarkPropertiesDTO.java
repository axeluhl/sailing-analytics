package com.sap.sailing.gwt.ui.shared.courseCreation;

import java.io.Serializable;

import com.sap.sailing.domain.common.MarkType;
import com.sap.sse.common.Color;
import com.sap.sse.security.shared.dto.NamedDTO;

public class CommonMarkPropertiesDTO extends NamedDTO implements Serializable {
    private static final long serialVersionUID = 1458294751819986667L;

    private String shortName;
    private Color color;
    private String shape;
    private String pattern;
    private MarkType type;

    protected CommonMarkPropertiesDTO() {
        // GWT serialization
    }

    public CommonMarkPropertiesDTO(String name, String shortName, Color color, String shape, String pattern,
            MarkType type) {
        super(name);
        this.shortName = shortName;
        this.color = color;
        this.shape = shape;
        this.pattern = pattern;
        this.type = type;
    }

    public String getShortName() {
        return shortName;
    }

    public Color getColor() {
        return color;
    }

    public String getShape() {
        return shape;
    }

    public String getPattern() {
        return pattern;
    }

    public MarkType getType() {
        return type;
    }

}
