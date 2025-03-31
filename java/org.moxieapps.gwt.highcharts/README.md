# Patches to GWT Highcharts Wrapper API

## Show reset zoom button
Added method to `org.moxieapps.gwt.highcharts.client.BaseChart` to provide highcharts functionality in Java API:
```
    /**
     * Patch, to provide highcharts functionality in Java API. 
     */
    public native void showResetZoom() /*-{
        var chart = this.@org.moxieapps.gwt.highcharts.client.BaseChart::chart;
        if (chart) chart.showResetZoom();
    }-*/;
```

## Other changes
Some other change in the following classes are not documented yet:
- `org.moxieapps.gwt.highcharts.client.BaseChart`
- `org.moxieapps.gwt.highcharts.client.Series`