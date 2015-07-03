package com.sap.sailing.gwt.home.client.place.event.partials.racelist;

import java.util.Date;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.DateCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.TextTransform;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.safecss.shared.SafeStyles;
import com.google.gwt.safecss.shared.SafeStylesBuilder;
import com.google.gwt.safecss.shared.SafeStylesUtils;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.safehtml.shared.UriUtils;
import com.sap.sailing.domain.common.InvertibleComparator;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.impl.NaturalComparator;
import com.sap.sailing.gwt.common.client.SharedResources;
import com.sap.sailing.gwt.common.client.SharedResources.MainCss;
import com.sap.sailing.gwt.common.client.SharedResources.MediaCss;
import com.sap.sailing.gwt.common.client.i18n.TextMessages;
import com.sap.sailing.gwt.home.client.place.event.EventView;
import com.sap.sailing.gwt.home.client.place.event.partials.raceListLive.RacesListLiveResources;
import com.sap.sailing.gwt.home.client.place.event.partials.raceListLive.RacesListLiveResources.LocalCss;
import com.sap.sailing.gwt.regattaoverview.client.FlagsMeaningExplanator;
import com.sap.sailing.gwt.regattaoverview.client.SailingFlagsBuilder;
import com.sap.sailing.gwt.ui.client.FlagImageResolver;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.dispatch.event.LiveRaceDTO;
import com.sap.sailing.gwt.ui.shared.dispatch.event.RaceListRaceDTO;
import com.sap.sailing.gwt.ui.shared.race.FlagStateDTO;
import com.sap.sailing.gwt.ui.shared.race.FleetMetadataDTO;
import com.sap.sailing.gwt.ui.shared.race.RaceMetadataDTO;
import com.sap.sailing.gwt.ui.shared.race.RaceMetadataDTO.RaceTrackingState;
import com.sap.sailing.gwt.ui.shared.race.RaceMetadataDTO.RaceViewState;
import com.sap.sailing.gwt.ui.shared.race.RaceProgressDTO;
import com.sap.sailing.gwt.ui.shared.race.SimpleWindDTO;
import com.sap.sailing.gwt.ui.shared.util.NullSafeComparableComparator;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class RaceListColumnFactory {
    
    private static final LocalCss CSS = RacesListLiveResources.INSTANCE.css(); 
    private static final MainCss MAIN_CSS = SharedResources.INSTANCE.mainCss();
    private static final MediaCss MEDIA_CSS = SharedResources.INSTANCE.mediaCss();
    private static final StringMessages I18N = StringMessages.INSTANCE;
    private static final TextMessages I18N_UBI = TextMessages.INSTANCE;
    private static final CellTemplates TEMPLATE = GWT.create(CellTemplates.class);

    interface CellTemplates extends SafeHtmlTemplates {
        @Template("<div style=\"{1}\" class=\"{0}\"></div>")
        SafeHtml fleetCorner(String styleNames, SafeStyles color);

        @Template("<div>{3}</div><div class=\"{0}\"><div style=\"{2}\" class=\"{1}\"></div></div>")
        SafeHtml viewStateRunning(String styleNamesBar, String styleNamesProgress, SafeStyles width, String text);

        @Template("<img style=\"{0}\" src=\"images/home/windkompass_nord.svg\"/>")
        SafeHtml windDirection(SafeStyles rotation);

        @Template("<div style=\"{0}\">{1}</div>")
        SafeHtml raceNotTracked(SafeStyles styles, String text);

        @Template("<a href=\"{2}\" class=\"{0}\" target=\"_blank\">{1}</a>")
        SafeHtml raceViewerLinkButton(String styleNames, String text, String link);
        
        @Template("<img src=\"{2}\" class=\"{0}\" /><span class=\"{1}\">{3}</span>")
        SafeHtml winner(String styleNamesFlag, String styleNamesText, String flagImageURL, String name);
        
        @Template("<img src=\"{2}\" class=\"{0}\" /><span class=\"{1}\">{3}</span>")
        SafeHtml winner(String styleNamesFlag, String styleNamesText, SafeUri flagImageURL, String name);
    }
    
    public static <T extends RaceMetadataDTO> SortableRaceListColumn<T, FleetMetadataDTO> getFleetCornerColumn() {
        Cell<FleetMetadataDTO> cell = new AbstractCell<FleetMetadataDTO>() {
            @Override
            public void render(Context context, FleetMetadataDTO value, SafeHtmlBuilder sb) {
                if (value != null) {
                    SafeStyles style = SafeStylesUtils.fromTrustedNameAndValue("border-top-color", value.getFleetColor());
                    sb.append(TEMPLATE.fleetCorner(CSS.race_fleetcorner_icon(), style));
                }
            }
        };
        InvertibleComparator<T> comparator = null;
        return new SortableRaceListColumn<T, FleetMetadataDTO>("", cell, comparator) {
            @Override
            public String getColumnStyle() {
                return CSS.race_fleetcorner();
            }
            
            @Override
            public FleetMetadataDTO getValue(T object) {
                return object.getFleet();
            }
        };
    }
    
    public static <T extends RaceMetadataDTO> SortableRaceListColumn<T, String> getRegattaNameColumn() {
        Cell<String> cell = new TextCell();
        InvertibleComparator<T> comparator = new InvertibleComparatorWrapper<T, String>(new NaturalComparator(false)) {
            @Override
            protected String getComparisonValue(T object) {
                return object.getRegattaDisplayName();
            }
        };
        return new SortableRaceListColumn<T, String>(I18N.regatta(), cell, comparator) {
            @Override
            public String getHeaderStyle() {
                return CSS.raceslist_head_item();
            }

            @Override
            public String getColumnStyle() {
                return getStyleNamesString(CSS.race_item(), CSS.race_itemname());
            }

            @Override
            public String getValue(T object) {
                return object.getRegattaDisplayName();
            }
        };
    }
    
    public static <T extends RaceMetadataDTO> SortableRaceListColumn<T, String> getRaceNameColumn() {
        Cell<String> cell = new TextCell();
        InvertibleComparator<T> comparator = new InvertibleComparatorWrapper<T, String>(new NaturalComparator(false)) {
            @Override
            protected String getComparisonValue(T object) {
                return object.getRaceName();
            }
        };
        return new SortableRaceListColumn<T, String>(I18N.race(), cell, comparator) {
            @Override
            public String getHeaderStyle() {
                return CSS.raceslist_head_item();
            }
            
            @Override
            public String getColumnStyle() {
                return getStyleNamesString(CSS.race_item(), CSS.race_itemname());
            }
            
            @Override
            public String getValue(T object) {
                return object.getRaceName();
            }
        };
    }
    
    public static <T extends RaceMetadataDTO> SortableRaceListColumn<T, String> getFleetNameColumn() {
        Cell<String> cell = new TextCell();
        InvertibleComparator<T> comparator = new InvertibleComparatorWrapper<T, String>(new NaturalComparator(false)) {
            @Override
            protected String getComparisonValue(T object) {
                return object.getFleet() != null ? object.getFleet().getFleetName() : null;
            }
        };
        return new SortableRaceListColumn<T, String>(I18N.fleet(), cell, comparator) {
            @Override
            public String getHeaderStyle() {
                return getStyleNamesString(CSS.raceslist_head_item(), MEDIA_CSS.showonlarge());
            }
            
            @Override
            public String getColumnStyle() {
                return getStyleNamesString(CSS.race_item(), MEDIA_CSS.showonlarge());
            }
            
            @Override
            public String getValue(T object) {
                return object.getFleet() != null ? object.getFleet().getFleetName() : null;
            }
        };
    }
    
    public static <T extends RaceMetadataDTO> SortableRaceListColumn<T, Date> getStartTimeColumn() {
        Cell<Date> cell = new DateCell(DateTimeFormat.getFormat(PredefinedFormat.HOUR24_MINUTE));
        InvertibleComparator<T> comparator = new InvertibleComparatorWrapper<T, Date>( new NullSafeComparableComparator<Date>()) {
            @Override
            protected Date getComparisonValue(T object) {
                return object.getStart();
            }
        };
        return new SortableRaceListColumn<T, Date>(I18N.start(), cell, comparator) {
            @Override
            public String getHeaderStyle() {
                return CSS.raceslist_head_item();
            }

            @Override
            public String getColumnStyle() {
                return CSS.race_item();
            }

            @Override
            public Date getValue(T object) {
                return object.getStart();
            }
        };
    }
    
    public static <T extends LiveRaceDTO> SortableRaceListColumn<T, FlagStateDTO> getFlagsColumn() {
        Cell<FlagStateDTO> cell = new AbstractCell<FlagStateDTO>() {
            @Override
            public void render(Context context, FlagStateDTO value, SafeHtmlBuilder sb) {
                if (value != null) {
                    sb.append(SailingFlagsBuilder.render(value, 0.55, FlagsMeaningExplanator.getFlagsMeaning(I18N,
                            value.getLastUpperFlag(), value.getLastLowerFlag(), value.isLastFlagsAreDisplayed())));
                }
            }
        };
        InvertibleComparator<T> comparator = null;
        return new SortableRaceListColumn<T, FlagStateDTO>(I18N.flags(), cell, comparator) {
            @Override
            public String getHeaderStyle() {
                return CSS.raceslist_head_item();
            }

            @Override
            public String getColumnStyle() {
                return getStyleNamesString(CSS.race_item(), CSS.racesListIcon());
            }

            @Override
            public FlagStateDTO getValue(T object) {
                return object.getFlagState();
            }
        };
    }
    
    public static <T extends RaceMetadataDTO> SortableRaceListColumn<T, String> getWindSpeedColumn() {
        Cell<String> cell = new TextCell();
        InvertibleComparator<T> comparator =  new InvertibleComparatorWrapper<T, Double>( new NullSafeComparableComparator<Double>()) {
            @Override
            protected Double getComparisonValue(T object) {
                return object.getWind() != null ? object.getWind().getTrueWindSpeedInKnots() : null;
            }
        };
        return new SortableRaceListColumn<T, String>(I18N.wind(), cell, comparator) {
            @Override
            public String getHeaderStyle() {
                return CSS.raceslist_head_item();
            }
            
            @Override
            public String getColumnStyle() {
                return CSS.race_item();
            }
            
            @Override
            public String getValue(T object) {
                return object.getWind() != null ? I18N.knotsValue(object.getWind().getTrueWindSpeedInKnots()) : null;
            }
        };
    }
    
    public static <T extends RaceMetadataDTO> SortableRaceListColumn<T, SimpleWindDTO> getWindDirectionColumn() {
        Cell<SimpleWindDTO> cell = new AbstractCell<SimpleWindDTO>() {
            @Override
            public void render(Context context, SimpleWindDTO value, SafeHtmlBuilder sb) {
                if (value != null) {
                    SafeStylesBuilder safeStyles = new SafeStylesBuilder();
                    safeStyles.trustedNameAndValue("-webkit-transform", "rotate(" + value.getTrueWindFromDeg() + "deg)");
                    safeStyles.trustedNameAndValue("transform", "rotate(" + value.getTrueWindFromDeg() + "deg)");
                    safeStyles.width(2.75, Unit.EM).height(2.75, Unit.EM);
                    sb.append(TEMPLATE.windDirection(safeStyles.toSafeStyles()));
                }
            }
        };
        InvertibleComparator<T> comparator =   new InvertibleComparatorWrapper<T, Double>( new NullSafeComparableComparator<Double>()) {
            @Override
            protected Double getComparisonValue(T object) {
                return object.getWind() != null ? object.getWind().getTrueWindFromDeg() : null;
            }
        };
        return new SortableRaceListColumn<T, SimpleWindDTO>(I18N.from(), cell, comparator) {
            @Override
            public String getHeaderStyle() {
                return getStyleNamesString(CSS.raceslist_head_item(), MEDIA_CSS.hideonsmall());
            }

            @Override
            public String getColumnStyle() {
                return getStyleNamesString(CSS.race_item(), CSS.racesListIcon(), MEDIA_CSS.hideonsmall());
            }

            @Override
            public SimpleWindDTO getValue(T object) {
                return object.getWind();
            }
        };
    }
    
    public static <T extends RaceMetadataDTO> SortableRaceListColumn<T, String> getCourseAreaColumn() {
        Cell<String> cell = new TextCell();
        InvertibleComparator<T> comparator = new InvertibleComparatorWrapper<T, String>(new NaturalComparator(false)) {
            @Override
            protected String getComparisonValue(T object) {
                return object.getCourseArea();
            }
        };
        return new SortableRaceListColumn<T, String>(I18N.courseArea(), cell, comparator) {
            @Override
            public String getHeaderStyle() {
                return getStyleNamesString(CSS.raceslist_head_item(), MEDIA_CSS.showonlarge());
            }
            
            @Override
            public String getColumnStyle() {
                return getStyleNamesString(CSS.race_item(), MEDIA_CSS.showonlarge());
            }
            
            @Override
            public String getValue(T object) {
                return object.getCourseArea();
            }
        };
    }
    
    public static <T extends RaceMetadataDTO> SortableRaceListColumn<T, String> getCourseColumn() {
        Cell<String> cell = new TextCell();
        InvertibleComparator<T> comparator = new InvertibleComparatorWrapper<T, String>(new NaturalComparator(false)) {
            @Override
            protected String getComparisonValue(T object) {
                return object.getCourse();
            }
        };
        return new SortableRaceListColumn<T, String>(I18N.course(), cell, comparator) {
            @Override
            public String getHeaderStyle() {
                return getStyleNamesString(CSS.raceslist_head_item(), MEDIA_CSS.showonlarge());
            }
            
            @Override
            public String getColumnStyle() {
                return getStyleNamesString(CSS.race_item(), MEDIA_CSS.showonlarge());
            }
            
            @Override
            public String getValue(T object) {
                return object.getCourse();
            }
        };
    }
    
    public static <T extends LiveRaceDTO> SortableRaceListColumn<T, T> getRaceViewStateColumn() {
        Cell<T> cell = new AbstractCell<T>() {
            @Override
            public void render(Context context, T value, SafeHtmlBuilder sb) {
                RaceViewState state = value.getViewState();
                RaceProgressDTO progress = value.getProgress();
                if (state == RaceViewState.SCHEDULED) {
                    double min = MillisecondsTimePoint.now().until(new MillisecondsTimePoint(value.getStart())).asMinutes();
                    sb.appendEscaped(I18N.startingInMinutes((int) min));
                } else if (state == RaceViewState.RUNNING && progress != null) {
                    SafeStyles width = SafeStylesUtils.forWidth(progress.getPercentageProgress(), Unit.PCT);
                    String text = I18N.currentOfTotalLegs(progress.getCurrentLeg(), progress.getTotalLegs());
                    sb.append(TEMPLATE.viewStateRunning(CSS.race_itemstatus_progressbar(),
                            CSS.race_itemstatus_progressbar_progress(), width, text));
                } else {
                    sb.appendEscaped(state.getLabel());
                }
            }
        };
        InvertibleComparator<T> comparator = new InvertibleComparatorWrapper<T, RaceViewState>(new NullSafeComparableComparator<RaceViewState>()) {
            @Override
            protected RaceViewState getComparisonValue(T object) {
                return object.getViewState();
            }
        };
        return new SortableRaceListColumn<T, T>(I18N.status(), cell, comparator) {
            @Override
            public String getHeaderStyle() {
                return CSS.raceslist_head_item();
            }
            
            @Override
            public String getColumnStyle() {
                return CSS.race_item();
            }
            
            @Override
            public T getValue(T object) {
                return object;
            }
        };
    }
    
    public static <T extends RaceListRaceDTO> SortableRaceListColumn<T, CompetitorDTO> getWinnerColumn() {
        Cell<CompetitorDTO> cell = new AbstractCell<CompetitorDTO>() {
            @Override
            public void render(Context context, CompetitorDTO value, SafeHtmlBuilder sb) {
                if (value != null) {
                    final SafeUri flagImageUri;
                    if (value.getFlagImageURL() == null || value.getFlagImageURL().isEmpty()) {
                        String twoLetterIsoCountryCode = value.getTwoLetterIsoCountryCode();
                        if (twoLetterIsoCountryCode==null || twoLetterIsoCountryCode.isEmpty()) {
                            flagImageUri = FlagImageResolver.getEmptyFlagImageResource().getSafeUri();
                        } else {
                            flagImageUri = FlagImageResolver.getFlagImageResource(twoLetterIsoCountryCode).getSafeUri();
                        }
                    } else {
                        flagImageUri = UriUtils.fromTrustedString(value.getFlagImageURL());
                    }
                    sb.append(TEMPLATE.winner(CSS.race_item_flag(), CSS.race_item_winner(), flagImageUri, value.getName()));
                }
            }
        };
        InvertibleComparator<T> comparator = new InvertibleComparatorWrapper<T, String>(new NaturalComparator(false)) {
            @Override
            protected String getComparisonValue(T object) {
                return object.getWinner() != null ? object.getWinner().getName() : null;
            }
        };
        return new SortableRaceListColumn<T, CompetitorDTO>(I18N.winner(), cell, comparator) {
            @Override
            public String getHeaderStyle() {
                return CSS.raceslist_head_item();
            }
            
            @Override
            public String getColumnStyle() {
                return getStyleNamesString(CSS.race_item(), CSS.race_itemwinner());
            }
            
            @Override
            public CompetitorDTO getValue(T object) {
                return object.getWinner();
            }
        };
    }
    
    public static <T extends RaceMetadataDTO> SortableRaceListColumn<T, T> getRaceViewerButtonColumn(final EventView.Presenter presenter) {
        Cell<T> cell = new AbstractCell<T>() {
            private final String watchNowStyle = getButtonStyleNames(MAIN_CSS.buttonred());
            private final String analyseRaceStyle = getButtonStyleNames(MAIN_CSS.buttonprimary());

            @Override
            public void render(Context context, T data, SafeHtmlBuilder sb) {
                if (data.getTrackingState() != RaceTrackingState.TRACKED_VALID_DATA) {
                    SafeStylesBuilder styles = new SafeStylesBuilder().textTransform(TextTransform.UPPERCASE);
                    styles.trustedColor("#666").trustedNameAndValue("font-size", "0.933333333333333rem").toSafeStyles();
                    styles.trustedNameAndValue("padding", "0.714285714285714em 1.071428571428571em");
                    sb.append(TEMPLATE.raceNotTracked(styles.toSafeStyles(), I18N_UBI.eventRegattaRaceNotTracked()));
                } else {
                    String styleNames = data.getViewState() == RaceViewState.FINISHED ? analyseRaceStyle : watchNowStyle;
                    String text = data.getViewState() == RaceViewState.FINISHED ? I18N.analyseRace() : I18N_UBI.watchNow();
                    String raceViewerURL = presenter.getRaceViewerURL(data.getRegattaName(), data.getTrackedRaceName());
                    sb.append(TEMPLATE.raceViewerLinkButton(styleNames, text, raceViewerURL));
                }
            }

            private final String getButtonStyleNames(String buttonColor) {
                return Util.join(" ", MAIN_CSS.button(), MAIN_CSS.buttonstrong(), buttonColor, MAIN_CSS.buttonarrowrightwhite());
            }
        };
        InvertibleComparator<T> comparator = null;
        return new SortableRaceListColumn<T, T>("", cell, comparator) {
            @Override
            public String getHeaderStyle() {
                return getStyleNamesString(CSS.raceslist_head_item(), CSS.raceslist_head_itembutton());
            }
            
            @Override
            public String getColumnStyle() {
                return getStyleNamesString(CSS.race_item(), CSS.race_itemright());
            }
            
            @Override
            public T getValue(T object) {
                return object;
            }
        };
    }
}
