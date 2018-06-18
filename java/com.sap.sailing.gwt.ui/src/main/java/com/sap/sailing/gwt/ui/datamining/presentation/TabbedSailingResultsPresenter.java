package com.sap.sailing.gwt.ui.datamining.presentation;

import com.sap.sailing.datamining.shared.ManeuverSpeedDetailsAggregation;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.polarmining.PolarBackendResultsPresenter;
import com.sap.sailing.gwt.ui.polarmining.PolarResultsPresenter;
import com.sap.sailing.polars.datamining.shared.PolarAggregation;
import com.sap.sailing.polars.datamining.shared.PolarBackendData;
import com.sap.sse.datamining.shared.data.PairWithStats;
import com.sap.sse.datamining.ui.client.presentation.TabbedResultsPresenter;
import com.sap.sse.datamining.ui.client.presentation.ResultsChart.DrillDownCallback;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.settings.ComponentContext;

public class TabbedSailingResultsPresenter extends TabbedResultsPresenter {


    public TabbedSailingResultsPresenter(Component<?> parent, ComponentContext<?> context,
            DrillDownCallback drillDownCallback, StringMessages stringMessages) {
        super(parent, context, drillDownCallback);

        registerResultsPresenter(PolarAggregation.class,
                new PolarResultsPresenter(TabbedSailingResultsPresenter.this, getComponentContext(), stringMessages));

        registerResultsPresenter(PolarBackendData.class, new PolarBackendResultsPresenter(
                TabbedSailingResultsPresenter.this, getComponentContext(), stringMessages));

        registerResultsPresenter(ManeuverSpeedDetailsAggregation.class, new ManeuverSpeedDetailsResultsPresenter(
                TabbedSailingResultsPresenter.this, getComponentContext(), stringMessages));

        registerResultsPresenter(PairWithStats.class, new NumberPairResultsPresenter(TabbedSailingResultsPresenter.this,
                getComponentContext(), stringMessages));
    }
}
