package com.sap.sse.datamining.ui.client.settings;

import com.sap.sse.datamining.ui.client.StringMessages;
import com.sap.sse.datamining.ui.client.settings.AdvancedDataMiningSettings.ChangeLossStrategy;

public class ChangeLossStrategyFormatter {
    
    public static String format(ChangeLossStrategy strategy, StringMessages stringMessages) {
        switch (strategy) {
        case ASK:
            return stringMessages.askChangeLossStrategyName();
        case DISCARD_CHANGES:
            return stringMessages.discardChangesChangeLossStrategyName();
        case KEEP_CHANGES:
            return stringMessages.keepChangesChangeLossStrategyName();
        }
        return strategy.toString();
    }
    
    public static String tooltipFor(ChangeLossStrategy strategy, StringMessages stringMessages) {
        switch (strategy) {
        case ASK:
            return stringMessages.askChangeLossStrategyTooltip();
        case DISCARD_CHANGES:
            return stringMessages.discardChangesChangeLossStrategyTooltip();
        case KEEP_CHANGES:
            return stringMessages.keepChangesChangeLossStrategyTooltip();
        }
        return null;
    }

}
