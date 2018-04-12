package com.sap.sse.datamining.ui;

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
    String run();
    String useClassGetNameTooltip();
    String useClassGetName();
    String useStringLiterals();
    String useStringLiteralsTooltip();
    String copyToClipboard();
    String code();
    String queryDefinitionViewer();
    String runAsSubstantive();
    String queryNotValidBecause();
    String queryRunner();
    String floatNumber();
    String integer();
    String runAQuery();
    String runningQuery();
    String choosePresentation();
    String plainText();
    String groupName();
    String valueAscending();
    String valueDescending();
    String groupAverageAscending();
    String groupAverageDescending();
    String groupMedianAscending();
    String groupMedianDescending();
    String shownDecimals();
    String queryResultsChartSubtitle(int retrievedDataAmount, double calculationTime);
    String elements(long count);
    String dataMiningErrorMargins();
    String dataMiningResult();
    String resultsChart();
    String cantDisplayDataOfType(String resultType);
    String sortBy();
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
    String groupBy();
    String groupingProvider();
    String clearSelection();
    String basedOn();
    String chooseDifferentDimensionTitle();
    String chooseDifferentDimensionMessage();
    String pleaseSelectADimension();
    String queryDefinitionProvider();
    String dataMiningRetrieval();
    String tabbedResultsPresenter();
    String angleInDegree();
    String angleInRadian();
    String centralAngleInRadian();
    String centralAngleInDegree();    
}
