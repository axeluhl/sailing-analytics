package com.sap.sailing.dashboards.gwt.client.widgets.startanalysis;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.dashboards.gwt.client.DashboardClientFactory;
import com.sap.sailing.dashboards.gwt.client.actions.GetCompetitorInLeaderboardAction;
import com.sap.sailing.dashboards.gwt.client.actions.GetStartAnalysesAction;
import com.sap.sailing.dashboards.gwt.client.notifications.bottom.BottomNotification;
import com.sap.sailing.dashboards.gwt.client.notifications.bottom.BottomNotificationClickListener;
import com.sap.sailing.dashboards.gwt.client.notifications.bottom.BottomNotificationType;
import com.sap.sailing.dashboards.gwt.client.popups.competitorselection.CompetitorSelectionListener;
import com.sap.sailing.dashboards.gwt.client.popups.competitorselection.CompetitorSelectionPopup;
import com.sap.sailing.dashboards.gwt.client.popups.competitorselection.util.SettingsButtonWithSelectionIndicationLabel;
import com.sap.sailing.dashboards.gwt.client.widgets.ActionPanel;
import com.sap.sailing.dashboards.gwt.client.widgets.PollsLiveDataEvery5Seconds;
import com.sap.sailing.dashboards.gwt.client.widgets.header.DashboardWidgetHeaderAndNoDataMessage;
import com.sap.sailing.dashboards.gwt.client.widgets.startanalysis.card.StartAnalysisCard;
import com.sap.sailing.dashboards.gwt.shared.DashboardURLParameters;
import com.sap.sailing.dashboards.gwt.shared.dto.LeaderboardCompetitorsDTO;
import com.sap.sailing.dashboards.gwt.shared.dto.StartAnalysesDTO;
import com.sap.sailing.dashboards.gwt.shared.dto.StartAnalysisDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMapResources;
import com.sap.sse.gwt.client.ErrorReporter;

/**
 * The class contains an collection of {@link StartAnalysisCard}s that are displayed in horizontal aligned pages. It
 * shows only one {@link StartAnalysisCard} at a time and enables the user to switch between {@link StartAnalysisCard}s
 * with left and right buttons represented by {@link #left_focus_panel} and {@link #right_focus_panel}. When a new
 * {@link StartAnalysisCard} gets added to the component it shows a {@link BottomNotification}.
 * 
 * @author Alexander Ries (D062114)
 *
 */
public class StartAnalysisWidget extends Composite implements HasWidgets, PollsLiveDataEvery5Seconds {

    private static StartAnalysisWidgetUiBinder uiBinder = GWT.create(StartAnalysisWidgetUiBinder.class);

    interface StartAnalysisWidgetUiBinder extends UiBinder<Widget, StartAnalysisWidget> {
    }

    interface StartlineAnalysis extends CssResource {
    }

    @UiField(provided = true)
    DashboardWidgetHeaderAndNoDataMessage dashboardWidgetHeaderAndNoDataMessage;

    @UiField(provided = true)
    SettingsButtonWithSelectionIndicationLabel selectedCompetitorWithSettingsButton;

    @UiField
    HTMLPanel currentStartPanel;

    @UiField
    HTMLPanel rightButton;

    @UiField
    HTMLPanel leftButton;

    @UiField
    Image rightButtonImage;

    @UiField
    Image leftButtonImage;

    @UiField
    FocusPanel left_focus_panel;

    @UiField
    FocusPanel right_focus_panel;

    /**
     * Gets populated with new {@link StartAnalysisCard}s when the method
     * {@link #addStartAnalysisCard(StartAnalysisDTO)} gets called.
     * */
    @UiField
    HTMLPanel startanalysis_card_container;

    /**
     * The CSS "left" property from the {@link #startanalysis_card_container}.
     * */
    private double currentScrollPosition = 0;

    /**
     * The page or {@link StartAnalysisCard} the {@link #startanalysis_card_container} displays. Begins with 0.
     * */
    private int page = 0;

    /**
     * Amount of {@link StartAnalysisCard}s in {@link #startanalysis_card_container}.
     * */
    private int numberOfStartAnalysisCards = 0;
    private boolean displaysCards;
    private final List<StartAnalysisDTO> starts;
    private final List<StartAnalysisCard> pageChangeListener;
    private BottomNotification bottomNotification;
    private CompetitorSelectionPopup competitorSelectionPopup;
    private final DashboardClientFactory dashboardClientFactory;
    private final ErrorReporter errorReporter;

    private final String cookieKeyForSelectedCompetitorInLeaderboard;

    private static final String SELECTED_COMPETITOR_IN_LEADERBOARD_COOKIE_KEY_FIRST_PART = "competitorId";
    private static final int SELECTED_COMPETITOR_ID_COOKIE_KEY_EXPIRE_TIME_IN_MILLIS = 60 * 1000 * 60 * 5;
    private static final int SCROLL_OFFSET_STARTANALYSIS_CARDS = 80;
    private static final double MARGIN_LEFT_STARTANALYSIS_CARD = 12.5;
    private static final Logger logger = Logger.getLogger(StartAnalysisWidget.class.getName());

    private static final RaceMapResources raceMapResources = GWT.create(DashboardRaceMapResources.class);

    /**
     * Component that contains handles, displays and loads startanalysis cards.
     * */
    public StartAnalysisWidget(DashboardClientFactory dashboardClientFactory, ErrorReporter errorReporter) {
        StartAnalysisWidgetResources.INSTANCE.gss().ensureInjected();
        this.errorReporter = errorReporter;
        dashboardWidgetHeaderAndNoDataMessage = new DashboardWidgetHeaderAndNoDataMessage();
        selectedCompetitorWithSettingsButton = new SettingsButtonWithSelectionIndicationLabel();
        selectedCompetitorWithSettingsButton.disable();
        selectedCompetitorWithSettingsButton.setSelectionIndicationTextOnLabel(StringMessages.INSTANCE.dashboardSelectCompetitor());
        addClickListenerToSettingsButtonAndLabel();
        raceMapResources.raceMapStyle().ensureInjected();
        this.dashboardClientFactory = dashboardClientFactory;
        pageChangeListener = new ArrayList<StartAnalysisCard>();
        starts = new ArrayList<StartAnalysisDTO>();
        cookieKeyForSelectedCompetitorInLeaderboard = computeCookieKeyForSelectedCompetitorInLeaderboard();
        initWidget(uiBinder.createAndBindUi(this));
        dashboardWidgetHeaderAndNoDataMessage.setHeaderText(StringMessages.INSTANCE.dashboardStartAnalysesHeader());
        initCompetitorSelectionPopupAndAddCompetitorSelectionListener();
        initLeftRightButtons();
        initAndAddBottomNotification();
        registerForDashboardFiveSecondsTimer(dashboardClientFactory);
    }

    private String computeCookieKeyForSelectedCompetitorInLeaderboard() {
        String result = SELECTED_COMPETITOR_IN_LEADERBOARD_COOKIE_KEY_FIRST_PART+DashboardURLParameters.LEADERBOARD_NAME.getValue();
        return result;
    }

    private String getCachedSelectedCompetitorId() {
        String result = null;
        String selectedCompetitorId = Cookies.getCookie(cookieKeyForSelectedCompetitorInLeaderboard);
        if (selectedCompetitorId == null || "undefined".equals(selectedCompetitorId)) {
            result = null;
        } else {
            result = selectedCompetitorId;
        }
        return result;
    }

    private void initLeftRightButtons() {
        leftButtonImage.setResource(StartAnalysisWidgetResources.INSTANCE.leftdisabled());
        left_focus_panel.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                clickedArrowButton(false);
            }
        });
        rightButtonImage.setResource(StartAnalysisWidgetResources.INSTANCE.rightdisabled());
        right_focus_panel.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                clickedArrowButton(true);
            }
        });
    }

    private void loadStartAnalysisDTOsForCompetitorID(String competitorIdAsString) {
        logger.log(Level.INFO, "Loading startanalysis for competitor id " + competitorIdAsString);
        dashboardClientFactory.getDispatch().execute(
                new GetStartAnalysesAction(DashboardURLParameters.LEADERBOARD_NAME.getValue(), competitorIdAsString),
                new AsyncCallback<StartAnalysesDTO>() {

                    @Override
                    public void onFailure(Throwable caught) {
                        logger.log(Level.INFO, "Failed to received startanalysis list, "+caught.getMessage());
                        dashboardWidgetHeaderAndNoDataMessage.showNoDataMessageWithHeaderAndMessage(StringMessages.INSTANCE.dashboardNoStartAnalysesAvailableHeader(), StringMessages.INSTANCE.dashboardNoStartAnalysesAvailableMessage());
                        selectedCompetitorWithSettingsButton.disable();
                    }

                    @Override
                    public void onSuccess(StartAnalysesDTO result) {
                        logger.log(Level.INFO, "Received startanalysis list");
                        if (result != null && !result.getStartAnalyses().isEmpty() && result.getStartAnalyses().size() > 0) {
                            dashboardWidgetHeaderAndNoDataMessage.hideNoDataMessage();
                            if (displayedStartAnalysisForDifferentCompetitorToRequestedOne()) {
                                removeAllStartAnalysisCards();
                            }
                            if (result.getStartAnalyses().size() != starts.size()) {
                                showNotificationForNewStartAnalysis();
                            }
                            logger.log(Level.INFO, "Updating UI with startanalysis list");
                            addNewStartAnalysisCards(result.getStartAnalyses());
                            showControllHeaderButtons();
                            if (result.getStartAnalyses().get(0).competitor != null) {
                                selectedCompetitorWithSettingsButton.setSelectionIndicationTextOnLabel(result.getStartAnalyses().get(0).competitor.getName());
                            }
                            selectedCompetitorWithSettingsButton.enable();
                        } else {
                            logger.log(Level.INFO, "Received startanalysis list is null or empty");
                            dashboardWidgetHeaderAndNoDataMessage.showNoDataMessageWithHeaderAndMessage(StringMessages.INSTANCE.dashboardNoStartAnalysesAvailableHeader(), StringMessages.INSTANCE.dashboardNoStartAnalysesAvailableMessage());
                        }
                    }
                });
    }

    private void initAndAddBottomNotification() {
        bottomNotification = new BottomNotification();
        bottomNotification.addBottomNotificationClickListener(new BottomNotificationClickListener() {

            @Override
            public void bottomNotificationClicked() {
                scrollToLast();
            }
        });
        RootPanel.get().add(bottomNotification);
    }

    private void initCompetitorSelectionPopupAndAddCompetitorSelectionListener() {
        competitorSelectionPopup = new CompetitorSelectionPopup();
        competitorSelectionPopup.addListener(new CompetitorSelectionListener() {
            @Override
            public void didClickOKWithSelectedCompetitor(CompetitorDTO competitor) {
                if (competitor != null) {
                    Cookies.removeCookie(cookieKeyForSelectedCompetitorInLeaderboard);
                    Cookies.setCookie(cookieKeyForSelectedCompetitorInLeaderboard, competitor.getIdAsString(), new Date(new Date().getTime()+SELECTED_COMPETITOR_ID_COOKIE_KEY_EXPIRE_TIME_IN_MILLIS));
                    if (selectedCompetitorWithSettingsButton != null) {
                        selectedCompetitorWithSettingsButton.setSelectionIndicationTextOnLabel(competitor.getName());
                    }
                    loadStartAnalysisDTOsForCompetitorID(competitor.getIdAsString());
                }
            }
        });
    }

    private void loadCompetitorsAndShowCompetitorSelectionPopup() {
        logger.log(Level.INFO, "Requesting Competitors in Leaderboard");
        dashboardClientFactory.getDispatch().execute(
                new GetCompetitorInLeaderboardAction(DashboardURLParameters.LEADERBOARD_NAME.getValue()),
                new AsyncCallback<LeaderboardCompetitorsDTO>() {

                    @Override
                    public void onSuccess(LeaderboardCompetitorsDTO result) {
                        logger.log(Level.INFO, "Received competitors for leaderboard name");
                        if (result.getCompetitors() != null && result.getCompetitors().size() > 0) {
                            selectedCompetitorWithSettingsButton.enable();
                            if (!competitorSelectionPopup.isShown()) {
                                logger.log(Level.INFO, "Showing CompetitorSelectionPopup with competitors");
                                competitorSelectionPopup.show(result.getCompetitors());
                            } else {
                                logger.log(Level.INFO, "CompetitorSelectionPopup aleady shown");
                            }
                        } else {
                            logger.log(Level.INFO, "Received competitors are null or empty");
                            dashboardWidgetHeaderAndNoDataMessage.showNoDataMessageWithHeaderAndMessage(
                                    StringMessages.INSTANCE.dashboardNoStartAnalysesAvailableHeader(),
                                    StringMessages.INSTANCE.dashboardNoStartAnalysesAvailableMessage());
                            selectedCompetitorWithSettingsButton.disable();
                        }
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        logger.log(Level.INFO,
                                "Failed to received competitors for leaderboard name, " + caught.getMessage());
                        dashboardWidgetHeaderAndNoDataMessage.showNoDataMessageWithHeaderAndMessage(
                                StringMessages.INSTANCE.dashboardNoStartAnalysesAvailableHeader(),
                                StringMessages.INSTANCE.dashboardNoStartAnalysesAvailableMessage());
                        selectedCompetitorWithSettingsButton.disable();
                    }
                });
    }

    private void addClickListenerToSettingsButtonAndLabel() {
        selectedCompetitorWithSettingsButton.addActionListener(new ActionPanel.ActionPanelListener() {

            @Override
            public void eventTriggered() {
                loadCompetitorsAndShowCompetitorSelectionPopup();
            }
        });
    }

    /**
     * <param>clickedLeft</param> if clicked left is true, the left arrow button was pressed, a false represent the
     * right button. The buttons are represented by the member variables {@link #left_focus_panel} and
     * {@link #right_focus_panel}.
     * */
    private void clickedArrowButton(boolean clickedLeft) {
        if (displaysCards == true) {
            if (clickedLeft) {
                if (page != 0) {
                    currentScrollPosition += SCROLL_OFFSET_STARTANALYSIS_CARDS;
                    page--;
                    startanalysis_card_container.getElement().getStyle().setLeft(currentScrollPosition, Unit.PCT);
                    notifyStartAnalysisPageChangeListener(page);
                    int displayPage = page + 1;
                    currentStartPanel.getElement().setInnerHTML(starts.get(displayPage - 1).raceName);
                    if (page != numberOfStartAnalysisCards - 1) {
                        rightButtonImage.setResource(StartAnalysisWidgetResources.INSTANCE.right());
                        rightButton.getElement().getStyle().setProperty("disabled", "false");
                    }
                    if (page == 0) {
                        leftButtonImage.setResource(StartAnalysisWidgetResources.INSTANCE.leftdisabled());
                        leftButtonImage.getElement().getStyle().setProperty("disabled", "true");
                    }
                }
            } else {
                if (page != numberOfStartAnalysisCards - 1) {
                    currentScrollPosition -= SCROLL_OFFSET_STARTANALYSIS_CARDS;
                    page++;
                    startanalysis_card_container.getElement().getStyle().setLeft(currentScrollPosition, Unit.PCT);
                    notifyStartAnalysisPageChangeListener(page);
                    int displayPage = page + 1;
                    currentStartPanel.getElement().setInnerHTML(starts.get(displayPage - 1).raceName);
                    if (page >= 0) {
                        leftButtonImage.setResource(StartAnalysisWidgetResources.INSTANCE.left());
                        leftButton.getElement().getStyle().setProperty("disabled", "false");
                    }
                    if (page == numberOfStartAnalysisCards - 1) {
                        rightButtonImage.setResource(StartAnalysisWidgetResources.INSTANCE.rightdisabled());
                        rightButton.getElement().getStyle().setProperty("disabled", "true");
                    }
                }
            }
        }
    }

    private void showControllHeaderButtons() {
        leftButton.getElement().addClassName(
                StartAnalysisWidgetResources.INSTANCE.gss().controll_header_button_visible());
        rightButton.getElement().addClassName(
                StartAnalysisWidgetResources.INSTANCE.gss().controll_header_button_visible());
    }

    private void scrollToLast() {
        if (numberOfStartAnalysisCards > 1) {
            currentScrollPosition = currentScrollPosition - (numberOfStartAnalysisCards - page - 1)
                    * SCROLL_OFFSET_STARTANALYSIS_CARDS;
            page = numberOfStartAnalysisCards - 1;
            startanalysis_card_container.getElement().getStyle().setLeft(currentScrollPosition, Unit.PCT);
            currentStartPanel.getElement().setInnerHTML(starts.get(page).raceName);
            notifyStartAnalysisPageChangeListener(page);
            rightButtonImage.setResource(StartAnalysisWidgetResources.INSTANCE.rightdisabled());
            rightButton.getElement().getStyle().setProperty("disabled", "true");
            leftButtonImage.setResource(StartAnalysisWidgetResources.INSTANCE.left());
            leftButton.getElement().getStyle().setProperty("disabled", "false");
        }
    }

    private void addStartAnalysisCard(final StartAnalysisDTO startAnalysisDTO) {
        logger.log(Level.INFO, "Adding Startanalysis Card");
        if (displaysCards == false) {
            displaysCards = true;
            currentStartPanel.getElement().setInnerHTML(startAnalysisDTO.raceName);
        }
        if (numberOfStartAnalysisCards > 0) {
            rightButtonImage.setResource(StartAnalysisWidgetResources.INSTANCE.right());
        }
        final StartAnalysisCard startlineAnalysisCard = new StartAnalysisCard(numberOfStartAnalysisCards
                * SCROLL_OFFSET_STARTANALYSIS_CARDS + MARGIN_LEFT_STARTANALYSIS_CARD, numberOfStartAnalysisCards,
                startAnalysisDTO, dashboardClientFactory.getSailingService(), errorReporter, raceMapResources);
        startanalysis_card_container.add(startlineAnalysisCard);
        startlineAnalysisCard.startAnalysisComponentPageChangedToIndexAndStartAnalysis(page, startAnalysisDTO);
        registerPageChangeListener(startlineAnalysisCard);
        numberOfStartAnalysisCards++;
    }

    private void removeAllStartAnalysisCards() {
        startanalysis_card_container.clear();
        numberOfStartAnalysisCards = 0;
        starts.clear();
    }

    private void showNotificationForNewStartAnalysis() {
        this.bottomNotification.show(BottomNotificationType.NEW_STARTANALYSIS_AVAILABLE);
    }

    public void registerPageChangeListener(StartAnalysisCard s) {
        pageChangeListener.add(s);
    }

    public void removeDataObserver(StartAnalysisPageChangeListener s) {
        pageChangeListener.remove(s);
    }

    public void notifyStartAnalysisPageChangeListener(int newPageIndex) {
        for (StartAnalysisPageChangeListener sO : pageChangeListener) {
            if (starts.get(newPageIndex) != null)
                sO.startAnalysisComponentPageChangedToIndexAndStartAnalysis(newPageIndex, starts.get(newPageIndex));
        }
    }

    private void addNewStartAnalysisCards(List<StartAnalysisDTO> startAnalysisDTOs) {
        for (int i = numberOfStartAnalysisCards; i <= startAnalysisDTOs.size() - 1; i++) {
            addStartAnalysisCard(startAnalysisDTOs.get(i));
            starts.add(startAnalysisDTOs.get(i));
        }
    }

    private String getCompetitorIdAsStringFromFirstDisplayedStartAnalysis() {
        String result = null;
        if (starts != null && starts.size() > 0) {
            CompetitorDTO competitor = starts.get(0).competitor;
            if (competitor != null)
                result = starts.get(0).competitor.getIdAsString();
        }
        return result;
    }

    private boolean displayedStartAnalysisForDifferentCompetitorToRequestedOne() {
        String competitorIDFromDispalyedStartAnalysisDTOs = getCompetitorIdAsStringFromFirstDisplayedStartAnalysis();
        if ((competitorIDFromDispalyedStartAnalysisDTOs != getCachedSelectedCompetitorId()) || (competitorIDFromDispalyedStartAnalysisDTOs == null && getCachedSelectedCompetitorId() != null)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void timeChanged(Date newTime, Date oldTime) {
        loadStartAnalysisDTOsForCompetitorID(getCachedSelectedCompetitorId());
    }

    @Override
    public void registerForDashboardFiveSecondsTimer(DashboardClientFactory dashboardClientFactory) {
        if (dashboardClientFactory != null) {
            dashboardClientFactory.getDashboardFiveSecondsTimer().addTimeListener(this);
        }
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
