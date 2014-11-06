package com.sap.sailing.dashboards.gwt.client;

import java.util.Iterator;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.googlecode.mgwt.ui.client.widget.carousel.Carousel;
import com.sap.sailing.dashboards.gwt.client.bottomnotification.BottomNotification;
import com.sap.sailing.dashboards.gwt.client.startanalysis.StartlineAnalysisComponent;
import com.sap.sailing.dashboards.gwt.client.startlineadvantage.StartLineAdvantageComponent;
import com.sap.sailing.dashboards.gwt.client.windchart.WindBotComponent;
import com.sap.sailing.dashboards.gwt.shared.dto.RibDashboardRaceInfoDTO;

/**
 * This class contains all components of the Dashboard and the basic UI Elements
 * of the Dashboard, like top and bottom bars, logos and headers. It initializes the windbot
 * components according to their available number. 
 * 
 * @author Alexander Ries
 * 
 */
public class RibDashboardPanel extends Composite implements RibDashboardDataRetrieverListener{

    interface RootUiBinder extends UiBinder<Widget, RibDashboardPanel> {}
    private static RootUiBinder uiBinder = GWT.create(RootUiBinder.class);

    @UiField
    RibDashboardPanelStyle style;

    @UiField
    LayoutPanel root;

    @UiField
    HTMLPanel header;

    @UiField
    Carousel carousel;

    @UiField
    HTMLPanel leftwindbotcontainer;

    @UiField
    HTMLPanel rightwindbotcontainer;

    @UiField
    public StartLineAdvantageComponent startLineCAComponent;

    @UiField
    public DivElement hint;

    @UiField
    public HTMLPanel bottombar;

    @UiField(provided = true)
    public StartlineAnalysisComponent startanalysisComponent;

    private BottomNotification bottomNotification;
    private boolean hasWindBotsInitialized;

    public RibDashboardPanel() {
        RibDashboardDataRetriever.getInstance().addDataObserver(this);
        initBottomNotification();
        startanalysisComponent = new StartlineAnalysisComponent(bottomNotification);
        initWidget(uiBinder.createAndBindUi(this));
        initLogos();
    }

    private void initLogos() {
        Image logo = new Image();
        logo.setResource(RibDashboardImageResources.INSTANCE.logo_sap());
        logo.getElement().addClassName(style.logo());
        Document.get().getBody().appendChild(logo.getElement());

        Image esslogo = new Image();
        esslogo.setResource(RibDashboardImageResources.INSTANCE.logoess());
        esslogo.getElement().addClassName(style.extremelogo());
        Document.get().getBody().appendChild(esslogo.getElement());
    }

    private void initBottomNotification() {
        bottomNotification = new BottomNotification();
        RootPanel.get().add(bottomNotification);
    }

    private void initWindBotComponents(Set<String> windBotIDs) {
        Iterator<String> windBotIDsIterator = windBotIDs.iterator();
        int windBotAddedCounterTOConsiderForPanels = 0;
        while (windBotIDsIterator.hasNext()) {
            if (windBotAddedCounterTOConsiderForPanels == 0) {
                WindBotComponent leftwindBotComponent = new WindBotComponent(windBotIDsIterator.next());
                leftwindbotcontainer.add(leftwindBotComponent);
            } else if (windBotAddedCounterTOConsiderForPanels == 1) {
                WindBotComponent rightwindBotComponent = new WindBotComponent(windBotIDsIterator.next());
                rightwindbotcontainer.add(rightwindBotComponent);
            }
            windBotAddedCounterTOConsiderForPanels++;
        }
    }

    @Override
    public void updateUIWithNewLiveRaceInfo(RibDashboardRaceInfoDTO liveRaceInfoDTO) {
        this.header.getElement().setInnerText(liveRaceInfoDTO.nameOfLastTrackedRace);
        if (!hasWindBotsInitialized && liveRaceInfoDTO.windBotDTOForID != null) {
            hasWindBotsInitialized = true;
            Set<String> windBotIDs = liveRaceInfoDTO.windBotDTOForID.keySet();
            initWindBotComponents(windBotIDs);
        }
    }
}
