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
        super(name);
        this.position = position;
    }

    @Override
    public Position getPosition() {
        return this.position;
    }

    @Override
    public String getColor() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getShape() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getPattern() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MarkType getType() {
        // TODO Auto-generated method stub
        return null;
    }

}
