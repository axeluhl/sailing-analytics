package com.sap.sailing.gwt.ui.datamining;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.sap.sailing.datamining.shared.AggregatorType;
import com.sap.sailing.datamining.shared.Components.GrouperType;
import com.sap.sailing.datamining.shared.QueryDefinition;
import com.sap.sailing.datamining.shared.SharedDimensions;
import com.sap.sailing.datamining.shared.SharedDimensions.GPSFix;
import com.sap.sailing.datamining.shared.SimpleQueryDefinition;
import com.sap.sailing.datamining.shared.StatisticType;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.dto.BoatClassDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.RaceDTO;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.RaceWithCompetitorsDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;

public class GPSFixQueryComponentsPanel extends AbstractQueryComponentsProvider<SharedDimensions.GPSFix> {

    private FlowPanel mainPanel;
    private ValueListBox<GrouperType> grouperTypeListBox;
    private TextArea customGrouperScriptTextBox;
    private HorizontalPanel dimensionsToGroupByPanel;
    private List<ValueListBox<SharedDimensions.GPSFix>> dimensionsToGroupByBoxes;
    private ValueListBox<StatisticAndAggregatorType> statisticsListBox;

    private Map<SharedDimensions.GPSFix, SelectionTable<SharedDimensions.GPSFix, ?, ?>> tablesMappedByDimension;
    private SelectionTable<SharedDimensions.GPSFix, RegattaDTO, String> regattaNameTable;
    private SelectionTable<SharedDimensions.GPSFix, BoatClassDTO, String> boatClassTable;
    private SelectionTable<SharedDimensions.GPSFix, RaceDTO, String> raceNameTable;
    private SelectionTable<SharedDimensions.GPSFix, Integer, Integer> legNumberTable;
    private SelectionTable<SharedDimensions.GPSFix, LegType, LegType> legTypeTable;
    private SelectionTable<SharedDimensions.GPSFix, CompetitorDTO, String> competitorNameTable;
    private SelectionTable<SharedDimensions.GPSFix, CompetitorDTO, String> competitorSailIDTable;
    private SelectionTable<SharedDimensions.GPSFix, String, String> nationalityTable;

    public GPSFixQueryComponentsPanel(StringMessages stringMessages, SailingServiceAsync sailingService,
            ErrorReporter errorReporter) {
        super(stringMessages, sailingService, errorReporter);
        mainPanel = new FlowPanel();
        dimensionsToGroupByBoxes = new ArrayList<ValueListBox<SharedDimensions.GPSFix>>();

        mainPanel.add(createSelectionTables());
        mainPanel.add(createFunctionsPanel());
        fillSelectionTables();
    }
    
    @Override
    public QueryDefinition<GPSFix> getQueryDefinition() {
        SimpleQueryDefinition<SharedDimensions.GPSFix> queryDTO = new SimpleQueryDefinition<SharedDimensions.GPSFix>(getGrouperType(), getStatisticType(), getAggregatorType());
        switch (queryDTO.getGrouperType()) {
        case Custom:
            queryDTO.setCustomGrouperScriptText(getCustomGrouperScriptText());
            break;
        case Dimensions:
        default:
            for (SharedDimensions.GPSFix dimension : getDimensionsToGroupBy()) {
                queryDTO.appendDimensionToGroupBy(dimension);
            }
            break;
        }
        for (Entry<SharedDimensions.GPSFix, Collection<?>> selectionEntry : getSelection().entrySet()) {
            queryDTO.setSelectionFor(selectionEntry.getKey(), selectionEntry.getValue());
        }
        return queryDTO;
    }

    private Map<SharedDimensions.GPSFix, Collection<?>> getSelection() {
        Map<SharedDimensions.GPSFix, Collection<?>> selection = new HashMap<SharedDimensions.GPSFix, Collection<?>>();
        for (SelectionTable<SharedDimensions.GPSFix, ?, ?> table : tablesMappedByDimension.values()) {
            Collection<?> specificSelection = table.getSelection();
            if (!specificSelection.isEmpty()) {
                selection.put(table.getDimension(), specificSelection);
            }
        }
        return selection;
    }

    private void clearSelection() {
        for (SelectionTable<SharedDimensions.GPSFix, ?, ?> table : tablesMappedByDimension.values()) {
            table.clearSelection();
        }
    }

    private GrouperType getGrouperType() {
        return grouperTypeListBox.getValue();
    }

    private String getCustomGrouperScriptText() {
        return getGrouperType() == GrouperType.Custom ? customGrouperScriptTextBox.getText() : "";
    }

    private Collection<SharedDimensions.GPSFix> getDimensionsToGroupBy() {
        Collection<SharedDimensions.GPSFix> dimensionsToGroupBy = new ArrayList<SharedDimensions.GPSFix>();
        if (getGrouperType() == GrouperType.Dimensions) {
            for (ValueListBox<SharedDimensions.GPSFix> dimensionToGroupByBox : dimensionsToGroupByBoxes) {
                if (dimensionToGroupByBox.getValue() != null) {
                    dimensionsToGroupBy.add(dimensionToGroupByBox.getValue());
                }
            }
        }
        return dimensionsToGroupBy;
    }

    private StatisticType getStatisticType() {
        return statisticsListBox.getValue().getStatisticType();
    }

    private AggregatorType getAggregatorType() {
        return statisticsListBox.getValue().getAggregatorType();
    }

    private void fillSelectionTables() {
        getSailingService().getRegattas(new AsyncCallback<List<RegattaDTO>>() {
            @Override
            public void onFailure(Throwable caught) {
                getErrorReporter().reportError("Error fetching the regattas from the server: " + caught.getMessage());
            }

            @Override
            public void onSuccess(List<RegattaDTO> regattas) {
                Set<BoatClassDTO> boatClasses = new HashSet<BoatClassDTO>();
                Set<RaceDTO> races = new HashSet<RaceDTO>();
                Set<CompetitorDTO> competitors = new HashSet<CompetitorDTO>();
                Set<String> nationalities = new HashSet<String>();
                for (RegattaDTO regatta : regattas) {
                    if (regatta != null) {
                        boatClasses.add(regatta.boatClass);
                        for (RaceWithCompetitorsDTO race : regatta.races) {
                            if (race != null) {
                                races.add(race);
                                for (CompetitorDTO competitor : race.competitors) {
                                    if (competitor != null) {
                                        competitors.add(competitor);
                                        nationalities.add(competitor.getThreeLetterIocCountryCode());
                                    }
                                }
                            }
                        }
                    }
                }

                List<RegattaDTO> sortedRegattas = new ArrayList<RegattaDTO>(regattas);
                Collections.sort(sortedRegattas, new Comparator<RegattaDTO>() {
                    @Override
                    public int compare(RegattaDTO o1, RegattaDTO o2) {
                        return o1.getName().compareTo(o2.getName());
                    }
                });

                List<BoatClassDTO> sortedBoatClasses = new ArrayList<BoatClassDTO>(boatClasses);
                Collections.sort(sortedBoatClasses, new Comparator<BoatClassDTO>() {
                    @Override
                    public int compare(BoatClassDTO o1, BoatClassDTO o2) {
                        return o1.getName().compareTo(o2.getName());
                    }
                });

                List<RaceDTO> sortedRaces = new ArrayList<RaceDTO>(races);
                Collections.sort(sortedRaces, new Comparator<RaceDTO>() {
                    @Override
                    public int compare(RaceDTO o1, RaceDTO o2) {
                        return o1.getName().compareTo(o2.getName());
                    }
                });

                regattaNameTable.setContent(sortedRegattas);
                boatClassTable.setContent(sortedBoatClasses);
                raceNameTable.setContent(sortedRaces);
                legNumberTable.setContent(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
                legTypeTable.setContent(Arrays.asList(LegType.values()));
                competitorNameTable.setContent(competitors);
                competitorSailIDTable.setContent(competitors);
                nationalityTable.setContent(nationalities);
            }
        });
    }

    private HorizontalPanel createFunctionsPanel() {
        HorizontalPanel functionsPanel = new HorizontalPanel();
        functionsPanel.setSpacing(5);

        Button clearSelectionButton = new Button(this.getStringMessages().clearSelection());
        clearSelectionButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                clearSelection();
            }
        });
        functionsPanel.add(clearSelectionButton);

        functionsPanel.add(createGroupByPanel());

        functionsPanel.add(new Label(getStringMessages().statisticToCalculate() + ": "));
        statisticsListBox = new ValueListBox<StatisticAndAggregatorType>(new Renderer<StatisticAndAggregatorType>() {
            @Override
            public String render(StatisticAndAggregatorType statisticAndAggregatorType) {
                if (statisticAndAggregatorType == null) {
                    return "";
                }
                return statisticAndAggregatorType.toString();
            }

            @Override
            public void render(StatisticAndAggregatorType statisticAndAggregatorType, Appendable appendable)
                    throws IOException {
                appendable.append(render(statisticAndAggregatorType));
            }
        });
        statisticsListBox.addValueChangeHandler(new ValueChangeHandler<StatisticAndAggregatorType>() {
            @Override
            public void onValueChange(ValueChangeEvent<StatisticAndAggregatorType> event) {
                notifyQueryComponentsChanged();
            }
        });
        List<StatisticAndAggregatorType> statistics = Arrays.asList(
                new StatisticAndAggregatorType(StatisticType.DataAmount, AggregatorType.Average),
                new StatisticAndAggregatorType(StatisticType.Speed, AggregatorType.Average));
        statisticsListBox.setValue(statistics.get(0), false);
        statisticsListBox.setAcceptableValues(statistics);
        functionsPanel.add(statisticsListBox);

        return functionsPanel;
    }

    private FlowPanel createGroupByPanel() {
        FlowPanel groupByPanel = new FlowPanel();

        HorizontalPanel selectGroupByPanel = new HorizontalPanel();
        selectGroupByPanel.setSpacing(5);
        selectGroupByPanel.add(new Label(getStringMessages().groupBy() + ": "));
        groupByPanel.add(selectGroupByPanel);

        grouperTypeListBox = new ValueListBox<GrouperType>(new Renderer<GrouperType>() {
            @Override
            public String render(GrouperType grouperType) {
                if (grouperType == null) {
                    return "";
                }
                return grouperType.toString();
            }

            @Override
            public void render(GrouperType grouperType, Appendable appendable) throws IOException {
                appendable.append(render(grouperType));
            }
        });
        grouperTypeListBox.setValue(GrouperType.Dimensions, false);
        grouperTypeListBox.setAcceptableValues(Arrays.asList(GrouperType.values()));
        selectGroupByPanel.add(grouperTypeListBox);

        final DeckPanel groupByOptionsPanel = new DeckPanel();
        groupByPanel.add(groupByOptionsPanel);
        grouperTypeListBox.addValueChangeHandler(new ValueChangeHandler<GrouperType>() {
            @Override
            public void onValueChange(ValueChangeEvent<GrouperType> event) {
                if (event.getValue() != null) {
                    switch (event.getValue()) {
                    case Custom:
                        groupByOptionsPanel.showWidget(1);
                        break;
                    case Dimensions:
                        groupByOptionsPanel.showWidget(0);
                        break;
                    }
                }
                notifyQueryComponentsChanged();
            }
        });

        dimensionsToGroupByPanel = new HorizontalPanel();
        dimensionsToGroupByPanel.setSpacing(5);
        groupByOptionsPanel.add(dimensionsToGroupByPanel);
        
        //Adding two dimension boxes, with regatta as first selected dimension
        ValueListBox<SharedDimensions.GPSFix> dimensionToGroupByBox = createDimensionToGroupByBox();
        dimensionToGroupByBox.setValue(SharedDimensions.GPSFix.RegattaName, false);
        dimensionsToGroupByPanel.add(dimensionToGroupByBox);
        dimensionsToGroupByBoxes.add(dimensionToGroupByBox);
        
        dimensionToGroupByBox = createDimensionToGroupByBox();
        dimensionsToGroupByPanel.add(dimensionToGroupByBox);
        dimensionsToGroupByBoxes.add(dimensionToGroupByBox);

        FlowPanel dynamicGroupByPanel = new FlowPanel();
        groupByOptionsPanel.add(dynamicGroupByPanel);
        dynamicGroupByPanel.add(new Label("public Object getValueToGroupByFrom(GPSFix data) {"));
        customGrouperScriptTextBox = new TextArea();
        customGrouperScriptTextBox.setCharacterWidth(100);
        customGrouperScriptTextBox.setVisibleLines(1);
        dynamicGroupByPanel.add(customGrouperScriptTextBox);
        dynamicGroupByPanel.add(new Label("}"));

        groupByOptionsPanel.showWidget(0);
        return groupByPanel;
    }

    private ValueListBox<SharedDimensions.GPSFix> createDimensionToGroupByBox() {
        ValueListBox<SharedDimensions.GPSFix> dimensionToGroupByBox = new ValueListBox<SharedDimensions.GPSFix>(
                new Renderer<SharedDimensions.GPSFix>() {
                    @Override
                    public String render(SharedDimensions.GPSFix gpsFixDimension) {
                        if (gpsFixDimension == null) {
                            return "";
                        }
                        return gpsFixDimension.toString();
                    }

                    @Override
                    public void render(SharedDimensions.GPSFix gpsFixDimension, Appendable appendable)
                            throws IOException {
                        appendable.append(render(gpsFixDimension));

                    }
                });
        dimensionToGroupByBox.addValueChangeHandler(new ValueChangeHandler<SharedDimensions.GPSFix>() {
            private boolean firstChange = true;

            @Override
            public void onValueChange(ValueChangeEvent<SharedDimensions.GPSFix> event) {
                if (firstChange && event.getValue() != null) {
                    ValueListBox<SharedDimensions.GPSFix> newBox = createDimensionToGroupByBox();
                    dimensionsToGroupByPanel.add(newBox);
                    dimensionsToGroupByBoxes.add(newBox);
                    firstChange = false;
                } else if (event.getValue() == null) {
                    dimensionsToGroupByPanel.remove((Widget) event.getSource());
                    dimensionsToGroupByBoxes.remove(event.getSource());
                }
                notifyQueryComponentsChanged();
            }
        });
        dimensionToGroupByBox.setAcceptableValues(Arrays.asList(SharedDimensions.GPSFix.values()));
        return dimensionToGroupByBox;
    }

    private Panel createSelectionTables() {
        HorizontalPanel tablesPanel = new HorizontalPanel();
        tablesPanel.setSpacing(5);
        ScrollPanel tablesScrollPanel = new ScrollPanel(tablesPanel);
        tablesScrollPanel.setHeight("21em");
        tablesMappedByDimension = new HashMap<SharedDimensions.GPSFix, SelectionTable<SharedDimensions.GPSFix, ?, ?>>();

        regattaNameTable = new SelectionTable<SharedDimensions.GPSFix, RegattaDTO, String>(getStringMessages()
                .regatta(), SharedDimensions.GPSFix.RegattaName) {
            @Override
            public String getValue(RegattaDTO regatta) {
                return regatta.getName();
            }
        };
        tablesPanel.add(regattaNameTable);
        tablesMappedByDimension.put(regattaNameTable.getDimension(), regattaNameTable);

        boatClassTable = new SelectionTable<SharedDimensions.GPSFix, BoatClassDTO, String>(getStringMessages()
                .boatClass(), SharedDimensions.GPSFix.BoatClassName) {
            @Override
            public String getValue(BoatClassDTO boatClass) {
                return boatClass.getName();
            }
        };
        tablesPanel.add(boatClassTable);
        tablesMappedByDimension.put(boatClassTable.getDimension(), boatClassTable);

        raceNameTable = new SelectionTable<SharedDimensions.GPSFix, RaceDTO, String>(getStringMessages().race(),
                SharedDimensions.GPSFix.RaceName) {
            @Override
            public String getValue(RaceDTO race) {
                return race.getName();
            }
        };
        tablesPanel.add(raceNameTable);
        tablesMappedByDimension.put(raceNameTable.getDimension(), raceNameTable);

        legNumberTable = new SelectionTable<SharedDimensions.GPSFix, Integer, Integer>(getStringMessages().legLabel(),
                SharedDimensions.GPSFix.LegNumber) {
            @Override
            public Integer getValue(Integer legNumber) {
                return legNumber;
            }
        };
        tablesPanel.add(legNumberTable);
        tablesMappedByDimension.put(legNumberTable.getDimension(), legNumberTable);

        legTypeTable = new SelectionTable<SharedDimensions.GPSFix, LegType, LegType>(getStringMessages().legType(),
                SharedDimensions.GPSFix.LegType) {
            @Override
            public LegType getValue(LegType legType) {
                return legType;
            }
        };
        tablesPanel.add(legTypeTable);
        tablesMappedByDimension.put(legTypeTable.getDimension(), legTypeTable);

        competitorNameTable = new SelectionTable<SharedDimensions.GPSFix, CompetitorDTO, String>(getStringMessages()
                .competitor(), SharedDimensions.GPSFix.CompetitorName) {
            @Override
            public String getValue(CompetitorDTO competitor) {
                return competitor.getName();
            }
        };
        tablesPanel.add(competitorNameTable);
        tablesMappedByDimension.put(competitorNameTable.getDimension(), competitorNameTable);

        competitorSailIDTable = new SelectionTable<SharedDimensions.GPSFix, CompetitorDTO, String>(getStringMessages()
                .sailID(), SharedDimensions.GPSFix.SailID) {
            @Override
            public String getValue(CompetitorDTO competitor) {
                return competitor.getSailID();
            }
        };
        tablesPanel.add(competitorSailIDTable);
        tablesMappedByDimension.put(competitorSailIDTable.getDimension(), competitorSailIDTable);

        nationalityTable = new SelectionTable<SharedDimensions.GPSFix, String, String>(getStringMessages()
                .nationality(), SharedDimensions.GPSFix.Nationality) {
            @Override
            public String getValue(String nationality) {
                return nationality;
            }
        };
        tablesPanel.add(nationalityTable);
        tablesMappedByDimension.put(nationalityTable.getDimension(), nationalityTable);

        for (SelectionTable<SharedDimensions.GPSFix, ?, ?> table : tablesMappedByDimension.values()) {
            table.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
                @Override
                public void onSelectionChange(SelectionChangeEvent event) {
                    notifyQueryComponentsChanged();
                }
            });
        }

        return tablesScrollPanel;
    }

    @Override
    public Widget getWidget() {
        return mainPanel;
    }

}
