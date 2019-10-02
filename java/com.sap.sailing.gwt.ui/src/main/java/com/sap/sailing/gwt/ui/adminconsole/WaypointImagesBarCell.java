package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.view.client.ListDataProvider;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.WaypointDTO;
import com.sap.sse.security.ui.client.component.DefaultActionsImagesBarCell;

public class WaypointImagesBarCell extends DefaultActionsImagesBarCell {
    public static final String ACTION_ORC_PCS_DEFINE_LEG = "ACTION_DEFINE_LEG";
    private static AdminConsoleResources resources = GWT.create(AdminConsoleResources.class);
    private final StringMessages stringMessages;
    private final ListDataProvider<WaypointDTO> waypointList;
    
    public WaypointImagesBarCell(final StringMessages stringMessages, ListDataProvider<WaypointDTO> waypointList) {
        super(stringMessages);
        this.stringMessages = stringMessages;
        this.waypointList = waypointList;
    }

    @Override
    protected Iterable<ImageSpec> getImageSpecs() {
        final WaypointDTO waypoint = (WaypointDTO) getContext().getKey();
        final List<ImageSpec> imageSpecs = new ArrayList<>();
        imageSpecs.add(getDeleteImageSpec());
        if (waypoint != waypointList.getList().get(0)) {
            imageSpecs.add(new ImageSpec(ACTION_ORC_PCS_DEFINE_LEG, stringMessages.actionDefineLegForOrcPcs(), resources.orcPcsDefineLegIcon()));
        }
        return imageSpecs;
    }
}