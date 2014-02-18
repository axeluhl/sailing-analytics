package com.sap.sailing.datamining.factories;

import java.util.Locale;
import java.util.concurrent.ThreadPoolExecutor;

import com.sap.sailing.datamining.builders.ExtractionWorkerBuilder;
import com.sap.sailing.datamining.impl.GroupDividingParallelExtractor;
import com.sap.sailing.datamining.shared.Components.StatisticType;
import com.sap.sse.datamining.components.ParallelExtractor;
import com.sap.sse.datamining.i18n.DataMiningStringMessages;
import com.sap.sse.datamining.shared.Message;
import com.sap.sse.datamining.shared.Unit;
import com.sap.sse.datamining.workers.ExtractionWorker;
import com.sap.sse.datamining.workers.WorkerBuilder;

public final class ExtractorFactory {
    
    private ExtractorFactory() { }
    
    public static <DataType, ExtractedType extends Number> ParallelExtractor<DataType, ExtractedType> createExtractor(DataMiningStringMessages stringMessages, Locale locale,
                                                                                                                      StatisticType statisticType, ThreadPoolExecutor executor) {
        WorkerBuilder<ExtractionWorker<DataType, ExtractedType>> workerBuilder = new ExtractionWorkerBuilder<DataType, ExtractedType>(statisticType);
        String signifier = statisticType.getUnit() == Unit.None ? stringMessages.get(locale, statisticType.getSignifierMessage()) : 
                                                                  stringMessages.get(locale, Message.SignifierInUnit, statisticType.getSignifierMessage(), statisticType.getUnitMessage());
        return new GroupDividingParallelExtractor<DataType, ExtractedType>(signifier, statisticType.getUnit(), statisticType.getValueDecimals(),
                                                                           workerBuilder, executor);
    }

}
