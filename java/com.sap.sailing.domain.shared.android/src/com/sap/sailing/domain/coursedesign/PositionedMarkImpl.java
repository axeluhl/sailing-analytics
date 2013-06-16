package com.sap.sailing.domain.coursedesign;

import com.sap.sailing.domain.base.impl.MarkImpl;
import com.sap.sailing.domain.common.MarkType;
import com.sap.sailing.domain.common.Position;

public class PositionedMarkImpl extends MarkImpl implements PositionedMark {

    /**
     * 
     */
    private static final long serialVersionUID = -7903960088124343841L;
    private Position position;

    public PositionedMarkImpl(String name, Position position) {
        this(name, position, MarkType.BUOY);
    }
    
    public PositionedMarkImpl(String name, Position position, MarkType markType) {
        super(name, name, markType, null, null, null);
        this.position = position;
    }

    @Override
    public Position getPosition() {
        return this.position;
    }

}
