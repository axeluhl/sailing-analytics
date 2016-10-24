package com.sap.sailing.polars.jaxrs.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.polars.jaxrs.api.PolarDataResource;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.impl.Helpers;

public class PolarDataResourceTest extends AbstractJaxRsApiTest {
    @Before
    public void setUp() {
        super.setUp();
    }

    @Test
    public void testExportCubicRegressionAsJson() throws JsonDeserializationException {
        PolarDataResource spyResource = spyResource(new PolarDataResource());

        String jsonString = spyResource.getCubicRegression().getEntity().toString();
        JSONObject json = Helpers.toJSONObjectSafe(JSONValue.parse(jsonString));
        JSONArray dataEntries = (JSONArray) json.get("cubicRegressionPerCourse");

        JSONObject dataEntry = (JSONObject) dataEntries.get(0);
        JSONObject gpsFix = (JSONObject) ((JSONObject) dataEntry.get("dataEntry")).get("fix");
        assertThat("key is correct", dataEntry.get("key"), equalTo(1L));
        assertThat("fix type is correct", gpsFix.get("type"), equalTo("GPSFixMoving"));
        assertThat("fix lat_deg is correct", gpsFix.get("lat_deg"), equalTo(54.87374));
        assertThat("fix bearing_deg is correct", gpsFix.get("bearing_deg"), equalTo(43.2));
        assertThat("fix lon_deg is correct", gpsFix.get("lon_deg"), equalTo(10.193648));
        assertThat("fix speed_knots is correct", gpsFix.get("speed_knots"), equalTo(10.0));
    }

    @Test
    public void testExportSpeedRegressionAsJson() throws JsonDeserializationException {
        PolarDataResource spyResource = spyResource(new PolarDataResource());

        String jsonString = spyResource.getSpeedRegression().getEntity().toString();
        JSONObject json = Helpers.toJSONObjectSafe(JSONValue.parse(jsonString));

        JSONArray clusters = (JSONArray) ((JSONObject) json.get("angleClusterGroup")).get("clusters");
        JSONArray boundaries = (JSONArray) ((JSONObject) clusters.get(0)).get("boundaries");

        assertThat("clusters amount is correct", clusters.size(), equalTo(36));
        assertThat("boundaries amount is correct", boundaries.size(), equalTo(2));
    }

    @Test
    public void testExportAngleClusterGroupAsJson() throws JsonDeserializationException {
        PolarDataResource spyResource = spyResource(new PolarDataResource());

        String jsonString = spyResource.getAngleClusterGroup().getEntity().toString();
        JSONObject json = Helpers.toJSONObjectSafe(JSONValue.parse(jsonString));

        JSONArray clusters = (JSONArray) json.get("clusters");
        JSONArray boundaries = (JSONArray) ((JSONObject) clusters.get(0)).get("boundaries");

        assertThat("clusters amount is correct", clusters.size(), equalTo(36));
        assertThat("boundaries amount is correct", boundaries.size(), equalTo(2));
    }

    @Test
    public void testExportBackendPolarSettingsAsJson() throws JsonDeserializationException {
        PolarDataResource spyResource = spyResource(new PolarDataResource());

        String jsonString = spyResource.getBackendPolarSettings().getEntity().toString();
        JSONObject json = Helpers.toJSONObjectSafe(JSONValue.parse(jsonString));

        assertThat("minimumConfidenceMeasure is correct", json.get("minimumConfidenceMeasure"), equalTo(0.1));
        assertThat("minimumDataCountPerAngle is correct", json.get("minimumDataCountPerAngle"), equalTo(20L));
        assertThat("minimumDataCountPerGraph is correct", json.get("minimumDataCountPerGraph"), equalTo(50L));
        assertThat("minimumWindConfidence is correct", json.get("minimumWindConfidence"), equalTo(0.1));
        assertThat("numberOfHistogramColumns is correct", json.get("numberOfHistogramColumns"), equalTo(20L));
        assertThat("outlierDetectionNeighboorhoodRadius is correct", json.get("outlierDetectionNeighboorhoodRadius"),
                equalTo(2.0));
        assertThat("outlierMinimumNeighboorhoodPct is correct", json.get("outlierMinimumNeighboorhoodPct"),
                equalTo(0.05));
        assertThat("pctOfLeadingCompetitorsToInclude is correct", json.get("pctOfLeadingCompetitorsToInclude"),
                equalTo(3.0));
        assertThat("shouldRemoveOutliers is correct", json.get("shouldRemoveOutliers"), equalTo(true));
        assertThat("splitByWindGauges is correct", json.get("splitByWindGauges"), equalTo(false));
        assertThat("useOnlyEstimationForWindDirection is correct", json.get("useOnlyEstimationForWindDirection"),
                equalTo(true));
        assertThat("useOnlyWindGaugesForWindSpeed is correct", json.get("useOnlyWindGaugesForWindSpeed"),
                equalTo(true));

        JSONObject windStepping = (JSONObject) json.get("windStepping");
        assertThat("windStepping levels amount is correct", ((JSONArray) windStepping.get("levels")).size(),
                equalTo(69));
        assertThat("windStepping maxDistance is correct", windStepping.get("maxDistance"), equalTo(0.5));

    }
}
