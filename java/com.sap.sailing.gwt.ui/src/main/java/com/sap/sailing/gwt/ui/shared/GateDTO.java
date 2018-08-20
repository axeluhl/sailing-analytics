package com.sap.sailing.gwt.ui.shared;

import java.util.Arrays;

public class GateDTO extends ControlPointDTO {
    private static final long serialVersionUID = 3436355049412041735L;
    private MarkDTO left;
    private MarkDTO right;
    
    public GateDTO() {}
    
    public GateDTO(String idAsString, String name, MarkDTO left, MarkDTO right) {
        super(idAsString, name);
        this.left = left;
        this.right = right;
    }
    
    public MarkDTO getLeft() {
        return left;
    }

    public MarkDTO getRight() {
        return right;
    }

    @Override
    public Iterable<MarkDTO> getMarks() {
        return Arrays.asList(left, right);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = prime * getIdAsString().hashCode() * getName().hashCode();
        result = prime * result + ((left == null) ? 0 : left.hashCode());
        result = prime * result + ((right == null) ? 0 : right.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (getClass() != obj.getClass())
            return false;
        GateDTO other = (GateDTO) obj;
        if (getIdAsString() == null) {
            if (other.getIdAsString() != null)
                return false;
        } else if (!getIdAsString().equals(other.getIdAsString()))
            return false;
        if (getName() == null) {
            if (other.getName() != null)
                return false;
        } else if (!getName().equals(other.getName()))
            return false;
        if (left == null) {
            if (other.left != null)
                return false;
        } else if (!left.equals(other.left))
            return false;
        if (right == null) {
            if (other.right != null)
                return false;
        } else if (!right.equals(other.right))
            return false;
        return true;
    }
    
    
}
