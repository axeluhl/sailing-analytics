package com.sap.sailing.grib.test;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Formatter;
import java.util.List;

import org.junit.Test;

import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.grib.GribWindField;
import com.sap.sailing.grib.GribWindFieldFactory;

import ucar.ma2.ArrayFloat.D2;
import ucar.nc2.VariableSimpleIF;
import ucar.nc2.constants.FeatureType;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.dt.grid.GeoGrid;
import ucar.nc2.dt.grid.GridDataset;
import ucar.nc2.ft.FeatureDataset;
import ucar.nc2.ft.FeatureDatasetFactoryManager;
import ucar.nc2.time.CalendarDate;
import ucar.nc2.time.CalendarDateRange;
import ucar.unidata.geoloc.LatLonRect;

public class SimpleGridFileReadingTest {
    private GridDataset dataSet;

    @Test
    public void testBoundingBox() throws IOException {
        dataSet = GridDataset.open("resources/Drake.wind.grb");
        final LatLonRect boundingBox = dataSet.getBoundingBox();
        System.out.println(boundingBox);
    }

    @Test
    public void testWindContent() throws IOException {
        dataSet = GridDataset.open("resources/Drake.wind.grb");
        final List<GridDatatype> grids = dataSet.getGrids();
        for (GridDatatype grid : grids) {
            assertTrue(grid instanceof GeoGrid);
        }
        GeoGrid grid0 = (GeoGrid) grids.get(0);
        final D2 volumeDataAtTime0 = (D2) grid0.readVolumeData(0);
        final List<VariableSimpleIF> dataVariables = dataSet.getDataVariables();
        final GridCoordSystem coordinateSystem = grid0.getCoordinateSystem();
        final LatLonRect latLngBoundingBox = coordinateSystem.getLatLonBoundingBox();
        final Position sw = new DegreePosition(latLngBoundingBox.getLowerLeftPoint().getLatitude(), latLngBoundingBox.getLowerLeftPoint().getLongitude());
        final Position ne = new DegreePosition(latLngBoundingBox.getUpperRightPoint().getLatitude(), latLngBoundingBox.getUpperRightPoint().getLongitude());
        final Position middle = sw.translateGreatCircle(sw.getBearingGreatCircle(ne), sw.getDistance(ne).scale(0.5));
        final int[] coordinateIndices = coordinateSystem.findXYindexFromLatLon(middle.getLatDeg(), middle.getLngDeg(), new int[2]);
        final float dataAtMiddle = volumeDataAtTime0.get(coordinateIndices[0], coordinateIndices[1]);
        System.out.println(dataVariables);
        System.out.println(dataAtMiddle);
    }
    
    @Test
    public void testUsingFtAPI() throws IOException {
        final Formatter errorLog = new Formatter(System.err);
        FeatureDataset dataSet = FeatureDatasetFactoryManager.open(FeatureType.ANY, "resources/wind-Atlantic.24hr.grb.bz2", /* task */ null, errorLog);
        GribWindField windField = GribWindFieldFactory.INSTANCE.createGribWindField(dataSet);
        System.out.println(windField);
    }
    
    @Test
    public void testWindContent2() throws IOException {
        dataSet = GridDataset.open("resources/wind-Atlantic.24hr.grb.bz2");
        final List<GridDatatype> grids = dataSet.getGrids();
        for (GridDatatype grid : grids) {
            assertTrue(grid instanceof GeoGrid);
        }
        GeoGrid grid0 = (GeoGrid) grids.get(0);
        final D2 volumeDataAtTime0 = (D2) grid0.readVolumeData(0);
        final List<VariableSimpleIF> dataVariables = dataSet.getDataVariables();
        final GridCoordSystem coordinateSystem = grid0.getCoordinateSystem();
        final LatLonRect latLngBoundingBox = coordinateSystem.getLatLonBoundingBox();
        final Position sw = new DegreePosition(latLngBoundingBox.getLowerLeftPoint().getLatitude(), latLngBoundingBox.getLowerLeftPoint().getLongitude());
        final Position ne = new DegreePosition(latLngBoundingBox.getUpperRightPoint().getLatitude(), latLngBoundingBox.getUpperRightPoint().getLongitude());
        final Position middle = sw.translateGreatCircle(sw.getBearingGreatCircle(ne), sw.getDistance(ne).scale(0.5));
        final int[] coordinateIndices = coordinateSystem.findXYindexFromLatLon(middle.getLatDeg(), middle.getLngDeg(), new int[2]);
        final float dataAtMiddle = volumeDataAtTime0.get(coordinateIndices[0], coordinateIndices[1]);
        System.out.println(dataVariables);
        System.out.println(dataAtMiddle);
    }
    
    @Test
    public void testDateRange() throws IOException {
        dataSet = GridDataset.open("resources/Drake.wind.grb");
        final CalendarDateRange dateRange = dataSet.getCalendarDateRange();
        final CalendarDate start = dateRange.getStart();
        final CalendarDate end = dateRange.getEnd();
        assertTrue(end.isAfter(start));
    }
}
