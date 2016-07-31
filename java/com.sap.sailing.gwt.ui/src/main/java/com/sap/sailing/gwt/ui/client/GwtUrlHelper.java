package com.sap.sailing.gwt.ui.client;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.http.client.URL;
import com.sap.sse.common.util.UrlHelper;

/**
 * Tycho complains that {@link com.google.gwt} cannot be resolved when placing this in *common projects, where it would ideally
 * go (then we could also auto-determine if GWT context is present, e.g. through {@link GWT#isClient}).
 * 
 * @author Fredrik Teschke
 *
 */
public enum GwtUrlHelper implements UrlHelper {
    INSTANCE;
    
    @Override
    public String encodeQueryString(String queryString) {
        // TODO See bug3664 => https://bugzilla.sapsailing.com/bugzilla/show_bug.cgi?id=3664
        // Because of changes in the query string encoding, the Sail InSight-App for iOS will now have problems
        // while parsing the registration URLs, which are sent to invited competitors, if e.g. the leaderboard name
        // contains whitespaces. To avoid this problems, we use this temporary fix until a new version of the Sail
        // InSight-App for iOS is rolled out. Afterwards this fix must be replaced by the line below.
        return URL.encodePathSegment(queryString);
        // return URL.encodeQueryString(queryString);
    }

}
