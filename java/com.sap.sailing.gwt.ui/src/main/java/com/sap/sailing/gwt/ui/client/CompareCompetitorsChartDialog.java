package com.sap.sailing.gwt.ui.client;

import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.gwt.ui.client.CompareCompetitorsPanel.DataLoadedEvent;
import com.sap.sailing.gwt.ui.client.CompareCompetitorsPanel.DataLoadedHandler;
import com.sap.sailing.gwt.ui.shared.CompetitorDAO;

public class CompareCompetitorsChartDialog extends DialogBox {

    public CompareCompetitorsChartDialog(SailingServiceAsync sailingService, List<CompetitorDAO> competitors, String raceName, String leaderboardName){
        super(true);
        VerticalPanel mainPanel = new VerticalPanel();
        
        //chartBox.setAnimationEnabled(true);
        CompareCompetitorsPanel ccp = new CompareCompetitorsPanel(sailingService, competitors, raceName, leaderboardName);
        ccp.addDataLoadedHandler(new DataLoadedHandler() {
            
            @Override
            public void onDataLoaded(DataLoadedEvent event) {
                CompareCompetitorsChartDialog.this.center();
                CompareCompetitorsChartDialog.this.show();
            }
        });
        mainPanel.add(ccp);
        Button closeButton = new Button("Close");
        closeButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                CompareCompetitorsChartDialog.this.hide();
            }
        });
        mainPanel.add(closeButton);
        this.add(mainPanel);
        this.setTitle("Compare competitors");
        this.center();
    }
}
