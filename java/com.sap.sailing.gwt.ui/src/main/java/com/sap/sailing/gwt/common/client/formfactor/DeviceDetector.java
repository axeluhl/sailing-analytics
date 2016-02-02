package com.sap.sailing.gwt.common.client.formfactor;

import java.util.logging.Logger;

import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.Window.Navigator;

public class DeviceDetector {
    private static Logger LOG = Logger.getLogger(DeviceDetector.class.getName());
    
    private static final RegExp isMobileRegExp = RegExp.compile(
            "Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini|Mobile Safari", "i");
    private static final RegExp tabletBlacklistRegExp = RegExp.compile(
            "iPad|" // Apple ;-)
            + "Nexus 7|Nexus 10|Nexus 9|" // Google Tablets
            + "PlayBook|" // Blackberry
            + "KFAPWI|" // Kindle Fire HD
            + "FZ-B|" // Panasonic ToughPad
            + "SGP|" // Sony Xperia Tablets
            + "GT-P|SM-T|SM-P|" // Galaxy Tab (https://en.wikipedia.org/wiki/Samsung_Galaxy ; http://forum.xda-developers.com/wiki/Samsung/Model_naming_scheme)
            + "Tablet" // Firefox on Android
                    , "i");
    
    private static DeviceCategory deviceCategory;
    public static DeviceCategory getDeviceCategory() {
        if(deviceCategory == null) {
            deviceCategory = calculateDeviceCategory();
        }
        return deviceCategory;
    }
    
    private static DeviceCategory calculateDeviceCategory() {
        boolean isMobile = isMobileRegExp.test(Navigator.getUserAgent());
        LOG.info("Navigator user agent matched mobile regex: " + isMobile);
        if(!isMobile) {
            return DeviceCategory.DESKTOP;
        }
        boolean isTablet = tabletBlacklistRegExp.test(Navigator.getUserAgent());
        LOG.info("Navigator user agent matched tablet regex: " + isTablet);
        return isTablet ? DeviceCategory.TABLET : DeviceCategory.MOBILE;
    }

    /**
     * Uses regular expression and user agent to detect mobile device.
     * 
     * @return
     */
    public static boolean isMobile() {
        return getDeviceCategory() == DeviceCategory.MOBILE;
    }
    
    /**
     * Uses regular expression and user agent to detect tablet device.
     * 
     * @return
     */
    public static boolean isTablet() {
        return getDeviceCategory() == DeviceCategory.TABLET;
    }

    /**
     * Convinience method
     * 
     * @return
     */
    public static boolean isDesktop() {
        return getDeviceCategory() == DeviceCategory.DESKTOP;
    }
}
