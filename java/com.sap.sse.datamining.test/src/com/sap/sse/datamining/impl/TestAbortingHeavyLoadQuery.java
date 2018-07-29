package com.sap.sse.datamining.impl;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.junit.Before;
import org.junit.Test;

import com.sap.sse.datamining.Query;
import com.sap.sse.datamining.QueryState;
import com.sap.sse.datamining.components.AdditionalResultDataBuilder;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.components.ProcessorInstruction;
import com.sap.sse.datamining.components.ProcessorInstructionHandler;
import com.sap.sse.datamining.data.QueryResult;
import com.sap.sse.datamining.impl.components.AbstractParallelProcessor;
import com.sap.sse.datamining.impl.components.AbstractProcessorInstruction;
import com.sap.sse.datamining.impl.components.AbstractRetrievalProcessor;
import com.sap.sse.datamining.impl.components.GroupedDataEntry;
import com.sap.sse.datamining.impl.components.ProcessorInstructionPriority;
import com.sap.sse.datamining.impl.components.aggregators.ParallelGroupedDataCollectingAsSetProcessor;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.data.QueryResultState;
import com.sap.sse.datamining.shared.impl.GenericGroupKey;
import com.sap.sse.datamining.test.util.components.StatefulBlockingInstruction;

public class TestAbortingHeavyLoadQuery {
    
    private static final int ExecutorPoolSize = Math.min(3, Runtime.getRuntime().availableProcessors() - 2);
    private static final int DataSourceSize = 2000;
    private static final String GroupKeyPrefix = "G";
    private static final long HeavyLoadInstructionDuration = 500;
    private static final long AbortQueryDelay = (long) (HeavyLoadInstructionDuration * 1.1);
    
    private static final boolean RecordExecution = true;
    private ConcurrentLinkedQueue<String> executionRecord;
    
    private ExecutorService executor;
    private Query<HashSet<Integer>> query;
    private Collection<Processor<?, ?>> processors;
    
    @Test
    public void testAbortingHeavyLoadQuery() throws InterruptedException, ExecutionException {
        RunnableFuture<QueryResult<HashSet<Integer>>> queryTask = new FutureTask<>(() -> { 
            logExecution("Starting query execution");
            long start = System.currentTimeMillis();
            QueryResult<HashSet<Integer>> result = query.run();
            long duration = System.currentTimeMillis() - start;
            logExecution("Finished query in " + duration + "ms");
            return result;
        });
        Thread worker = new Thread(queryTask, "Worker");
        worker.start();
        do {
            Thread.sleep(10);
        } while (query.getState() != QueryState.RUNNING);
        
        Thread.sleep(AbortQueryDelay);
        logExecution("Aborting query");
        query.abort();
        QueryResult<HashSet<Integer>> result = queryTask.get();
        assertThat(result.getState(), is(QueryResultState.ABORTED));
        assertTrue("The result is not empty", result.isEmpty());
        for (Processor<?, ?> processor : processors) {
            assertTrue("Processor wasn't aborted", processor.isAborted());
        }

        // TODO Ensure that no instructions are scheduled after the query has been aborted 
        // TODO More detailed assessment of the leftover instructions
        
        executor.shutdown();
        boolean terminated = executor.awaitTermination((long) (HeavyLoadInstructionDuration * 1.5), TimeUnit.MILLISECONDS);
        
        if (RecordExecution) {
            for (String string : executionRecord) {
                System.out.println(string);
            } 
        }
        assertTrue("The executor didn't terminate in the given time", terminated);
    }

    private void logExecution(String message) {
        if (RecordExecution) {
            executionRecord.add(Thread.currentThread().getName() + ": " + message);
        }
    }
    
    @Before
    @SuppressWarnings("unchecked")
    public void initialize() {
        executor = new DataMiningExecutorService(ExecutorPoolSize);
//        executor = new ThreadPoolExecutor(ExecutorPoolSize, ExecutorPoolSize, 0, TimeUnit.MILLISECONDS, new PriorityBlockingQueue<>());
//        executor = new ThreadPoolExecutor(ExecutorPoolSize, ExecutorPoolSize, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
        if (RecordExecution) {
            executionRecord = new ConcurrentLinkedQueue<>();
        }
        processors = new ArrayList<>();
        
        Class<Iterable<String>> dataSourceType = (Class<Iterable<String>>)(Class<?>) Iterable.class;
        Class<GroupedDataEntry<Integer>> groupedType = (Class<GroupedDataEntry<Integer>>)(Class<?>) GroupedDataEntry.class;
        Class<HashSet<Integer>> resultType = (Class<HashSet<Integer>>)(Class<?>) HashSet.class;
        
        Collection<String> dataSource = new ArrayList<>(DataSourceSize);
        for (int i = 0; i < DataSourceSize; i++) {
            dataSource.add(GroupKeyPrefix + i);
        }
        
        query = new ProcessorQuery<HashSet<Integer>, Iterable<String>>(dataSource, resultType) {
            @Override
            protected Processor<Iterable<String>, ?> createChainAndReturnFirstProcessor(Processor<Map<GroupKey, HashSet<Integer>>, Void> resultReceiver) {
                Processor<GroupedDataEntry<Integer>, Map<GroupKey, HashSet<Integer>>> aggregator = new ParallelGroupedDataCollectingAsSetProcessor<Integer>(executor, Collections.singleton(resultReceiver)) {
                    @Override
                    protected void storeElement(GroupedDataEntry<Integer> element) {
                        logExecution("Storing " + element);
                        super.storeElement(element);
                    }
                };
                
                Processor<Element, GroupedDataEntry<Integer>> grouper = new AbstractParallelProcessor<Element, GroupedDataEntry<Integer>>(Element.class, groupedType, executor, Collections.singleton(aggregator)) {
                    @Override
                    protected ProcessorInstruction<GroupedDataEntry<Integer>> createInstruction(Element element) {
                        return new AbstractProcessorInstruction<GroupedDataEntry<Integer>>(this, ProcessorInstructionPriority.Grouping) {
                            @Override
                            protected GroupedDataEntry<Integer> computeResult() throws Exception {
                                logExecution("Grouping " + element);
                                return new GroupedDataEntry<>(new GenericGroupKey<>(element.getName()), element.getValue());
                            }
                        };
                    }
                    @Override
                    protected void setAdditionalData(AdditionalResultDataBuilder additionalDataBuilder) { }
                };
                
                Processor<Element, Element> heavyLoadProcessor = new AbstractParallelProcessor<Element, Element>(Element.class, Element.class, executor, Collections.singleton(grouper)) {
                    @Override
                    protected ProcessorInstruction<Element> createInstruction(Element element) {
                        ProcessorInstruction<Element> instruction = new RecordingBlockingInstruction(this,
                                ProcessorInstructionPriority.Extraction, HeavyLoadInstructionDuration, element,
                                TestAbortingHeavyLoadQuery.this::logExecution);
                        return instruction;
                    }
                    @Override
                    protected void setAdditionalData(AdditionalResultDataBuilder additionalDataBuilder) { }
                    
                };
                
                Processor<String, Element> retriever1 = new AbstractRetrievalProcessor<String, Element>(String.class, Element.class, executor, Collections.singleton(heavyLoadProcessor), 1) {
                    @Override
                    protected Iterable<Element> retrieveData(String element) {
                        logExecution("Retrieving " + ExecutorPoolSize + " elements from " + element);
                        Collection<Element> data = new ArrayList<>();
                        for (int i = 0; i < ExecutorPoolSize; i++) {
                            data.add(new Element(element, i));
                        }
                        return data;
                    }
                    
                };
                Processor<Iterable<String>, String> retriever0 = new AbstractRetrievalProcessor<Iterable<String>, String>(dataSourceType, String.class, executor, Collections.singleton(retriever1), 0) {
                    @Override
                    protected Iterable<String> retrieveData(Iterable<String> element) {
                        logExecution("Retrieving data from data source");
                        return element;
                    }
                };

                processors.add(retriever0);
                processors.add(retriever1);
                processors.add(heavyLoadProcessor);
                processors.add(grouper);
                processors.add(aggregator);
                
                return retriever0;
            }
        };
    }
    
    private static class RecordingBlockingInstruction extends StatefulBlockingInstruction<Element> {

        private final Consumer<String> recorder;

        public RecordingBlockingInstruction(ProcessorInstructionHandler<Element> handler,
                ProcessorInstructionPriority priority, long blockDuration, Element result, Consumer<String> recorder) {
            super(handler, priority, blockDuration, result);
            this.recorder = recorder;
        }
        
        @Override
        public void run() {
            recorder.accept("Executing heavy load instruction for " + result);
            super.run();
        }
        
        @Override
        protected void actionBeforeBlock() {
            recorder.accept("Starting work for heavy load instruction for " + result);
        }
        
        @Override
        protected void actionAfterBlock() {
            recorder.accept("Finished heavy load instruction for " + result);
        }
        
    }
    
    private static class Element {
        
        private final String name;
        private final int value;
        
        public Element(String name, int value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public int getValue() {
            return value;
        }
        
        @Override
        public String toString() {
            return getName() + "-" + getValue();
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((name == null) ? 0 : name.hashCode());
            result = prime * result + value;
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Element other = (Element) obj;
            if (name == null) {
                if (other.name != null)
                    return false;
            } else if (!name.equals(other.name))
                return false;
            if (value != other.value)
                return false;
            return true;
        }
        
    }

}
