package com.sap.sailing.gwt.home.shared.partials.listview;

import java.util.function.Function;

import com.google.gwt.user.client.ui.IsWidget;
import com.sap.sailing.domain.common.dto.BoatClassDTO;
import com.sap.sailing.gwt.home.shared.partials.multiselection.SuggestedMultiSelectionBoatClassItemDescription;

public class BoatClassListView extends ListView<BoatClassDTO> {

    public BoatClassListView() {
        super(new Function<BoatClassDTO, IsWidget>() {
            @Override
            public IsWidget apply(BoatClassDTO t) {
                return new SuggestedMultiSelectionBoatClassItemDescription(t);
            }
        });
    }
}
