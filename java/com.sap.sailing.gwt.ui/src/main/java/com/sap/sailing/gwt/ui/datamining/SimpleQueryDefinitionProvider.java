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
import com.sap.sailing.datamining.shared.SharedDimensions;
import com.sap.sailing.datamining.shared.QueryDefinition;
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

public class SimpleQueryDefinitionProvider extends AbstractQueryDefinitionProvider {

    private FlowPanel mainPanel;
    private ValueListBox<GrouperType> grouperTypeListBox;
    private TextArea customGrouperScriptTextBox;
    private HorizontalPanel dimensionsToGroupByPanel;
    private List<ValueListBox<SharedDimensions>> dimensionsToGroupByBoxes;
    private ValueListBox<StatisticAndAggregatorType> statisticsListBox;

    private Map<SharedDimensions, SelectionTable<?, ?>> tablesMappedByDimension;
    private SelectionTable<RegattaDTO, String> regattaNameTable;
    private SelectionTable<BoatClassDTO, String> boatClassTable;
    private SelectionTable<RaceDTO, String> raceNameTable;
    private SelectionTable<Integer, Integer> legNumberTable;
    private SelectionTable<LegType, LegType> legTypeTable;
    private SelectionTable<CompetitorDTO, String> competitorNameTable;
    private SelectionTable<CompetitorDTO, String> competitorSailIDTable;
    private SelectionTable<String, String> nationalityTable;

    public SimpleQueryDefinitionProvider(StringMessages stringMessages, SailingServiceAsync sailingService,
            ErrorReporter errorReporter) {
        super(stringMessages, sailingService, errorReporter);
        mainPanel = new FlowPanel();
        dimensionsToGroupByBoxes = new ArrayList<ValueListBox<SharedDimensions>>();

        mainPanel.add(createSelectionTables());
        mainPanel.add(createFunctionsPanel());
        fillSelectionTables();
    }
    
    @Override
    public QueryDefinition getQueryDefinition() {
        SimpleQueryDefinition queryDTO = new SimpleQueryDefinition(getGrouperType(), getStatisticType(), getAggregatorType());
        switch (queryDTO.getGrouperType()) {
        case Custom:
            queryDTO.setCustomGrouperScriptText(getCustomGrouperScriptText());
            break;
        case Dimensions:
        default:
            for (SharedDimensions dimension : getDimensionsToGroupBy()) {
                queryDTO.appendDimensionToGroupBy(dimension);
            }
            break;
        }
        for (Entry<SharedDimensions, Collection<?>> selectionEntry : getSelection().entrySet()) {
            queryDTO.setSelectionFor(selectionEntry.getKey(), selectionEntry.getValue());
        }
        return queryDTO;
    }

    private Map<SharedDimensions, Collection<?>> getSelection() {
        Map<SharedDimensions, Collection<?>> selection = new HashMap<SharedDimensions, Collection<?>>();
        for (SelectionTable<?, ?> table : tablesMappedByDimension.values()) {
            Collection<?> specificSelection = table.getSelection();
            if (!specificSelection.isEmpty()) {
                selection.put(table.getDimension(), specificSelection);
            }
        }
        return selection;
    }

    private void clearSelection() {
        for (SelectionTable<?, ?> table : tablesMappedByDimension.values()) {
            table.clearSelection();
        }
    }

    private GrouperType getGrouperType() {
        return grouperTypeListBox.getValue();
    }

    private String getCustomGrouperScriptText() {
        return getGrouperType() == GrouperType.Custom ? customGrouperScriptTextBox.getText() : "";
    }

    private Collection<SharedDimensions> getDimensionsToGroupBy() {
        Collection<SharedDimensions> dimensionsToGroupBy = new ArrayList<SharedDimensions>();
        if (getGrouperType() == GrouperType.Dimensions) {
            for (ValueListBox<SharedDimensions> dimensionToGroupByBox : dimensionsToGroupByBoxes) {
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
                notifyQueryDefinitionChanged();
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
                notifyQueryDefinitionChanged();
            }
        });

        dimensionsToGroupByPanel = new HorizontalPanel();
        dimensionsToGroupByPanel.setSpacing(5);
        groupByOptionsPanel.add(dimensionsToGroupByPanel);
        
        //Adding two dimension boxes, with regatta as first selected dimension
        ValueListBox<SharedDimensions> dimensionToGroupByBox = createDimensionToGroupByBox();
        dimensionToGroupByBox.setValue(SharedDimensions.RegattaName, false);
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

    private ValueListBox<SharedDimensions> createDimensionToGroupByBox() {
        ValueListBox<SharedDimensions> dimensionToGroupByBox = new ValueListBox<SharedDimensions>(
                new Renderer<SharedDimensions>() {
                    @Override
                    public String render(SharedDimensions gpsFixDimension) {
                        if (gpsFixDimension == null) {
                            return "";
                        }
                        return gpsFixDimension.toString();
                    }

                    @Override
                    public void render(SharedDimensions gpsFixDimension, Appendable appendable)
                            throws IOException {
                        appendable.append(render(gpsFixDimension));

                    }
                });
        dimensionToGroupByBox.addValueChangeHandler(new ValueChangeHandler<SharedDimensions>() {
            private boolean firstChange = true;

            @Override
            public void onValueChange(ValueChangeEvent<SharedDimensions> event) {
                if (firstChange && event.getValue() != null) {
                    ValueListBox<SharedDimensions> newBox = createDimensionToGroupByBox();
                    dimensionsToGroupByPanel.add(newBox);
                    dimensionsToGroupByBoxes.add(newBox);
                    firstChange = false;
                } else if (event.getValue() == null) {
                    dimensionsToGroupByPanel.remove((Widget) event.getSource());
                    dimensionsToGroupByBoxes.remove(event.getSource());
                }
                notifyQueryDefinitionChanged();
            }
        });
        dimensionToGroupByBox.setAcceptableValues(Arrays.asList(SharedDimensions.values()));
        return dimensionToGroupByBox;
    }

    private Panel createSelectionTables() {
        HorizontalPanel tablesPanel = new HorizontalPanel();
        tablesPanel.setSpacing(5);
        ScrollPanel tablesScrollPanel = new ScrollPanel(tablesPanel);
        tablesScrollPanel.setHeight("21em");
        tablesMappedByDimension = new HashMap<SharedDimensions, SelectionTable<?, ?>>();

        regattaNameTable = new SelectionTable<RegattaDTO, String>(getStringMessages()
                .regatta(), SharedDimensions.RegattaName) {
            @Override
            public String getValue(RegattaDTO regatta) {
                return regatta.getName();
            }
        };
        tablesPanel.add(regattaNameTable);
        tablesMappedByDimension.put(regattaNameTable.getDimension(), regattaNameTable);

        boatClassTable = new SelectionTable<BoatClassDTO, String>(getStringMessages()
                .boatClass(), SharedDimensions.BoatClassName) {
            @Override
            public String getValue(BoatClassDTO boatClass) {
                return boatClass.getName();
            }
        };
        tablesPanel.add(boatClassTable);
        tablesMappedByDimension.put(boatClassTable.getDimension(), boatClassTable);

        raceNameTable = new SelectionTable<RaceDTO, String>(getStringMessages().race(),
                SharedDimensions.RaceName) {
            @Override
            public String getValue(RaceDTO race) {
                return race.getName();
            }
        };
        tablesPanel.add(raceNameTable);
        tablesMappedByDimension.put(raceNameTable.getDimension(), raceNameTable);

        legNumberTable = new SelectionTable<Integer, Integer>(getStringMessages().legLabel(),
                SharedDimensions.LegNumber) {
            @Override
            public Integer getValue(Integer legNumber) {
                return legNumber;
            }
        };
        tablesPanel.add(legNumberTable);
        tablesMappedByDimension.put(legNumberTable.getDimension(), legNumberTable);

        legTypeTable = new SelectionTable<LegType, LegType>(getStringMessages().legType(),
                SharedDimensions.LegType) {
            @Override
            public LegType getValue(LegType legType) {
                return legType;
            }
        };
        tablesPanel.add(legTypeTable);
        tablesMappedByDimension.put(legTypeTable.getDimension(), legTypeTable);

        competitorNameTable = new SelectionTable<CompetitorDTO, String>(getStringMessages()
                .competitor(), SharedDimensions.CompetitorName) {
            @Override
            public String getValue(CompetitorDTO competitor) {
                return competitor.getName();
            }
        };
        tablesPanel.add(competitorNameTable);
        tablesMappedByDimension.put(competitorNameTable.getDimension(), competitorNameTable);

        competitorSailIDTable = new SelectionTable<CompetitorDTO, String>(getStringMessages()
                .sailID(), SharedDimensions.SailID) {
            @Override
            public String getValue(CompetitorDTO competitor) {
                return competitor.getSailID();
            }
        };
        tablesPanel.add(competitorSailIDTable);
        tablesMappedByDimension.put(competitorSailIDTable.getDimension(), competitorSailIDTable);

        nationalityTable = new SelectionTable<String, String>(getStringMessages()
                .nationality(), SharedDimensions.Nationality) {
            @Override
            public String getValue(String nationality) {
                return nationality;
            }
        };
        tablesPanel.add(nationalityTable);
        tablesMappedByDimension.put(nationalityTable.getDimension(), nationalityTable);

        for (SelectionTable<?, ?> table : tablesMappedByDimension.values()) {
            table.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
                @Override
                public void onSelectionChange(SelectionChangeEvent event) {
                    notifyQueryDefinitionChanged();
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
