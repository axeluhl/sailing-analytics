package com.sap.sailing.gwt.ui.datamining.presentation;

import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.polarmining.PolarBackendResultsPresenter;
import com.sap.sailing.gwt.ui.polarmining.PolarResultsPresenter;
import com.sap.sse.datamining.shared.impl.dto.QueryResultDTO;
import com.sap.sse.datamining.ui.client.presentation.AbstractTabbedResultsPresenter;
import com.sap.sse.datamining.ui.client.presentation.MultiResultsPresenter;
import com.sap.sse.datamining.ui.client.presentation.ResultsChart.DrillDownCallback;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.settings.ComponentContext;

public class TabbedSailingResultsPresenter extends AbstractTabbedResultsPresenter {

    private final StringMessages stringMessages;

    public TabbedSailingResultsPresenter(Component<?> parent, ComponentContext<?> context,
            DrillDownCallback drillDownCallback, StringMessages stringMessages) {
        super(parent, context, drillDownCallback);
        this.stringMessages = stringMessages;
    }

    @Override
    public void showResult(QueryResultDTO<?> result) { // TODO make abstract
        if (result != null) {
            if (result.getResultType().equals("com.sap.sailing.polars.datamining.shared.PolarAggregation")) {
                CloseableTabHeader oldHeader = getSelectedHeader();
                addTabAndFocus(new PolarResultsPresenter(TabbedSailingResultsPresenter.this, getComponentContext(),
                        stringMessages));
                removeTab(oldHeader);
            } else if (result.getResultType().equals("com.sap.sailing.polars.datamining.shared.PolarBackendData")) {
                CloseableTabHeader oldHeader = getSelectedHeader();
                addTabAndFocus(new PolarBackendResultsPresenter(TabbedSailingResultsPresenter.this,
                        getComponentContext(), stringMessages));
                removeTab(oldHeader);
            } else if (result.getResultType()
                    .equals("com.sap.sailing.datamining.shared.ManeuverSpeedDetailsAggregation")) {
                CloseableTabHeader oldHeader = getSelectedHeader();
                addTabAndFocus(new ManeuverSpeedDetailsResultsPresenter(TabbedSailingResultsPresenter.this,
                        getComponentContext(), stringMessages));
                removeTab(oldHeader);
            } else {
                if (!(getSelectedPresenter() instanceof MultiResultsPresenter)) {
                    CloseableTabHeader oldHeader = getSelectedHeader();
                    addTabAndFocus(new MultiResultsPresenter(this, getComponentContext(), drillDownCallback));
                    removeTab(oldHeader);
                }
            }
            getSelectedHeader().setText(result.getResultSignifier());
        }
        getSelectedPresenter().showResult(result);
    }

}
