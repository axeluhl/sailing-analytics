package com.sap.sailing.dashboards.gwt.client.linker;

import com.google.gwt.core.ext.linker.Shardable;
import com.sap.sse.gwt.linker.ManifestLinker;

/**
 * @author Alexander Ries (D062114)
 *
 */

@Shardable
public class DashboardManifestLinker extends ManifestLinker {

        @Override
        protected String[] staticCachedFiles() {
                return new String[] { "/dashboards/RibDashboard.html",
                                      "/dashboards/RibDashboard.css",
                                      "/dashboards/images/dashboardicon.png",
                                      "/dashboards/js/jquery-3.3.1.min.js",
                                      "/sailing-fontface-1.0.cache.css"};
        }
}
