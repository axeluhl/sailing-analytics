package com.sap.sse.security.ui.client.premium;

import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.celltable.CellTableWithCheckboxResources;
import com.sap.sse.gwt.client.celltable.EntityIdentityComparator;
import com.sap.sse.gwt.client.celltable.RefreshableSingleSelectionModel;
import com.sap.sse.gwt.client.celltable.TableWrapper;
import com.sap.sse.security.ui.client.i18n.StringMessages;
import com.sap.sse.security.ui.shared.subscription.SubscriptionPlanDTO;

public class FeatureOverviewTableWrapper extends
        TableWrapper<SubscriptionPlanDTO, RefreshableSingleSelectionModel<SubscriptionPlanDTO>, StringMessages, CellTableWithCheckboxResources> {

    public FeatureOverviewTableWrapper(StringMessages stringMessages, ErrorReporter errorReporter) {
        super(stringMessages, errorReporter, false, false,
                new EntityIdentityComparator<SubscriptionPlanDTO>() {
                    @Override
                    public boolean representSameEntity(SubscriptionPlanDTO dto1, SubscriptionPlanDTO dto2) {
                        return dto1.getId().equals(dto2.getId());
                    }
                    @Override
                    public int hashCode(SubscriptionPlanDTO t) {
                        return t.getId().hashCode();
                    }
                });
        final Column<SubscriptionPlanDTO, String> nameColumn = new TextColumn<SubscriptionPlanDTO>() {
            @Override
            public String getValue(SubscriptionPlanDTO object) {
                return object.getName();
            }
        };
        table.addColumn(nameColumn);
        final Column<SubscriptionPlanDTO, String> featuresColumn = new TextColumn<SubscriptionPlanDTO>() {
            @Override
            public String getValue(SubscriptionPlanDTO object) {
                return object.getFeatures().toString();
            }
        };
        table.addColumn(featuresColumn);
        final Column<SubscriptionPlanDTO, String> priceColumn = new TextColumn<SubscriptionPlanDTO>() {
            @Override
            public String getValue(SubscriptionPlanDTO object) {
                return object.getPrice();
            }
        };
        table.addColumn(priceColumn);
    }
    
}
