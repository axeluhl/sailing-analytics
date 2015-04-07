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
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.Navigator;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.googlecode.mgwt.ui.client.widget.carousel.Carousel;
import com.sap.sailing.dashboards.gwt.client.dataretriever.NumberOfWindBotsChangeListener;
import com.sap.sailing.dashboards.gwt.client.dataretriever.RibDashboardDataRetriever;
import com.sap.sailing.dashboards.gwt.client.dataretriever.RibDashboardDataRetrieverListener;
import com.sap.sailing.dashboards.gwt.client.dataretriever.WindBotDataRetrieverProvider;
import com.sap.sailing.dashboards.gwt.client.eventlogo.EventLogo;
import com.sap.sailing.dashboards.gwt.client.notifications.WrongDeviceOrientationNotification;
import com.sap.sailing.dashboards.gwt.client.startanalysis.StartlineAnalysisComponent;
import com.sap.sailing.dashboards.gwt.client.startlineadvantage.StartLineAdvantageComponent;
import com.sap.sailing.dashboards.gwt.client.windchart.WindBotComponent;
import com.sap.sailing.dashboards.gwt.shared.dto.RibDashboardRaceInfoDTO;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;

/**
 * This class contains all components of the Dashboard and the basic UI Elements of the Dashboard, like top and bottom
 * bars, logos and headers. It initializes the windbot components according to their available number.
 * 
 * @author Alexander Ries
 * 
 */
public class RibDashboardPanel extends Composite implements RibDashboardDataRetrieverListener,
        NumberOfWindBotsChangeListener {

    interface RootUiBinder extends UiBinder<Widget, RibDashboardPanel> {
    }

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

    @UiField(provided = true)
    public StartLineAdvantageComponent startLineCAComponent;

    @UiField
    public DivElement windloadinghintleft;

    @UiField
    public DivElement windloadinghintright;

    @UiField
    public HTMLPanel bottombar;
    
    @UiField
    public HTMLPanel windcharthint;

    @UiField(provided = true)
    public StartlineAnalysisComponent startanalysisComponent;

    private List<WindBotComponent> windBotComponents;
    private StringMessages stringConstants;
    private final String EVENT_ID_PARAMETER = "eventId";

    public RibDashboardPanel(RibDashboardServiceAsync ribDashboardService, SailingServiceAsync sailingServiceAsync, RibDashboardDataRetriever ribDashboardDataRetriever) {
        windBotComponents = new ArrayList<WindBotComponent>();
        ribDashboardDataRetriever.addDataObserver(this);
        startanalysisComponent = new StartlineAnalysisComponent(ribDashboardService, sailingServiceAsync);
        startLineCAComponent = new StartLineAdvantageComponent(ribDashboardDataRetriever);
        stringConstants = StringMessages.INSTANCE;
        initWidget(uiBinder.createAndBindUi(this));
        initLogos();
        windcharthint.getElement().setInnerText(stringConstants.dashboardWindChartHint());
        windloadinghintleft.setInnerText(stringConstants.dashboardWindBotLoading());
        windloadinghintright.setInnerText(stringConstants.dashboardWindBotLoading());
        loadAndAddEventLogo(sailingServiceAsync);
        initAndAddWrongOrientationNotification();
    }

    private void initLogos() {
        Image logo = new Image();
        logo.setResource(RibDashboardImageResources.INSTANCE.logo_sap());
        logo.getElement().addClassName(style.logo());
        Document.get().getBody().appendChild(logo.getElement());
    }
    
    private void loadAndAddEventLogo(SailingServiceAsync sailingServiceAsync){
        String eventId = Window.Location.getParameter(EVENT_ID_PARAMETER);
        if (eventId != null) {
            EventLogo eventLogo = EventLogo.getEventLogoFromEventId(sailingServiceAsync, eventId);
            if (eventLogo != null) {
                Document.get().getBody().appendChild(eventLogo.getElement());
            }
        }
    }
    
    private void initAndAddWrongOrientationNotification(){
        WrongDeviceOrientationNotification wrongDeviceOrientationNotification = new WrongDeviceOrientationNotification();
        Document.get().getBody().appendChild(wrongDeviceOrientationNotification.getElement());
        //wrongDeviceOrientationNotification.show();
    }

    private void initWindBotComponents(List<String> windBotIDs,
            WindBotDataRetrieverProvider windBotDataRetrieverProvider) {
        int windBotAddedCounterTOConsiderForPanels = 0;
        leftwindbotcontainer.clear();
        removeWindBotComponentsAsDataRetrieverListener(windBotDataRetrieverProvider);
        windBotComponents.clear();
        for (String windBotID : windBotIDs) {
            if (windBotAddedCounterTOConsiderForPanels == 0) {
                WindBotComponent leftwindBotComponent = new WindBotComponent(windBotID);
                leftwindbotcontainer.add(leftwindBotComponent);
                windBotComponents.add(leftwindBotComponent);
                windloadinghintleft.getStyle().setOpacity(0.0);
            } else if (windBotAddedCounterTOConsiderForPanels == 1) {
                WindBotComponent rightwindBotComponent = new WindBotComponent(windBotID);
                rightwindbotcontainer.add(rightwindBotComponent);
                windBotComponents.add(rightwindBotComponent);
                windloadinghintright.getStyle().setOpacity(0.0);
            }
            windBotAddedCounterTOConsiderForPanels++;
            if (windloadinghintleft.getStyle().getOpacity() != "0.0"
                    || windloadinghintright.getStyle().getOpacity() != "0.0") {
                windloadinghintleft.setInnerHTML(stringConstants.dashboardWindBotNotAvailable());
                windloadinghintright.setInnerHTML(stringConstants.dashboardWindBotNotAvailable());
            }
        }
        addWindBotComponentsAsDataRetrieverListener(windBotDataRetrieverProvider);
    }

    private void addWindBotComponentsAsDataRetrieverListener(WindBotDataRetrieverProvider windBotDataRetrieverProvider) {
        for (WindBotComponent windBotComponent : windBotComponents) {
            windBotDataRetrieverProvider.addWindBotDataRetrieverListener(windBotComponent);
        }
    }

    private void removeWindBotComponentsAsDataRetrieverListener(
            WindBotDataRetrieverProvider windBotDataRetrieverProvider) {
        for (WindBotComponent windBotComponent : windBotComponents) {
            windBotDataRetrieverProvider.removeWindBotDataRetrieverListener(windBotComponent);
        }
    }
    
    @Override
    public void updateUIWithNewLiveRaceInfo(RibDashboardRaceInfoDTO liveRaceInfoDTO) {
        if (liveRaceInfoDTO != null && liveRaceInfoDTO.idOfLastTrackedRace != null) {
            this.header.getElement().setInnerText(liveRaceInfoDTO.idOfLastTrackedRace.getRaceName());
        }
    }

    @Override
    public void numberOfWindBotsChanged(List<String> windBotIDs,
            WindBotDataRetrieverProvider windBotDataRetrieverProvider) {
        logger.log(Level.INFO, "Number of Windbots changed");
        initWindBotComponents(windBotIDs, windBotDataRetrieverProvider);
    }
}
