package com.sap.sse.datamining.ui.client;

import java.util.function.Consumer;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.datamining.shared.dto.StatisticQueryDefinitionDTO;
import com.sap.sse.gwt.client.shared.components.Component;

public interface DataMiningComponentProvider<SettingsType extends Settings> extends Component<SettingsType> {

    /**
     * Tells the provider, that {@link #reloadComponents()} will be called in the future.
     */
    void awaitReloadComponents();

    /**
     * @return <code>true</code>, if the provider is reloading its components or is waiting for the
     *         {@link #reloadComponents()} call.
     */
    boolean isAwaitingReload();

    void reloadComponents();

    void applyQueryDefinition(StatisticQueryDefinitionDTO queryDefinition, Consumer<Iterable<String>> callback);

}
