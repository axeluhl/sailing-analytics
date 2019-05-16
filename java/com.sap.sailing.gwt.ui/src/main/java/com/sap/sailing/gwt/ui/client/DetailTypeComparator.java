package com.sap.sailing.gwt.ui.client;

import java.util.Comparator;

import com.sap.sailing.domain.common.DetailType;

/**
 * Comparator for sorting {@link DetailType}s alphabetically.
 * 
 * @author Tim Hessenmüller (D062243)
 */
public class DetailTypeComparator implements Comparator<DetailType> {

    @Override
    public int compare(DetailType o1, DetailType o2) {
        final boolean o1Expedition = o1.isExpeditionType();
        final boolean o2Expedition = o2.isExpeditionType();
        if ((o1Expedition && o2Expedition) || (!o1Expedition && !o2Expedition)) {
            final String o1Name = DetailTypeFormatter.format(o1);
            final String o2Name = DetailTypeFormatter.format(o2);
            return Collator.getInstance().compare(o1Name, o2Name);
        }
        if (o1Expedition && !o2Expedition) {
            return 1;
        }
        if (o2Expedition && !o1Expedition) {
            return -1;
        }
        return 0;
    }
}
