package com.sap.sailing.gwt.ui.shared.components;

/**
 * {@link Component}s are arranged in an "ordered forest" structure (multi-rooted tree with the roots ordered in a sequence).
 * Each hierarchy level can optionally provide a localized name. Each hierarchy level consists of one or more 
 *  
 * @author Axel Uhl (d043530)
 *
 */
public interface ComponentForest extends ComponentForestEntry {
    Iterable<ComponentForest> getEntries();
}
