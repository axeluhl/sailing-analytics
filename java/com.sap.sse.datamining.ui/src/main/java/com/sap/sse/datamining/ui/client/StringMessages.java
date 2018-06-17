package com.sap.sse.datamining.ui.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.LocalizableResource.DefaultLocale;


/**
 * Defines the text strings for i18n that are used by the Datamining GWT bundle. 
 * 
 * @author Maximilian Groﬂ (D064866)
 */
@DefaultLocale("en")
public interface StringMessages extends com.sap.sse.gwt.client.StringMessages {

    public static final StringMessages INSTANCE = GWT.create(StringMessages.class);

    String dataMiningSettings();
    String runPredefinedQuery();
    String viewQueryDefinition();
    String selectPredefinedQuery();
    String errorRunningDataMiningQuery();
    String predefinedQueryRunner();
    String useClassGetNameTooltip();
    String useClassGetName();
    String useStringLiteralsTooltip();
    String queryDefinitionViewer();
    String runAsSubstantive();
    String queryNotValidBecause();
    String queryRunner();
    String runAQuery();
    String runningQuery();
    String queryResultsChartSubtitle(int retrievedDataAmount, double calculationTime);
    String dataMiningErrorMargins();
    String dataMiningResult();
    String developerOptions();
    String runAutomatically();
    String runAutomaticallyTooltip();
    String currentFilterSelection();
    String plainResultsPresenter();
    String multiResultsPresenter();
    String columnChart();
    String columnChartWithErrorBars();
    String errorFetchingComponentsChangedTimepoint(String message);
    String dataMiningComponentsHaveBeenUpdated();
    String dataMiningComponentsNeedReloadDialogMessage();
    String noDimensionToGroupBySelectedError();
    String noDataRetrieverChainDefinitonSelectedError();
    String noStatisticSelectedError();
    String calculateThe();
    String statisticProvider();
    String groupingProvider();    
    String basedOn();
    String chooseDifferentDimensionTitle();
    String chooseDifferentDimensionMessage();
    String pleaseSelectADimension();
    String queryDefinitionProvider();
    String dataMiningRetrieval();
    String tabbedResultsPresenter();
    String useStringLiterals();
    String numberPairResultsPresenter();
    String copiedToClipboard();
    String csvExport();
    String search();
    String csvCopiedToClipboard();
    String filter();
    String filterShownDimensions();
    String selectDimensionsToFilterBy();
    
}
