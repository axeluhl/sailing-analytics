package com.sap.sailing.grib.test;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import ucar.ma2.Array;
import ucar.nc2.VariableSimpleIF;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.dt.grid.GeoGrid;
import ucar.nc2.dt.grid.GridDataset;
import ucar.nc2.time.CalendarDate;
import ucar.nc2.time.CalendarDateRange;
import ucar.unidata.geoloc.LatLonRect;

public class SimpleGridFileReadingTest {
    private GridDataset dataSet;

    @Before
    public void setUp() throws IOException {
        dataSet = GridDataset.open("resources/Drake.wind.grb");
    }
    
    @Test
    public void testBoundingBox() {
        final LatLonRect boundingBox = dataSet.getBoundingBox();
        System.out.println(boundingBox);
    }

    @Test
    public void testWindContent() throws IOException {
        final List<GridDatatype> grids = dataSet.getGrids();
        for (GridDatatype grid : grids) {
            assertTrue(grid instanceof GeoGrid);
        }
        GeoGrid grid0 = (GeoGrid) grids.get(0);
        final Array volumeDataAtTime0 = grid0.readVolumeData(0);
        final List<VariableSimpleIF> dataVariables = dataSet.getDataVariables();
        System.out.println(dataVariables);
    }
    
    @Test
    public void testDateRange() {
        final CalendarDateRange dateRange = dataSet.getCalendarDateRange();
        final CalendarDate start = dateRange.getStart();
        final CalendarDate end = dateRange.getEnd();
        assertTrue(end.isAfter(start));
    }
}
