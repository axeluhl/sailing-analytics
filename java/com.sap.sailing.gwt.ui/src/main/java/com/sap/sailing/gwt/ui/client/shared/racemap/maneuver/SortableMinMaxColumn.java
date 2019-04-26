package com.sap.sailing.gwt.ui.client.shared.racemap.maneuver;

import java.util.Comparator;
import java.util.function.Function;

import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.TextHeader;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.view.client.ListDataProvider;
import com.sap.sailing.gwt.ui.client.NumberFormatterFactory;
import com.sap.sailing.gwt.ui.leaderboard.HasStringAndDoubleValue;
import com.sap.sailing.gwt.ui.leaderboard.MinMaxRenderer;
import com.sap.sse.common.InvertibleComparator;
import com.sap.sse.common.SortingOrder;
import com.sap.sse.common.impl.InvertibleComparatorAdapter;
import com.sap.sse.gwt.client.celltable.AbstractSortableColumnWithMinMax;

public class SortableMinMaxColumn extends AbstractSortableColumnWithMinMax<ManeuverTableData, String> {
    private final static NumberFormat TWO_DIGIT_ACCURACY = NumberFormatterFactory.getDecimalFormat(2);
    private final String title;
    private final String unit;

    final InvertibleComparator<ManeuverTableData> comparator;
    
    final HasStringAndDoubleValue<ManeuverTableData> dataProvider;

    final MinMaxRenderer<ManeuverTableData> renderer;
    
    final ListDataProvider<ManeuverTableData> maneuverTableListDataProvider;

    public SortableMinMaxColumn(final Function<ManeuverTableData, Double> extractor, String title, String unit,
            ListDataProvider<ManeuverTableData> maneuverTableListDataProvider, boolean absolute) {
        super(new TextCell(), SortingOrder.ASCENDING);
        this.title = title;
        this.unit = unit;
        this.maneuverTableListDataProvider = maneuverTableListDataProvider;
        this.comparator = new InvertibleComparatorAdapter<ManeuverTableData>() {
            @Override
            public int compare(ManeuverTableData o1, ManeuverTableData o2) {
                Double o1v = extractor.apply(o1);
                Double o2v = extractor.apply(o2);
                return Comparator.nullsFirst((Double v1, Double v2)->Double.compare(absolute?Math.abs(v1):v1, absolute?Math.abs(v2):v2)).compare(o1v, o2v);
            }
        };
        this.dataProvider = new HasStringAndDoubleValue<ManeuverTableData>() {
            @Override
            public String getStringValueToRender(ManeuverTableData row) {
                Double value = extractor.apply(row);
                if (value == null) {
                    return null;
                }
                return TWO_DIGIT_ACCURACY.format(value);
            }

            @Override
            public Double getDoubleValue(ManeuverTableData row) {
                Double value = extractor.apply(row);
                return value == null ? null : absolute ? Math.abs(value) : value;
            }
        };

        this.renderer = new MinMaxRenderer<ManeuverTableData>(dataProvider, comparator);
        this.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
    }
    
    @Override
    public InvertibleComparator<ManeuverTableData> getComparator() {
        return comparator;
    }

    @Override
    public void render(Context context, ManeuverTableData object, SafeHtmlBuilder sb) {
        renderer.render(context, object, title, sb);
    }

    @Override
    public Header<?> getHeader() {
        return new TextHeader(title + " [" + unit + "]");
    }

    @Override
    public String getValue(ManeuverTableData object) {
        return dataProvider.getStringValueToRender(object);
    }

    @Override
    public void updateMinMax() {
        renderer.updateMinMax(maneuverTableListDataProvider.getList());
    }
}