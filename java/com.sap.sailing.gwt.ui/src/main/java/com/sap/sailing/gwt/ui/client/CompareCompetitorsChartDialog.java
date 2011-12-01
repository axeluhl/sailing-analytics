package com.sap.sailing.gwt.ui.client;

import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.gwt.ui.client.CompareCompetitorsPanel.DataLoadedEvent;
import com.sap.sailing.gwt.ui.client.CompareCompetitorsPanel.DataLoadedHandler;
import com.sap.sailing.gwt.ui.shared.CompetitorDAO;

public class CompareCompetitorsChartDialog extends DialogBox {

    public CompareCompetitorsChartDialog(SailingServiceAsync sailingService, List<CompetitorDAO> competitors, String raceName, String leaderboardName, StringConstants stringConstants){
        super(true);
        VerticalPanel mainPanel = new VerticalPanel();
        mainPanel.setSpacing(5);
        mainPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
        CompareCompetitorsPanel ccp = new CompareCompetitorsPanel(sailingService, competitors, raceName, leaderboardName, stringConstants, (int) (Window.getClientWidth()*0.8), (int) (Window.getClientHeight()*0.8));
        ccp.addDataLoadedHandler(new DataLoadedHandler() {
            
            @Override
            public void onDataLoaded(DataLoadedEvent event) {
                CompareCompetitorsChartDialog.this.setPopupPosition(5, 5);
                CompareCompetitorsChartDialog.this.show();
            }
        });
        mainPanel.add(ccp);
        Button closeButton = new Button(stringConstants.close());
        closeButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                CompareCompetitorsChartDialog.this.hide();
            }
        });
        mainPanel.add(closeButton);
        this.add(mainPanel);

        this.setTitle(stringConstants.compareCompetitors());
        this.setPopupPosition(5, 5);
    }
}
