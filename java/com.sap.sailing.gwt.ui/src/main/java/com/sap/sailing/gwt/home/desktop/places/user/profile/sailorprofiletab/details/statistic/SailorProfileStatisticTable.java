package com.sap.sailing.gwt.home.desktop.places.user.profile.sailorprofiletab.details.statistic;

import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sailing.gwt.common.theme.component.celltable.DesignedCellTableResources;
import com.sap.sailing.gwt.home.communication.event.SimpleCompetitorWithIdDTO;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfileNumericStatisticType;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfileNumericStatisticType.StatisticType;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfileStatisticDTO.SingleEntry;
import com.sap.sailing.gwt.home.desktop.places.user.profile.sailorprofiletab.SailorProfileDesktopResources;
import com.sap.sailing.gwt.home.desktop.places.user.profile.sailorprofiletab.details.ShowAndEditSailorProfile;
import com.sap.sailing.gwt.home.desktop.places.user.profile.sailorprofiletab.details.events.CompetitorWithoutClubnameItemDescription;
import com.sap.sailing.gwt.home.desktop.places.user.profile.sailorprofiletab.details.events.NavigatorColumn;
import com.sap.sailing.gwt.home.shared.resources.SharedHomeResources;
import com.sap.sailing.gwt.settings.client.EntryPointWithSettingsLinkFactory;
import com.sap.sailing.gwt.settings.client.raceboard.RaceBoardPerspectiveOwnSettings;
import com.sap.sailing.gwt.settings.client.raceboard.RaceboardContextDefinition;
import com.sap.sailing.gwt.ui.client.FlagImageResolver;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMapLifecycle;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMapSettings;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.impl.InvertibleComparatorAdapter;
import com.sap.sse.common.impl.MillisecondsDurationImpl;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.celltable.SortedCellTable;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveCompositeSettings;
import com.sap.sse.security.ui.client.UserService;

/**
 * This element displays a given statistic for each sailor in a sailor profile in the {@link ShowAndEditSailorProfile}
 * view.
 */
public class SailorProfileStatisticTable extends Composite {

    interface MyBinder extends UiBinder<Widget, SailorProfileStatisticTable> {
    }

    private static MyBinder uiBinder = GWT.create(MyBinder.class);

    @UiField(provided = true)
    final SortedCellTable<Pair<SimpleCompetitorWithIdDTO, SingleEntry>> sailorProfilesTable = new SortedCellTable<>(0,
            DesignedCellTableResources.INSTANCE);

    @UiField
    SpanElement titleUi;

    @UiField
    Image titleIconUi;

    @UiField
    Anchor anchor;

    private final Image titleHeaderRight;

    private final FlagImageResolver flagImageResolver;

    private final SailorProfileNumericStatisticType type;

    private final StringMessages stringMessages;

    private Function<Pair<SimpleCompetitorWithIdDTO, SingleEntry>, String> navigationTarget;

    private String dataMiningUrl;

    private UserService userService;

    public SailorProfileStatisticTable(FlagImageResolver flagImageResolver, SailorProfileNumericStatisticType type,
            StringMessages stringMessages, UserService userService) {
        this.userService = userService;
        this.flagImageResolver = flagImageResolver;
        this.type = type;
        this.stringMessages = stringMessages;

        SailorProfileDesktopResources.INSTANCE.css().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
        titleHeaderRight = new Image();
        titleHeaderRight.addStyleName(SailorProfileDesktopResources.INSTANCE.css().statisticsTableHeaderRight());
        setupTable();
        titleUi.setInnerText(SailorProfileNumericStatisticTypeFormatter.getDisplayName(type, stringMessages));
        titleIconUi.setUrl(SailorProfileNumericStatisticTypeFormatter.getIcon(type));
    }

    /**
     * @param isAverage
     *            false -> {@link #navigationTarget} is called when the arrow in each row is clicked <br/>
     *            true -> {@link #navigationTarget} is called when the arrow in the table header is clicked
     * @param navigationTarget
     */
    private void setNavigationTarget(Function<Pair<SimpleCompetitorWithIdDTO, SingleEntry>, String> navigationTarget,
            boolean isAverage) {
        if (isAverage) {
            setNavigationTarget(navigationTarget);
            titleHeaderRight.setVisible(true);
            sailorProfilesTable.removeColumn(navigatorColumn);
        } else {
            this.navigationTarget = navigationTarget;
            titleHeaderRight.setVisible(false);
            sailorProfilesTable.addColumn(navigatorColumn);
        }
    }

    /** sets {@link #navigationTarget} which is called when the arrow in the table header is clicked */
    public void setNavigationTarget(Function<Pair<SimpleCompetitorWithIdDTO, SingleEntry>, String> navigationTarget) {
        titleHeaderRight.setUrl(SharedHomeResources.INSTANCE.arrowDownWhite().getSafeUri());
        anchor.getElement().appendChild(titleHeaderRight.getElement());
        this.navigationTarget = navigationTarget;
    }

    public void setData(final List<Pair<SimpleCompetitorWithIdDTO, SingleEntry>> data) {
        sailorProfilesTable.setPageSize(data.size());
        if (data.size() > 0) {
            dataMiningUrl = navigationTarget.apply(data.get(0));
            anchor.setHref(dataMiningUrl);
        }
        if (!userService.hasPermission(SecuredDomainType.DATA_MINING.getStringPermission())) {
            anchor.setVisible(false);
        }
        sailorProfilesTable.setList(data);
    }

    private void setupTable() {
        boolean isAverage = type.getAggregationType() == StatisticType.AVERAGE;

        if (!isAverage) {
            sailorProfilesTable.addColumn(eventNameColumn, StringMessages.INSTANCE.eventName());
            sailorProfilesTable.addColumn(timeColumn, StringMessages.INSTANCE.time());
        }

        actualValueColumn.setSortable(true);
        sailorProfilesTable.getColumnSortList().push(actualValueColumn);
        sailorProfilesTable.setInitialSortColumn(actualValueColumn);
        sailorProfilesTable.setDefaultSortOrder(actualValueColumn, true);

        sailorProfilesTable.addColumn(actualValueColumn,
                SailorProfileNumericStatisticTypeFormatter.getColumnHeadingName(type, stringMessages),
                new InvertibleComparatorAdapter<Pair<SimpleCompetitorWithIdDTO, SingleEntry>>(true) {
                    @Override
                    public int compare(Pair<SimpleCompetitorWithIdDTO, SingleEntry> o1,
                            Pair<SimpleCompetitorWithIdDTO, SingleEntry> o2) {
                        return o1.getB().getValue().compareTo(o2.getB().getValue());
                    }
                }, true);
        sailorProfilesTable.addColumn(competitorColumn, StringMessages.INSTANCE.competitor());
        sailorProfilesTable.addColumn(clubNameColumn, StringMessages.INSTANCE.name());

        navigatorColumn.setCellStyleNames(DesignedCellTableResources.INSTANCE.cellTableStyle().buttonCell());
        navigatorColumn.setFieldUpdater(new FieldUpdater<Pair<SimpleCompetitorWithIdDTO, SingleEntry>, String>() {
            @Override
            public void update(int index, Pair<SimpleCompetitorWithIdDTO, SingleEntry> entry, String value) {
                Window.Location.assign(value);
            }
        });
        if (!isAverage) {
            setNavigationTarget(this::createRaceboardURL, false);
        } else {
            // navigation target is set from outside
        }

        if (userService.hasPermission(SecuredDomainType.DATA_MINING.getStringPermission())
                || !isAverage) {
            addButtonStyle(actualValueColumn);
            addButtonStyle(clubNameColumn);
            addButtonStyle(competitorColumn);
            addButtonStyle(eventNameColumn);
            addButtonStyle(navigatorColumn);
            addButtonStyle(timeColumn);

            sailorProfilesTable.addCellPreviewHandler(e -> {
                if ("click".equals(e.getNativeEvent().getType())) {
                    Window.Location.assign(navigationTarget.apply(e.getValue()));
                }
            });
        }
    }

    private void addButtonStyle(Column<?, ?> col) {
        col.setCellStyleNames(col.getCellStyleNames(null, null) + " "
                + SailorProfileDesktopResources.INSTANCE.css().clickableColumn());
    }

    private String createRaceboardURL(Pair<SimpleCompetitorWithIdDTO, SingleEntry> entry) {
        String result = null;
        if (type.getAggregationType() != StatisticType.AVERAGE) {
            final RegattaAndRaceIdentifier raceIdentifier = entry.getB().getRelatedRaceOrNull();

            // create raceboard context
            RaceboardContextDefinition raceboardContext = new RaceboardContextDefinition(
                    raceIdentifier.getRegattaName(), raceIdentifier.getRaceName(),
                    entry.getB().getLeaderboardNameOrNull(), entry.getB().getLeaderboardGroupNameOrNull(),
                    entry.getB().getEventIdOrNull(), type.getPlayerMode().name(), entry.getA().getIdAsString());
            RaceBoardPerspectiveOwnSettings perspectiveOwnSettings = new RaceBoardPerspectiveOwnSettings(
                    new MillisecondsDurationImpl(entry.getB().getRelatedTimePointOrNull().asMillis()
                            - entry.getB().getRelatedRaceStartTimePointOrNull().asMillis()));

            // create raceboard settings
            HashMap<String, Settings> innerSettings = new HashMap<>();
            innerSettings.put(RaceMapLifecycle.ID, RaceMapSettings.getDefaultWithShowMapControls(true));
            PerspectiveCompositeSettings<RaceBoardPerspectiveOwnSettings> settings = new PerspectiveCompositeSettings<>(
                    perspectiveOwnSettings, innerSettings);
            result = EntryPointWithSettingsLinkFactory.createRaceBoardLink(raceboardContext, settings);
        }
        return result;
    }

    private final Column<Pair<SimpleCompetitorWithIdDTO, SingleEntry>, String> navigatorColumn = new NavigatorColumn<Pair<SimpleCompetitorWithIdDTO, SingleEntry>>() {
        @Override
        public String getValue(Pair<SimpleCompetitorWithIdDTO, SingleEntry> entry) {
            return navigationTarget.apply(entry);
        }
    };

    private final Column<Pair<SimpleCompetitorWithIdDTO, SingleEntry>, String> actualValueColumn = new Column<Pair<SimpleCompetitorWithIdDTO, SingleEntry>, String>(
            new TextCell()) {
        @Override
        public String getValue(Pair<SimpleCompetitorWithIdDTO, SingleEntry> entry) {
            return SailorProfileNumericStatisticTypeFormatter.format(type, entry.getB().getValue(), stringMessages);
        }
    };
    private final Column<Pair<SimpleCompetitorWithIdDTO, SingleEntry>, SimpleCompetitorWithIdDTO> competitorColumn = new Column<Pair<SimpleCompetitorWithIdDTO, SingleEntry>, SimpleCompetitorWithIdDTO>(
            new AbstractCell<SimpleCompetitorWithIdDTO>() {
                @Override
                public void render(Context context, SimpleCompetitorWithIdDTO value, SafeHtmlBuilder sb) {
                    sb.appendHtmlConstant(new CompetitorWithoutClubnameItemDescription(value, flagImageResolver)
                            .getElement().getInnerHTML());
                }
            }) {

        @Override
        public SimpleCompetitorWithIdDTO getValue(Pair<SimpleCompetitorWithIdDTO, SingleEntry> object) {
            return object.getA();
        }
    };

    private final Column<Pair<SimpleCompetitorWithIdDTO, SingleEntry>, String> clubNameColumn = new Column<Pair<SimpleCompetitorWithIdDTO, SingleEntry>, String>(
            new TextCell()) {
        @Override
        public String getValue(Pair<SimpleCompetitorWithIdDTO, SingleEntry> entry) {
            return entry.getA().getName();
        }
    };
    private final Column<Pair<SimpleCompetitorWithIdDTO, SingleEntry>, String> eventNameColumn = new Column<Pair<SimpleCompetitorWithIdDTO, SingleEntry>, String>(
            new TextCell()) {
        @Override
        public String getValue(Pair<SimpleCompetitorWithIdDTO, SingleEntry> entry) {
            return entry.getB().getLeaderboardNameOrNull() + " " + entry.getB().getRaceNameOrNull();
        }
    };
    private final Column<Pair<SimpleCompetitorWithIdDTO, SingleEntry>, String> timeColumn = new Column<Pair<SimpleCompetitorWithIdDTO, SingleEntry>, String>(
            new TextCell()) {
        @Override
        public String getValue(Pair<SimpleCompetitorWithIdDTO, SingleEntry> entry) {
            return SailorProfileNumericStatisticTypeFormatter.format(entry.getB().getRelatedTimePointOrNull());
        }
    };
}
