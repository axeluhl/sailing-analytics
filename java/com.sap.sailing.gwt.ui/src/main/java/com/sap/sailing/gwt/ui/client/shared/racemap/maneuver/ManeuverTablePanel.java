package com.sap.sailing.gwt.ui.client.shared.racemap.maneuver;

import java.util.ArrayList;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.TextHeader;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.InvertibleComparator;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.SortingOrder;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.impl.InvertibleComparatorAdapter;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionChangeListener;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionProvider;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.controls.SortableColumn;
import com.sap.sse.common.Util;
import com.sap.sse.common.filter.Filter;
import com.sap.sse.common.filter.FilterSet;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.shared.components.AbstractCompositeComponent;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.SettingsDialog;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;
import com.sap.sse.gwt.client.shared.settings.ComponentContext;

public class ManeuverTablePanel extends AbstractCompositeComponent<ManeuverTableSettings>
        implements CompetitorSelectionChangeListener {
    private static class AnchorCell extends AbstractCell<SafeHtml> {
        @Override
        public void render(com.google.gwt.cell.client.Cell.Context context, SafeHtml safeHtml, SafeHtmlBuilder sb) {
            sb.append(safeHtml);
        }
    }

    private final SailingServiceAsync sailingService;
    private RegattaAndRaceIdentifier raceIdentifier;
    private final ErrorReporter errorReporter;
    private final StringMessages stringMessages;
    private final CompetitorSelectionProvider competitorSelectionModel;

    private Label selectCompetitorLabel = new Label();
    private ManeuverTableSettings settings;
    private CellTable<SingleManeuverDTO> maneuverCellTable;

    public ManeuverTablePanel(Component<?> parent, ComponentContext<?> context,
            final SailingServiceAsync sailingService, final RegattaAndRaceIdentifier raceIdentifier,
            final StringMessages stringMessages, final CompetitorSelectionProvider competitorSelectionModel,
            final ErrorReporter errorReporter, final Timer timer, ManeuverTableSettings initialSettings) {
        super(parent, context);
        this.settings = initialSettings;
        this.sailingService = sailingService;
        this.raceIdentifier = raceIdentifier;
        this.errorReporter = errorReporter;
        this.competitorSelectionModel = competitorSelectionModel;
        this.stringMessages = stringMessages;
        competitorSelectionModel.addCompetitorSelectionChangeListener(this);

        AbsolutePanel rootPanel = new AbsolutePanel();
        HorizontalPanel tableAndButtons = new HorizontalPanel();
        rootPanel.add(tableAndButtons, 0, 0);
        tableAndButtons.setSpacing(3);
        VerticalPanel buttonPanel = new VerticalPanel();
        buttonPanel.setSpacing(3);
        tableAndButtons.add(buttonPanel);
        buttonPanel.add(selectCompetitorLabel);
        
        maneuverCellTable = new CellTable<>();
        
        SortableColumn<SingleManeuverDTO, String> competitorColumn = new SortableColumn<SingleManeuverDTO, String>(new TextCell(), SortingOrder.ASCENDING) {

            @Override
            public InvertibleComparator<SingleManeuverDTO> getComparator() {
                return new InvertibleComparatorAdapter<SingleManeuverDTO>(){
                    @Override
                    public int compare(SingleManeuverDTO o1, SingleManeuverDTO o2) {
                        return o1.competitor.getName().compareTo(o2.competitor.getName());
                    }
                };
            }

            @Override
            public Header<?> getHeader() {
                return new TextHeader("i18n Competitor");
            }

            @Override
            public String getValue(SingleManeuverDTO object) {
                return object.competitor.getName();
            }
        };

        maneuverCellTable.addColumn(competitorColumn,competitorColumn.getHeader());
        maneuverCellTable.setVisible(false);
        rootPanel.add(maneuverCellTable);
        rootPanel.add(SettingsDialog.createSettingsButton(this, stringMessages));
        initWidget(rootPanel);
        setVisible(false);
        refresh();
    }
    

    public void refresh(){
        ArrayList<SingleManeuverDTO> test = new ArrayList<>();
        maneuverCellTable.setRowData(test);
    }
    
    @Override
    public void setVisible(boolean visible) {
        processCompetitorSelectionChange(visible);
        super.setVisible(visible);
    }

    @Override
    public void addedToSelection(CompetitorDTO competitor) {
        processCompetitorSelectionChange(isVisible());
    }

    @Override
    public void removedFromSelection(CompetitorDTO competitor) {
        processCompetitorSelectionChange(isVisible());
    }
    private void processCompetitorSelectionChange(boolean visible) {
        if (visible && Util.size(competitorSelectionModel.getSelectedCompetitors()) > 0) {
            selectCompetitorLabel.setText("");
            maneuverCellTable.setVisible(true);
        } else {
            selectCompetitorLabel.setText(stringMessages.selectCompetitor());
            maneuverCellTable.setVisible(false);
        }
    }

    @Override
    public Widget getEntryWidget() {
        return this;
    }

    @Override
    public boolean hasSettings() {
        return true;
    }

    @Override
    public String getLocalizedShortName() {
        return null;
    }

    @Override
    public String getDependentCssClassName() {
        return null;
    }

    @Override
    public void filterChanged(FilterSet<CompetitorDTO, ? extends Filter<CompetitorDTO>> oldFilterSet,
            FilterSet<CompetitorDTO, ? extends Filter<CompetitorDTO>> newFilterSet) {
    }

    @Override
    public void competitorsListChanged(Iterable<CompetitorDTO> competitors) {
    }

    @Override
    public void filteredCompetitorsListChanged(Iterable<CompetitorDTO> filteredCompetitors) {
    }

    @Override
    public ManeuverTableSettings getSettings() {
        return settings;
    }

    @Override
    public String getId() {
        return ManeuverTableLifecycle.ID;
    }

    @Override
    public SettingsDialogComponent<ManeuverTableSettings> getSettingsDialogComponent(
            ManeuverTableSettings useTheseSettings) {
        return new ManeuverTableSettingsDialogComponent(settings, stringMessages);
    }

    @Override
    public void updateSettings(ManeuverTableSettings newSettings) {
        settings = newSettings;
    }

}
