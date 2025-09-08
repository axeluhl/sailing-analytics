package com.sap.sailing.gwt.home.desktop.places.user.profile.sailorprofiletab.details;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.UUID;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.home.communication.event.SimpleCompetitorWithIdDTO;
import com.sap.sailing.gwt.home.communication.user.profile.domain.ParticipatedEventDTO;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfileDTO;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfileEventsDTO;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfileNumericStatisticType;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfileStatisticDTO;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfileStatisticDTO.SingleEntry;
import com.sap.sailing.gwt.home.desktop.partials.desktopaccordion.DesktopAccordion;
import com.sap.sailing.gwt.home.desktop.partials.desktopaccordion.DesktopAccordion.AccordionExpansionListener;
import com.sap.sailing.gwt.home.desktop.partials.desktopaccordion.DesktopAccordionResources;
import com.sap.sailing.gwt.home.desktop.places.user.profile.sailorprofiletab.SailorProfileDesktopResources;
import com.sap.sailing.gwt.home.desktop.places.user.profile.sailorprofiletab.details.events.SailorProfileEventsTable;
import com.sap.sailing.gwt.home.desktop.places.user.profile.sailorprofiletab.details.statistic.SailorProfileStatisticTable;
import com.sap.sailing.gwt.home.shared.partials.editable.EditableSuggestedMultiSelectionCompetitor;
import com.sap.sailing.gwt.home.shared.partials.editable.InlineEditLabel;
import com.sap.sailing.gwt.home.shared.partials.listview.BoatClassListView;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.DataMiningQueryForSailorProfilesPersistor;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.EditSailorProfileDetailsView;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.SharedSailorProfileResources;
import com.sap.sailing.gwt.ui.client.FlagImageResolver;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.settings.SailingSettingsConstants;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;
import com.sap.sse.security.ui.client.UserService;


//added imports 
//import com.sap.sailing.gwt.home.desktop.places.user.profile.sailorprofiletab.details.analytics.SailorProfileAnalyticsTableAndCharts;
//import com.google.gwt.user.client.ui.HTMLPanel;
//import org.moxieapps.gwt.highcharts.client.Chart;
import org.moxieapps.gwt.highcharts.client.*;
//import com.sap.sse.gwt.resources.highcharts;
import com.sap.sailing.gwt.ui.datamining.presentation.ChartFactory;
import org.moxieapps.gwt.highcharts.client.ChartTitle;
import org.moxieapps.gwt.highcharts.client.ChartSubtitle;
import org.moxieapps.gwt.highcharts.client.Series;
import org.moxieapps.gwt.highcharts.client.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.moxieapps.gwt.highcharts.client.Chart;
import org.moxieapps.gwt.highcharts.client.Series;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.datamining.presentation.AbstractSailingResultsPresenter;
import com.sap.sailing.gwt.ui.datamining.presentation.ChartFactory;
import com.sap.sailing.polars.datamining.shared.PolarBackendData;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.common.util.NaturalComparator;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.dto.StatisticQueryDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.QueryResultDTO;
import com.sap.sse.datamining.ui.client.ChartToCsvExporter;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;
import com.sap.sse.gwt.client.shared.settings.ComponentContext;
import com.sap.sse.gwt.resources.Highcharts; //Zugriff auf Injection Methode
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.dom.client.Style.Unit;
/**
 * Implementation of {@link EditSailorProfileDetailsView} where users can view the details of a SailorProfile and edit
 * them. The data is loaded when the accordion is opened for the first time.
 */

public class ShowAndEditSailorProfile extends Composite implements EditSailorProfileDetailsView {
    
    private static SharedSailorProfileUiBinder uiBinder = GWT.create(SharedSailorProfileUiBinder.class);

    interface SharedSailorProfileUiBinder extends UiBinder<Widget, ShowAndEditSailorProfile> {
    }

    @UiField
    StringMessages i18n;

    @UiField(provided = true)
    EditableSuggestedMultiSelectionCompetitor competitorSelectionUi;
    @UiField
    InlineEditLabel titleUi;
    @UiField
    BoatClassListView boatClassesUi;

    @UiField
    DesktopAccordion accordionEventsUi;
    @UiField
    DesktopAccordion accordionStatisticsUi;
//    @UiField
//    DesktopAccordion accordionPolarDiagramUi;
    @UiField
    DesktopAccordion accordionAnalyticsUi;

    Label eventsEmptyUi;

    private final EditSailorProfileDetailsView.Presenter presenter;

    private FlagImageResolver flagImageResolver;
    
    private UserService userService;

    public ShowAndEditSailorProfile(EditSailorProfileDetailsView.Presenter presenter,
            FlagImageResolver flagImageResolver, SailorProfileDetailsView parent, UserService userService) {
        this.presenter = presenter;
        this.flagImageResolver = flagImageResolver;
        this.userService = userService;
        presenter.getDataProvider().setView(this);
        competitorSelectionUi = new EditableSuggestedMultiSelectionCompetitor(presenter.getCompetitorPresenter(),
                flagImageResolver);
        initWidget(uiBinder.createAndBindUi(this));
        SharedSailorProfileResources.INSTANCE.css().ensureInjected();
        SailorProfileDesktopResources.INSTANCE.css().ensureInjected();
        boatClassesUi.setText(i18n.boatClasses());
//        accordionPolarDiagramUi.setVisible(false);
    }

    private void setupTitleChangeListener(final UUID uuid) {
        // setup title change handler
        titleUi.clearTextChangeHandlers();
        titleUi.addTextChangeHandler((text) -> {
            presenter.getDataProvider().updateTitle(uuid, text);
        });
    }

    @Override
    public void setEntry(SailorProfileDTO entry) {
        competitorSelectionUi.setSelectedItems(entry.getCompetitors());
        titleUi.setText(entry.getName());
        boatClassesUi.setEmptyMessage(StringMessages.INSTANCE.pleaseSelectCompetitorFirst());
        boatClassesUi.setItems(entry.getBoatclasses());
        accordionEventsUi.clear();

        // Get events asynchronously
        presenter.getDataProvider().getEvents(entry.getKey(), new AsyncCallback<SailorProfileEventsDTO>() {
            @Override
            public void onFailure(Throwable caught) {
                GWT.log(caught.getMessage(), caught);
            }

            @Override
            public void onSuccess(SailorProfileEventsDTO result) {
                appendEmptyMessageIfNecessary(result);
                for (ParticipatedEventDTO dto : result.getParticipatedEvents()) {
                    SailorProfileEventsTable table = new SailorProfileEventsTable(flagImageResolver,
                            presenter.getPlaceController(), dto);
                    accordionEventsUi.addWidget(table);
                }
            }

            /** adds empty widget */
            private void appendEmptyMessageIfNecessary(SailorProfileEventsDTO result) {
                if (Util.isEmpty(result.getParticipatedEvents())) {
                    createEventsEmptyUiIfNecessary();
                    accordionEventsUi.addWidget(eventsEmptyUi);
                } else if (eventsEmptyUi != null && eventsEmptyUi.isAttached()) {
                    eventsEmptyUi.removeFromParent();
                }
            }

            /** creates empty widget */
            private void createEventsEmptyUiIfNecessary() {
                if (eventsEmptyUi == null) {
                    eventsEmptyUi = new Label(StringMessages.INSTANCE.noEventsFoundForCompetitors());
                    DesktopAccordionResources.INSTANCE.css().ensureInjected();
                    eventsEmptyUi.addStyleName(DesktopAccordionResources.INSTANCE.css().accordionEmptyMessage());
                }
            }
        });

        accordionStatisticsUi.clear();
        
        setupTables(entry);
        
        accordionAnalyticsUi.clear();
        Label testLabel = new Label("Analytics funktioniert!");
        accordionAnalyticsUi.addWidget(testLabel);
        createPolarChart(entry);
//        setupAnalyticsTablesAndCharts(entry);
        setupTitleChangeListener(entry.getKey());
    }

    /** create tables for statistic types */
    private void setupTables(SailorProfileDTO entry) {
        DataMiningQueryForSailorProfilesPersistor.removeDMQueriesFromLocalStorage(userService.getStorage());
        for (SailorProfileNumericStatisticType type : SailorProfileNumericStatisticType.values()) {
            SailorProfileStatisticTable table = new SailorProfileStatisticTable(flagImageResolver, type, i18n,
                    userService);
            accordionStatisticsUi.addWidget(table);
            if (accordionStatisticsUi.isExpanded()) {
                updateStatistic(entry, type, table);
            } else {
                // load the statistic data when the accordion is expanded for the first time
                accordionStatisticsUi.addAccordionListener(new AccordionExpansionListener() {
                    @Override
                    public void onExpansion(boolean expanded) {
                        if (expanded) {
                            updateStatistic(entry, type, table);
                        }
                    }

                });
            }
        }
    }

    /** retrieve and update the statistic for a given type of a sailor profile entry */
    private void updateStatistic(SailorProfileDTO entry, SailorProfileNumericStatisticType type,
            SailorProfileStatisticTable table) {
        presenter.getDataProvider().getStatisticFor(entry.getKey(), type,
                new AsyncCallback<SailorProfileStatisticDTO>() {

                    private String navigationUrl;

                    @Override
                    public void onFailure(Throwable caught) {
                        Notification.notify(i18n.couldNotDetermineStatistic(), NotificationType.WARNING);
                    }

                    @Override
                    public void onSuccess(SailorProfileStatisticDTO answer) {
                        ArrayList<Pair<SimpleCompetitorWithIdDTO, SingleEntry>> data = new ArrayList<>();
                        for (Entry<SimpleCompetitorWithIdDTO, ArrayList<SingleEntry>> entry : answer.getResult()
                                .entrySet()) {
                            for (SingleEntry value : entry.getValue()) {
                                data.add(new Pair<SimpleCompetitorWithIdDTO, SingleEntry>(entry.getKey(), value));
                            }
                        }
                        if (answer.getDataMiningQuery() != null) {
                            final String identifier = SailingSettingsConstants.DATAMINING_QUERY_PREFIX
                                    + UUID.randomUUID().toString();
                            navigationUrl = "DataMining.html?q=" + identifier;
                            table.setNavigationTarget(this::func);
                            DataMiningQueryForSailorProfilesPersistor.writeDMQueriesToLocalStorageIfPossible(answer,
                                    identifier, userService.getStorage());
                        }
                        table.setData(data);
                    }

                    String func(Object o) {
                        return navigationUrl;
                    }
                });
    }
    
//    // Implementation of setupAnalyticsTablesAndChart() with ChartFactory
//    private void setupAnalyticsTablesAndCharts(SailorProfileDTO entry) {
//        try {
//            // Highcharts-More laden für Polar Charts
//            Highcharts.ensureInjectedWithMore();
//    
//            Chart polarChart = ChartFactory.createPolarChart();
//                polarChart.setType(Series.Type.LINE); // oder AREA, COLUMN
//                polarChart.setChartTitleText("Sailing Performance Chart");
//                polarChart.setPolar(true);
//                polarChart.getYAxis().setMin(0);
//                polarChart.setWidth("100%");
//                polarChart.setHeight("400px");     
//                polarChart.setOption("/pane/startAngle", 0);  // Startwinkel
//
//                // X-Axis = Winkel (0-360° oder -180 bis +180°)
//                polarChart.getXAxis()
//                    .setMin(-179)
//                    .setMax(180)
//                    .setTickInterval(45);  // Alle 45°
//      
//                // Y-Axis = Radius (Geschwindigkeit, Distanz, etc.)
//                polarChart.getYAxis()
//                    .setMin(0);
// 
//            Series series = polarChart.createSeries()
//              .setName("Boat Speed")
//              .addPoint(-180, 3.5)
//              .addPoint(-135, 5.2)
//              .addPoint(-90, 6.8)
//              .addPoint(-45, 7.1)
//              .addPoint(0, 4.2)
//              .addPoint(45, 7.3)
//              .addPoint(90, 6.9)
//              .addPoint(135, 5.1)
//              .addPoint(180, 3.4);              
//            polarChart.addSeries(series);  
//            
//            // Chart in Panel packen
//            SimpleLayoutPanel chartWrapper = new SimpleLayoutPanel();
//            chartWrapper.add(polarChart);
//            chartWrapper.setHeight("400px");
//              
//            // Zum Accordion hinzufügen
//            accordionAnalyticsUi.addWidget(chartWrapper);
//
//            Label successLabel = new Label("Chart erfolgreich erstellt!");
//            accordionPolarDiagramUi.addWidget(successLabel);
//        } 
//        catch (Exception e) {
//              Label errorLabel = new Label("Fehler: " + e.getMessage());
//              accordionPolarDiagramUi.addWidget(errorLabel);
//              GWT.log("Chart error", e);
//        }
//    }
    
    
    
//                // Optional: Lazy Loading
//                if (accordionAnalyticsUi.isExpanded()) {
//                    loadPolarData(polarChart);
//                } else {
//                    accordionAnalyticsUi.addAccordionListener(new AccordionExpansionListener() {
//                        @Override
//                        public void onExpansion(boolean expanded) {
//                            if (expanded) {
//                                loadPolarData(polarChart);
//                            }
//                        }
//                    });
//                }
//            }
      //in polarChart umbenennen? Startwinkel o.ä.? oben schauen gleich einmal nur polarChart ture und das ensure injected raus
    private void createPolarChart(SailorProfileDTO entry) {
        try {
            // Highcharts-More laden für Polar Charts
            Highcharts.ensureInjectedWithMore();
                                
            // Einfaches Line Chart erstellen
            Chart chart = new Chart()
                .setType(Series.Type.LINE)
                .setChartTitleText("Sailing Performance Chart")
                .setPolar (true)
                .setHeight100()
                .setWidth100();
                        
            // Achsen-Titel setzen
                        
            // Sample-Daten hinzufügen
            Series series = chart.createSeries()
                .setName("Boat Speed")
                .addPoint(-180, 3.5)
                .addPoint(-135, 5.2)
                .addPoint(-90, 6.8)
                .addPoint(-45, 7.1)
                .addPoint(0, 4.2)
                .addPoint(45, 7.3)
                .addPoint(90, 6.9)
                .addPoint(135, 5.1)
                .addPoint(180, 3.4); 
            chart.addSeries(series);
                        
            // Chart in Panel packen
            SimpleLayoutPanel chartWrapper = new SimpleLayoutPanel();
            chartWrapper.add(chart);
            chartWrapper.setHeight("400px");
            
            // Zum Accordion hinzufügen
            accordionAnalyticsUi.addWidget(chartWrapper);
            
            // Erfolgs-Label
            Label successLabel = new Label("Chart erfolgreich erstellt!");
            accordionStatisticsUi.addWidget(successLabel);
        } 
        catch (Exception e) {
            Label errorLabel = new Label("Fehler: " + e.getMessage());
            accordionStatisticsUi.addWidget(errorLabel);
            GWT.log("Chart error", e);
        }
    }
}

    
//polarChartTest = ChartFactory.createPolarChart();
//polarChartTest.getYAxis().setMin(0);
//polarChartWrapperPanel = new SimpleLayoutPanel() {
//  @Override
//  public void onResize() {
//      polarChartTest.setSizeToMatchContainer();
//      polarChartTest.redraw();
//  }
//};

//polarChartWrapperPanel.add(polarChartTest);
//dockLayoutPanel = new DockLayoutPanel(Unit.PCT);
//dockLayoutPanel.addWest(polarChartWrapperPanel, 40);
//accordionAnalyticsUi.setVisible(true);
//setupAnalyticsTablesAndCharts(entry); 
//polarChartWrapperPanel.setHeight("400px");
//accordionAnalyticsUi.addWidget(polarChartWrapperPanel);

//Chart polarChart = ChartFactory.createPolarChart();
//SimpleLayoutPanel wrapper = new SimpleLayoutPanel();
//wrapper.add(polarChart);
//wrapper.setHeight("400px"); 
//accordionAnalyticsUi.addWidget(wrapper);