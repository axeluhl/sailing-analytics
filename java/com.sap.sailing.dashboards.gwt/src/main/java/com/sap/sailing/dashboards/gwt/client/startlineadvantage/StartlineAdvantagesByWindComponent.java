package com.sap.sailing.dashboards.gwt.client.startlineadvantage;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.dashboards.gwt.client.RibDashboardServiceAsync;
import com.sap.sailing.dashboards.gwt.client.actions.GetStartlineAdvantagesAction;
import com.sap.sailing.dashboards.gwt.shared.dto.StartLineAdvantageDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.player.TimeListener;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.player.Timer.PlayModes;

/**
 * @author Alexander Ries (D062114)
 *
 */
public class StartlineAdvantagesByWindComponent extends Composite implements HasWidgets, TimeListener {

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
    
    private RibDashboardServiceAsync ribDashboardService;
    private AsyncActionsExecutor asyncActionsExecutor;
    private String leaderboardName;
    
    public StartlineAdvantagesByWindComponent(RibDashboardServiceAsync ribDashboardService) {
        startlineAdvantagesOnLineChart = new StartlineAdvantagesOnLineChart();
        advantageMaximumLiveAverage = new LiveAverageComponent(StringMessages.INSTANCE.dashboardStartlineAdvantagesByWind(), "s");
        advantageMaximumLiveAverage.header.getStyle().setFontSize(14, Unit.PT);
        advantageMaximumLiveAverage.liveLabel.setInnerHTML("advantage max.");
        advantageMaximumLiveAverage.averageLabel.setInnerHTML("advantage max. average "+StringMessages.INSTANCE.dashboardAverageWindMinutes(15));
        initWidget(uiBinder.createAndBindUi(this));
        this.ribDashboardService = ribDashboardService;
        this.asyncActionsExecutor = new AsyncActionsExecutor();
        initSampleTimer();
    }
    
    private void loadData() {
        GetStartlineAdvantagesAction getRibDashboardRaceInfoAction = new GetStartlineAdvantagesAction(
                ribDashboardService, "");
        asyncActionsExecutor.execute(getRibDashboardRaceInfoAction, new AsyncCallback<List<StartLineAdvantageDTO>>() {
            
            @Override
            public void onFailure(Throwable caught) {
            }

            @Override
            public void onSuccess(List<StartLineAdvantageDTO> result) {
                startlineAdvantagesOnLineChart.setStartlineAdvantages(result);
                advantageMaximumLiveAverage.setValues("44.2", "23.1");
            }

        });
    }
    
    private void initSampleTimer(){
        Timer timer = new Timer(PlayModes.Live);
        timer.setRefreshInterval(3000);
        timer.addTimeListener(this);
        timer.play();
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
    
    @Override
    public void timeChanged(Date newTime, Date oldTime) {
        loadData();
    }
}