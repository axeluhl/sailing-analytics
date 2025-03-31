package com.sap.sailing.dashboards.gwt.client.dashboardpanel;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.googlecode.mgwt.ui.client.widget.carousel.Carousel;
import com.sap.sailing.dashboards.gwt.client.DashboardClientFactory;
import com.sap.sailing.dashboards.gwt.client.actions.GetIDFromRaceThatIsLiveAction;
import com.sap.sailing.dashboards.gwt.client.dataretriever.NumberOfWindBotsChangeListener;
import com.sap.sailing.dashboards.gwt.client.dataretriever.WindBotDataRetrieverProvider;
import com.sap.sailing.dashboards.gwt.client.header.DashboardHeader;
import com.sap.sailing.dashboards.gwt.client.notifications.orientation.WrongDeviceOrientationNotification;
import com.sap.sailing.dashboards.gwt.client.widgets.PollsLiveDataEvery5Seconds;
import com.sap.sailing.dashboards.gwt.client.widgets.startanalysis.StartAnalysisWidget;
import com.sap.sailing.dashboards.gwt.client.widgets.startlineadvantage.course.StartlineAdvantageByGeometryWidget;
import com.sap.sailing.dashboards.gwt.client.widgets.startlineadvantage.wind.StartlineAdvantagesByWindWidget;
import com.sap.sailing.dashboards.gwt.client.widgets.windbot.WindBotWidget;
import com.sap.sailing.dashboards.gwt.shared.DashboardURLParameters;
import com.sap.sailing.dashboards.gwt.shared.dto.RaceIdDTO;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.media.ImageDTO;

/**
 * This class contains all components of the Dashboard and the basic UI Elements of the Dashboard, like top and bottom
 * bars, logos and headers. It initializes the windbot components according to their available number.
 * 
 * @author Alexander Ries
 * 
 */
public class DashboardPanel extends Composite implements NumberOfWindBotsChangeListener, PollsLiveDataEvery5Seconds {

    interface RootUiBinder extends UiBinder<Widget, DashboardPanel> {}

    private static RootUiBinder uiBinder = GWT.create(RootUiBinder.class);

    @UiField
    LayoutPanel root;
    
    @UiField
    DashboardHeader header;

    @UiField
    Carousel carousel;

    @UiField(provided = true)
    WindBotWidget leftWindBotWidget;
    
    @UiField(provided = true)
    WindBotWidget rightWindBotWidget;
    
    @UiField(provided = true)
    StartlineAdvantagesByWindWidget startlineAdvantagesByWindComponent;

    @UiField
    public HTMLPanel bottombar;
    
    @UiField
    public HTMLPanel windcharthint;

    @UiField
    public Image eventLogo;
    
    @UiField(provided = true)
    public StartAnalysisWidget startanalysisComponent;
    
    @UiField(provided = true)
    public StartlineAdvantageByGeometryWidget startlineAdvantageByGeometryWidget;

    private StringMessages stringConstants;
    DashboardClientFactory dashboardClientFactory;
    private static final Logger logger = Logger.getLogger(DashboardPanel.class.getName());

    public DashboardPanel(DashboardClientFactory dashboardClientFactory, ErrorReporter errorReporter) {
        DashboardPanelResources.INSTANCE.style().ensureInjected();
        this.dashboardClientFactory = dashboardClientFactory;
        leftWindBotWidget = new WindBotWidget(dashboardClientFactory);
        rightWindBotWidget = new WindBotWidget(dashboardClientFactory);
        startanalysisComponent = new StartAnalysisWidget(this.dashboardClientFactory, errorReporter);
        startlineAdvantagesByWindComponent = new StartlineAdvantagesByWindWidget(this.dashboardClientFactory);
        startlineAdvantageByGeometryWidget = new StartlineAdvantageByGeometryWidget(this.dashboardClientFactory);
        stringConstants = StringMessages.INSTANCE;
        initWidget(uiBinder.createAndBindUi(this));
        windcharthint.getElement().setInnerText(stringConstants.dashboardWindChartHint());
        loadEventLogoAndName(dashboardClientFactory.getSailingService());
        initAndAddWrongOrientationNotification();
        registerForDashboardFiveSecondsTimer(dashboardClientFactory);
    }
    
    private void loadEventLogoAndName(SailingServiceAsync sailingServiceAsync){
        String eventId = DashboardURLParameters.EVENT_ID.getValue();
        if (eventId != null) {
            try {
                final UUID eventUUID = UUID.fromString(eventId);
                logger.log(Level.INFO, "Loading EventDTO for id " + eventId);
                sailingServiceAsync.getEventById(eventUUID, true, new AsyncCallback<EventDTO>() {
                    @Override
                    public void onSuccess(final EventDTO event) {
                        logger.log(Level.INFO, "Received EventDTO");
                        if (event != null) {
                            final ImageDTO logo = event.getLogoImage();
                            if (logo != null) {
                                eventLogo.getElement().setAttribute("src", UriUtils.fromString(logo.getSourceRef()).asString());
                            } else {
                                eventLogo.getElement().getStyle().setVisibility(Visibility.HIDDEN);
                            }
                            if (event.getName() != null) {
                                header.setEventName(event.getName());
                                Window.setTitle("Dashboard - "+event.getName());
                            }
                        } else {
                            logger.log(Level.INFO, "Received EventDTO is null");
                        }
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        logger.log(Level.INFO, "Failed to received EventDTO, "+caught.getMessage());
                        eventLogo.getElement().getStyle().setVisibility(Visibility.HIDDEN);
                    }
                });
            } catch (IllegalArgumentException e) {
                eventLogo.getElement().getStyle().setVisibility(Visibility.HIDDEN);
            }
        }
    }
    
    private void initAndAddWrongOrientationNotification(){
        WrongDeviceOrientationNotification wrongDeviceOrientationNotification = new WrongDeviceOrientationNotification();
        Document.get().getBody().appendChild(wrongDeviceOrientationNotification.getElement());
    }
    
    @Override
    public void timeChanged(Date newTime, Date oldTime) {
        String leaderboardNameParameterValue = DashboardURLParameters.LEADERBOARD_NAME.getValue();
        if (leaderboardNameParameterValue != null) {
            dashboardClientFactory.getDispatch().execute(
                    new GetIDFromRaceThatIsLiveAction(leaderboardNameParameterValue), new AsyncCallback<RaceIdDTO>() {

                        @Override
                        public void onSuccess(RaceIdDTO result) {
                            if (result != null && result.getRaceId() != null) {
                                logger.log(Level.INFO, "Updating UI with live race id");
                                header.setRaceName(result.getRaceId().getRaceName());
                            } else {
                                logger.log(Level.INFO, "Received null for live race id");
                            }
                        }

                        @Override
                        public void onFailure(Throwable caught) {
                            logger.log(Level.INFO, "Failed to received live race id" + caught.getMessage());
                        }
                    });
        }
    }

    @Override
    public void registerForDashboardFiveSecondsTimer(DashboardClientFactory dashboardClientFactory) {
        if (dashboardClientFactory != null) {
            dashboardClientFactory.getDashboardFiveSecondsTimer().addTimeListener(this);
        }
    }
    
    @Override
    public void numberOfWindBotsReceivedChanged(List<String> windBotIDs, WindBotDataRetrieverProvider windBotDataRetrieverProvider) {
        if (windBotIDs != null) {
            logger.log(Level.INFO, "Number of available windbots changed to " + windBotIDs.size());
            for (int i = 0; i < windBotIDs.size(); i++) {
                if (i == 0) {
                    leftWindBotWidget.setWindBotId(windBotIDs.get(i));
                    windBotDataRetrieverProvider.addWindBotDataRetrieverListener(leftWindBotWidget);
                }
                if (i == 1) {
                    rightWindBotWidget.setWindBotId(windBotIDs.get(i));
                    windBotDataRetrieverProvider.addWindBotDataRetrieverListener(rightWindBotWidget);
                }
            }
        } else {
            logger.log(Level.INFO, "Number of available windbots changed. WindBots List is null");
        }
    }
}
