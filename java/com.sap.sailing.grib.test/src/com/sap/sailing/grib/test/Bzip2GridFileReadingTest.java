package com.sap.sailing.grib.test;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.impl.DegreePosition;

import ucar.ma2.ArrayFloat.D3;
import ucar.nc2.VariableSimpleIF;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.dt.grid.GeoGrid;
import ucar.nc2.dt.grid.GridDataset;
import ucar.nc2.time.CalendarDate;
import ucar.nc2.time.CalendarDateRange;
import ucar.unidata.geoloc.LatLonRect;

public class Bzip2GridFileReadingTest {
    private GridDataset dataSet;

    @Before
    public void setUp() throws IOException {
        dataSet = GridDataset.open("resources/EWAM_DD_10M_2017011912_050.grib2.bz2");
    }
    
    @After
    public void tearDown() throws IOException {
        dataSet.close();
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
        final D3 volumeDataAtTime0 = (D3) grid0.readVolumeData(0);
        final List<VariableSimpleIF> dataVariables = dataSet.getDataVariables();
        final GridCoordSystem coordinateSystem = grid0.getCoordinateSystem();
        final LatLonRect latLngBoundingBox = coordinateSystem.getLatLonBoundingBox();
        final Position sw = new DegreePosition(latLngBoundingBox.getLowerLeftPoint().getLatitude(), latLngBoundingBox.getLowerLeftPoint().getLongitude());
        final Position ne = new DegreePosition(latLngBoundingBox.getUpperRightPoint().getLatitude(), latLngBoundingBox.getUpperRightPoint().getLongitude());
        final Position middle = sw.translateGreatCircle(sw.getBearingGreatCircle(ne), sw.getDistance(ne).scale(0.5));
        final int[] coordinateIndices = coordinateSystem.findXYindexFromLatLon(middle.getLatDeg(), middle.getLngDeg(), new int[2]);
        final float dataAtMiddle = volumeDataAtTime0.get(0, coordinateIndices[0], coordinateIndices[1]);
        System.out.println(dataVariables);
        System.out.println(dataAtMiddle);
    }
    
    @Test
    public void testDateRange() {
        final CalendarDateRange dateRange = dataSet.getCalendarDateRange();
        final CalendarDate start = dateRange.getStart();
        final CalendarDate end = dateRange.getEnd();
        assertTrue(!end.isBefore(start));
    }
}
