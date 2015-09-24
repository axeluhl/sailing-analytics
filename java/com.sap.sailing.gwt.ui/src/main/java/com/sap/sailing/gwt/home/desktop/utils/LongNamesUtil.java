package com.sap.sailing.gwt.home.desktop.utils;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

/**
 * A utility class for handling long names
 * @author Frank
 */
public class LongNamesUtil {
    private static String NAME_SPLIT_RULE1 = " - ";

    public static SafeHtml breakLongName(String name) {
        SafeHtmlBuilder builder = new SafeHtmlBuilder();
        if(name != null) {
            if (name.contains(NAME_SPLIT_RULE1)) {
                String[] splittedName = name.split(NAME_SPLIT_RULE1);
                for (int i = 0; i < splittedName.length; i++) {
                    builder.appendEscaped(splittedName[i]);
                    if (i != splittedName.length - 1) {
                        builder.appendHtmlConstant("<br/>");
                    }
                }
            } else {
                builder.appendEscaped(name);
            }
        } else {
            builder.appendEscaped("");
        }
        return builder.toSafeHtml();
    }

}
