package com.sap.sailing.gwt.ui.datamining;

import com.google.gwt.core.shared.GWT;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.datamining.shared.Unit;

public final class UnitFormatter {

    private static final StringMessages stringMessages = GWT.create(StringMessages.class);

    private UnitFormatter() {
    }

    public static String format(Unit unit) {
        switch (unit) {
        case Knots:
            return stringMessages.knotsUnit();
        case Meters:
            return stringMessages.metersUnit();
        case None:
            return "";
        }
        return "";
    }

}
