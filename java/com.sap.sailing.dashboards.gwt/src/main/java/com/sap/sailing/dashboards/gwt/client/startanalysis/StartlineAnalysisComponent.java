package com.sap.sailing.dashboards.gwt.client.startanalysis;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.dashboards.gwt.client.RibDashboardDataRetriever;
import com.sap.sailing.dashboards.gwt.client.RibDashboardImageResources;
import com.sap.sailing.dashboards.gwt.client.bottomnotification.BottomNotification;
import com.sap.sailing.dashboards.gwt.client.bottomnotification.BottomNotificationClickListener;
import com.sap.sailing.dashboards.gwt.shared.dto.startanalysis.StartAnalysisDTO;

/**
 * The class contains an collection of {@link StartlineAnalysisCard}s that are displayed in horizontal aligned pages. It
 * shows only one {@link StartlineAnalysisCard} at a time and enables the user to switch between
 * {@link StartlineAnalysisCard}s with left and right buttons represented by {@link #left_focus_panel} and
 * {@link #right_focus_panel}. When a new {@link StartlineAnalysisCard} gets added to the component it shows a
 * {@link BottomNotification}.
 * 
 * @author Alexander Ries (D062114)
 *
 */
public class StartlineAnalysisComponent extends Composite implements HasWidgets, NewStartAnalysisListener,
        BottomNotificationClickListener {

    private static StartlineAnalysisComponentUiBinder uiBinder = GWT.create(StartlineAnalysisComponentUiBinder.class);

    interface StartlineAnalysisComponentUiBinder extends UiBinder<Widget, StartlineAnalysisComponent> {
    }

    interface StartlineAnalysis extends CssResource {
    }

    @UiField
    StartlineAnalysis style;

    @UiField
    HTMLPanel header;

    @UiField
    Image rightButton;

    @UiField
    Image leftButton;

    @UiField
    FocusPanel left_focus_panel;

    @UiField
    FocusPanel right_focus_panel;

    /**
     * Gets populated with new {@link StartlineAnalysisCard}s when the method
     * {@link #addStartAnalysisCard(StartAnalysisDTO)} gets called.
     * */
    @UiField
    HTMLPanel startanalysis_card_container;

    /**
     * The left property from the {@link #startanalysis_card_container}.
     * */
    private double currentScrollPosition = 0;

    /**
     * The page or {@link StartlineAnalysisCard} the {@link #startanalysis_card_container} displays. Begins with 0.
     * */
    private int page = 0;

    /**
     * Amount of {@link StartlineAnalysisCard}s in {@link #startanalysis_card_container}.
     * */
    private int numberOfStartAnalysisCards = 0;

    private boolean displaysCards;
    private List<StartAnalysisDTO> starts;
    private List<StartAnalysisPageChangeListener> pageChangeListener;
    private BottomNotification bottomNotification;

    /**
     * The class requires a {@link BottomNotification} to inform the user when a new {@link StartlineAnalysisCard} gets
     * added.
     * */
    public StartlineAnalysisComponent(BottomNotification bottomNotification) {
        this.bottomNotification = bottomNotification;
        pageChangeListener = new ArrayList<StartAnalysisPageChangeListener>();
        bottomNotification.addBottomNotificationClickListener(this);
        RibDashboardDataRetriever.getInstance().addNewStartAnalysisListener(this);
        starts = new ArrayList<StartAnalysisDTO>();
        initWidget(uiBinder.createAndBindUi(this));
        initLeftRightButtons();
    }

    private void initLeftRightButtons() {
        leftButton.setResource(RibDashboardImageResources.INSTANCE.leftdisabled());
        left_focus_panel.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                clickedArrowButton(false);
            }
        });
        rightButton.setResource(RibDashboardImageResources.INSTANCE.rightdisabled());
        right_focus_panel.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                clickedArrowButton(true);
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
                    currentScrollPosition = currentScrollPosition + 83;
                    page--;
                    startanalysis_card_container.getElement().getStyle().setLeft(currentScrollPosition, Unit.PCT);
                    notifyStartAnalysisPageChangeListener(page);
                    int displayPage = page + 1;
                    header.getElement().setInnerHTML("Start " + starts.get(displayPage - 1).raceName);
                    if (page != numberOfStartAnalysisCards - 1) {
                        rightButton.setResource(RibDashboardImageResources.INSTANCE.right());
                        rightButton.getElement().getStyle().setProperty("disabled", "false");
                    }
                    if (page == 0) {
                        leftButton.setResource(RibDashboardImageResources.INSTANCE.leftdisabled());
                        leftButton.getElement().getStyle().setProperty("disabled", "true");
                    }
                }
            } else {
                if (page != numberOfStartAnalysisCards - 1) {
                    currentScrollPosition -= 83;
                    page++;
                    startanalysis_card_container.getElement().getStyle().setLeft(currentScrollPosition, Unit.PCT);
                    notifyStartAnalysisPageChangeListener(page);
                    int displayPage = page + 1;
                    header.getElement().setInnerHTML("Start " + starts.get(displayPage - 1).raceName);
                    if (page >= 0) {
                        leftButton.setResource(RibDashboardImageResources.INSTANCE.left());
                        leftButton.getElement().getStyle().setProperty("disabled", "false");
                    }
                    if (page == numberOfStartAnalysisCards - 1) {
                        rightButton.setResource(RibDashboardImageResources.INSTANCE.rightdisabled());
                        rightButton.getElement().getStyle().setProperty("disabled", "true");
                    }
                }
            }
        }
    }

    private void scrollToLast() {
        if (numberOfStartAnalysisCards > 1) {
            currentScrollPosition = currentScrollPosition - (numberOfStartAnalysisCards - page - 1) * 83;
            page = numberOfStartAnalysisCards - 1;
            startanalysis_card_container.getElement().getStyle().setLeft(currentScrollPosition, Unit.PCT);
            header.getElement().setInnerHTML("Start " + starts.get(page).raceName);
            notifyStartAnalysisPageChangeListener(page);
            rightButton.setResource(RibDashboardImageResources.INSTANCE.rightdisabled());
            rightButton.getElement().getStyle().setProperty("disabled", "true");
            leftButton.setResource(RibDashboardImageResources.INSTANCE.left());
            leftButton.getElement().getStyle().setProperty("disabled", "false");
        }
    }

    private void addStartAnalysisCard(StartAnalysisDTO startAnalysisDTO) {

        if (displaysCards == false) {
            displaysCards = true;
            header.getElement().setInnerHTML("Start " + startAnalysisDTO.raceName);
        }
        if (numberOfStartAnalysisCards > 0) {
            rightButton.setResource(RibDashboardImageResources.INSTANCE.right());
        }
        StartlineAnalysisCard sac = new StartlineAnalysisCard(numberOfStartAnalysisCards * 83 + 10,
                numberOfStartAnalysisCards, startAnalysisDTO);
        startanalysis_card_container.add(sac);
        registerPageChangeListener(sac);
        numberOfStartAnalysisCards++;

    }

    public void registerPageChangeListener(StartAnalysisPageChangeListener s) {
        pageChangeListener.add(s);
    }

    public void removeDataObserver(StartAnalysisPageChangeListener s) {
        pageChangeListener.remove(s);
    }

    public void notifyStartAnalysisPageChangeListener(int newPageIndex) {
        for (StartAnalysisPageChangeListener sO : pageChangeListener) {
            sO.loadMapAndContent(newPageIndex, "someData");
        }
    }

    @Override
    public void addNewStartAnalysisCard(List<StartAnalysisDTO> startAnalysisDTOs) {
        this.bottomNotification.show("New Start Analysis available.", "#F0AB00", "#000000", true);
        for (int i = numberOfStartAnalysisCards; i <= startAnalysisDTOs.size() - 1; i++) {
            addStartAnalysisCard(startAnalysisDTOs.get(i));
            starts.add(startAnalysisDTOs.get(i));
        }
    }

    @Override
    public void bottomNotificationClicked() {
        // TODO Auto-generated method stub
        scrollToLast();
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
