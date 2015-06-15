package com.sap.sailing.dashboards.gwt.client.startlineadvantage;

import java.util.Iterator;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.dashboards.gwt.client.RibDashboardServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;

/**
 * @author Alexander Ries (D062114)
 *
 */
public class StartlineAdvantagesByWindComponent extends Composite implements HasWidgets {

    private static StartlineAdvantagesByWindComponentUiBinder uiBinder = GWT
            .create(StartlineAdvantagesByWindComponentUiBinder.class);

    interface StartlineAdvantagesByWindComponentUiBinder extends UiBinder<Widget, StartlineAdvantagesByWindComponent> {
    }
    
    interface StartlineAdvantagesByWindComponentStyle extends CssResource {
        
    }
    
    @UiField(provided = true)
    LiveAverageComponent advantageMaximumLiveAverage;
    
    @UiField(provided = true)
    StartlineAdvantagesOnLineChart startlineAdvantagesOnLineChart;
    
    @UiField
    StartlineAdvantagesByWindComponentStyle style;
    
    public StartlineAdvantagesByWindComponent(RibDashboardServiceAsync ribDashboardService) {
        startlineAdvantagesOnLineChart = new StartlineAdvantagesOnLineChart(ribDashboardService);
        advantageMaximumLiveAverage = new LiveAverageComponent(StringMessages.INSTANCE.dashboardStartlineAdvantagesByWind(), "s");
        advantageMaximumLiveAverage.header.getStyle().setFontSize(14, Unit.PT);
        advantageMaximumLiveAverage.liveLabel.setInnerHTML("advantage max.");
        advantageMaximumLiveAverage.averageLabel.setInnerHTML("advantage max. average "+StringMessages.INSTANCE.dashboardAverageWindMinutes(15));
        initWidget(uiBinder.createAndBindUi(this));
        advantageMaximumLiveAverage.updateValues("55.7", "43.9");
    }
    
    @Override
    public void add(Widget w) {
        throw new UnsupportedOperationException("The method add(Widget w) is not supported.");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("The method clear() is not supported.");
    }

    @Override
    public Iterator<Widget> iterator() {
        return null;
    }

    @Override
    public boolean remove(Widget w) {
        return false;
    }
}