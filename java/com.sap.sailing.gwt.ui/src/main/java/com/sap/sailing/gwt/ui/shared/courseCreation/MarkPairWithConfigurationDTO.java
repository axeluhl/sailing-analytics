package com.sap.sailing.gwt.ui.shared.courseCreation;

import java.util.Arrays;

public class MarkPairWithConfigurationDTO extends ControlPointWithMarkConfigurationDTO {
    private static final long serialVersionUID = -6712123101182399453L;
    private MarkConfigurationDTO left;
    private MarkConfigurationDTO right;
    private String shortName;
    private String name;

    @Override
    public Iterable<MarkConfigurationDTO> getMarkConfigurations() {
        return Arrays.asList(left, right);
    }

    public MarkConfigurationDTO getLeft() {
        return left;
    }

    public void setLeft(MarkConfigurationDTO left) {
        this.left = left;
    }

    public MarkConfigurationDTO getRight() {
        return right;
    }

    public void setRight(MarkConfigurationDTO right) {
        this.right = right;
    }

    @Override
    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
