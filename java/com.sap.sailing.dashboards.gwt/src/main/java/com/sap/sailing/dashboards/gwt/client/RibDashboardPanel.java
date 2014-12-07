package com.sap.sailing.dashboards.gwt.client;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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
public class RibDashboardPanel extends Composite implements RibDashboardDataRetrieverListener, NumberOfWindBotsChangeListener{

    interface RootUiBinder extends UiBinder<Widget, RibDashboardPanel> {}
    private static RootUiBinder uiBinder = GWT.create(RootUiBinder.class);
    private static final Logger logger = Logger.getLogger(RibDashboardPanel.class.getName());

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
    private List<WindBotComponent> windBotComponents;

    public RibDashboardPanel() {
        windBotComponents = new ArrayList<WindBotComponent>();
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

    private void initWindBotComponents(List<String> windBotIDs, WindBotDataRetrieverProvider windBotDataRetrieverProvider) {
        int windBotAddedCounterTOConsiderForPanels = 0;
        leftwindbotcontainer.clear();
        removeWindBotComponentsAsDataRetrieverListener(windBotDataRetrieverProvider);
        windBotComponents.clear();
        for(String windBotID : windBotIDs){
            if (windBotAddedCounterTOConsiderForPanels == 0) {
                WindBotComponent leftwindBotComponent = new WindBotComponent(windBotID);
                leftwindbotcontainer.add(leftwindBotComponent);
                windBotComponents.add(leftwindBotComponent);
            } else if (windBotAddedCounterTOConsiderForPanels == 1) {
                WindBotComponent rightwindBotComponent = new WindBotComponent(windBotID);
                rightwindbotcontainer.add(rightwindBotComponent);
                windBotComponents.add(rightwindBotComponent);
            }
            windBotAddedCounterTOConsiderForPanels++;
        }
        addWindBotComponentsAsDataRetrieverListener(windBotDataRetrieverProvider);
    }
    
    private void addWindBotComponentsAsDataRetrieverListener(WindBotDataRetrieverProvider windBotDataRetrieverProvider){
        for(WindBotComponent windBotComponent : windBotComponents){
            windBotDataRetrieverProvider.addWindBotDataRetrieverListener(windBotComponent);
        }
    }
    
    private void removeWindBotComponentsAsDataRetrieverListener(WindBotDataRetrieverProvider windBotDataRetrieverProvider){
        for(WindBotComponent windBotComponent : windBotComponents){
            windBotDataRetrieverProvider.removeWindBotDataRetrieverListener(windBotComponent);
        }
    }

    @Override
    public void updateUIWithNewLiveRaceInfo(RibDashboardRaceInfoDTO liveRaceInfoDTO) {
        this.header.getElement().setInnerText(liveRaceInfoDTO.idOfLastTrackedRace.getRaceName());
    }

    @Override
    public void numberOfWindBotsChanged(List<String> windBotIDs, WindBotDataRetrieverProvider windBotDataRetrieverProvider) {
        logger.log(Level.INFO, "Number of Windbots changed");
        initWindBotComponents(windBotIDs, windBotDataRetrieverProvider);
    }
}
