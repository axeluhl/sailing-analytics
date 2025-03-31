package com.sap.sse.datamining.ui.client.developer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.google.gwt.safehtml.shared.OnlyToBeUsedInGeneratedCodeStringBlessedAsSafeHtml;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.sap.sse.common.settings.SerializableSettings;
import com.sap.sse.datamining.shared.dto.StatisticQueryDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.AggregationProcessorDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.ClusterDTO;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverChainDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverLevelDTO;
import com.sap.sse.datamining.shared.impl.dto.FunctionDTO;
import com.sap.sse.datamining.shared.impl.dto.LocalizedTypeDTO;
import com.sap.sse.datamining.shared.impl.dto.ModifiableStatisticQueryDefinitionDTO;

public class QueryDefinitionParser {
    
    private static final String NullComponentText = "None selected";
    private static final String NullComponentCode = "UNDEFINED";
    
    public SafeHtml parseToDetailsAsSafeHtml(StatisticQueryDefinitionDTO queryDefinition) {
        String safeHtml = parseToDetails(queryDefinition, new HtmlBuilder());
        return new OnlyToBeUsedInGeneratedCodeStringBlessedAsSafeHtml(safeHtml);
    }
    
    public String parseToDetailsAsText(StatisticQueryDefinitionDTO queryDefinition) {
        return parseToDetails(queryDefinition, new TextBuilder());
    }
    
    private String parseToDetails(StatisticQueryDefinitionDTO queryDefinition, Builder builder) {
        if (queryDefinition == null) {
            return "";
        }
        
        builder.appendText("Locale: " + queryDefinition.getLocaleInfoName())
                .appendLineBreak().appendLineBreak();
        
        DataRetrieverChainDefinitionDTO chainDefinition = queryDefinition.getDataRetrieverChainDefinition();
        builder.appendText("Retrieval: ");
        if (chainDefinition != null) {
            builder.appendText(chainDefinition.toString()).appendLineBreak()
                   .appendTab(1).appendText("Levels: ").appendLineBreak();
            for (int levelIndex = 0; levelIndex < chainDefinition.getLevelAmount(); levelIndex++) {
                builder.appendTab(2).appendText(levelIndex + ": " + chainDefinition.getRetrieverLevel(levelIndex)).appendLineBreak();
            }
        } else {
            builder.appendText(NullComponentText).appendLineBreak();
        }
        builder.appendLineBreak();
         
        HashMap<DataRetrieverLevelDTO, SerializableSettings> retrieverSettings = queryDefinition.getRetrieverSettings();
        if (retrieverSettings != null && !retrieverSettings.isEmpty()) {
            // TODO Display retriever settings
        }
        
        HashMap<DataRetrieverLevelDTO,HashMap<FunctionDTO,HashSet<? extends Serializable>>> filterSelection = queryDefinition.getFilterSelection();
        if (filterSelection != null && !filterSelection.isEmpty()) {
            builder.appendText("Filter Selection:").appendLineBreak();
            List<DataRetrieverLevelDTO> retrieverLevels = new ArrayList<>(filterSelection.keySet());
            Collections.sort(retrieverLevels);
            for (DataRetrieverLevelDTO retrieveLevel : retrieverLevels) {
                builder.appendTab(1).appendText("Level " + retrieveLevel.getLevel() + ": ").appendLineBreak();
                HashMap<FunctionDTO, HashSet<? extends Serializable>> levelFilterSelection = filterSelection.get(retrieveLevel);
                for (FunctionDTO dimension : levelFilterSelection.keySet()) {
                    builder.appendTab(2).appendText(dimension.toString()).appendLineBreak()
                           .appendTab(3).appendText("Values: ");
                    boolean first = true;
                    for (Serializable value : levelFilterSelection.get(dimension)) {
                        if (!first) {
                            builder.appendText(", ");
                        }
                        builder.appendText(value.toString());
                        first = false;
                    }
                    builder.appendLineBreak();
                }
            }
            builder.appendLineBreak();
        }

        ArrayList<FunctionDTO> dimensionsToGroupBy = queryDefinition.getDimensionsToGroupBy();
        builder.appendText("Group By: ");
        if (dimensionsToGroupBy != null && !dimensionsToGroupBy.isEmpty()) {
            builder.appendLineBreak();
            for (int index = 0; index < dimensionsToGroupBy.size(); index++) {
                builder.appendTab(1).appendText(index + ": " + dimensionsToGroupBy.get(index)).appendLineBreak();
            }
        } else {
            builder.appendText(NullComponentText).appendLineBreak();
        }
        builder.appendLineBreak();
        
        FunctionDTO statistic = queryDefinition.getStatisticToCalculate();
        builder.appendText("Statistic: " + (statistic == null ? NullComponentText : statistic)).appendLineBreak();
        AggregationProcessorDefinitionDTO aggregator = queryDefinition.getAggregatorDefinition();
        builder.appendText("Aggregator: " + (aggregator == null ? NullComponentText : aggregator));
        
        return builder.toString();
    }

    public SafeHtml parseToCodeAsSafeHtml(StatisticQueryDefinitionDTO queryDefinition, TypeToCodeStrategy typeStrategy) {
        String safeHtml = parseToCode(queryDefinition, typeStrategy, new HtmlBuilder());
        return new OnlyToBeUsedInGeneratedCodeStringBlessedAsSafeHtml(safeHtml);
    }
    
    public String parseToCodeAsText(StatisticQueryDefinitionDTO queryDefinition, TypeToCodeStrategy typeStrategy) {
        return parseToCode(queryDefinition, typeStrategy, new TextBuilder());
    }
    
    private String parseToCode(StatisticQueryDefinitionDTO queryDefinition, TypeToCodeStrategy typeStrategy, Builder builder) {
        if (queryDefinition == null) {
            return "";
        }
        
        String functionClassName = FunctionDTO.class.getSimpleName();
        String aggregatorClassName = AggregationProcessorDefinitionDTO.class.getSimpleName();
        String retrieverLevelClassName = DataRetrieverLevelDTO.class.getSimpleName();
        String arrayListClassName = ArrayList.class.getSimpleName();
        String localizedTypeClassName = LocalizedTypeDTO.class.getSimpleName();
        String retrieverChainClassName = DataRetrieverChainDefinitionDTO.class.getSimpleName();
        String queryDefinitionClassName = ModifiableStatisticQueryDefinitionDTO.class.getSimpleName();
        String hashMapClassName = HashMap.class.getSimpleName();
        String hashSetClassName = HashSet.class.getSimpleName();
        String serializableClassName = Serializable.class.getSimpleName();

        // Extraction Function
        String statisticVariable = "statistic";
        builder.appendText(functionToCode(statisticVariable, queryDefinition.getStatisticToCalculate(), typeStrategy));
        builder.appendLineBreak();
        // Aggregator Definition
        String aggregatorVariable = "aggregator";
        AggregationProcessorDefinitionDTO aggregator = queryDefinition.getAggregatorDefinition();
        builder.appendText(aggregatorClassName + " " + aggregatorVariable + " = ");
        if (aggregator != null) {
            builder.appendText("new " + aggregatorClassName + "(" + literal(aggregator.getMessageKey()) + ", "
                    + typeStrategy.toCode(aggregator.getExtractedTypeName()) + ", "
                    + typeStrategy.toCode(aggregator.getAggregatedTypeName()) + ", \"\")");
        } else {
            builder.appendText(NullComponentCode);
        }
        builder.appendText(";").appendLineBreak().appendLineBreak();
        
        // Retriever Levels and Retriever Chain Definition
        DataRetrieverChainDefinitionDTO retrieverChain = queryDefinition.getDataRetrieverChainDefinition();
        String retrieverChainVariable = "retrieverChain";
        
        if (retrieverChain != null) {
            String retrieverLevelsVariable = "retrieverLevels";
            builder.appendText(arrayListClassName + "<" + retrieverLevelClassName + "> " + retrieverLevelsVariable
                    + " = new " + arrayListClassName + "<>();").appendLineBreak();
            for (DataRetrieverLevelDTO retrieverLevel : retrieverChain.getRetrieverLevels()) {
                LocalizedTypeDTO retrievedType = retrieverLevel.getRetrievedDataType();
                String retrievedTypeAsCode = "new " + localizedTypeClassName + "("
                        + typeStrategy.toCode(retrievedType.getTypeName()) + ", \"\")";
                String retrieverSettingsAsCode = "null";
                // TODO Handle the retriever level settings
                builder.appendText(retrieverLevelsVariable + ".add(new " + retrieverLevelClassName + "("
                        + retrieverLevel.getLevel() + ", " + typeStrategy.toCode(retrieverLevel.getRetrieverTypeName())
                        + ", " + retrievedTypeAsCode + ", " + retrieverSettingsAsCode + "));").appendLineBreak();
            }
            builder.appendText(retrieverChainClassName + " " + retrieverChainVariable + " = new "
                    + retrieverChainClassName + "(\"\", " + typeStrategy.toCode(retrieverChain.getDataSourceTypeName())
                    + ", " + retrieverLevelsVariable + ");");
        } else {
            builder.appendText(retrieverChainClassName + " " + retrieverChainVariable + " = " + NullComponentCode + ";");
        }
        builder.appendLineBreak().appendLineBreak();
        
        // Query Definition instantiation
        String queryDefinitionVariable = "queryDefinition";
        builder.appendText(queryDefinitionClassName + " " + queryDefinitionVariable + " = new "
                + queryDefinitionClassName + "(" + literal(queryDefinition.getLocaleInfoName()) + ", "
                + statisticVariable + ", " + aggregatorVariable + ", " + retrieverChainVariable + ");")
                .appendLineBreak().appendLineBreak();
        
        // Filter Selection per Retriever Level separated by two line breaks
        HashMap<DataRetrieverLevelDTO, HashMap<FunctionDTO, HashSet<? extends Serializable>>> filterSelection = queryDefinition.getFilterSelection();
        if (filterSelection != null && retrieverChain != null) {
            for (DataRetrieverLevelDTO retrieverLevel : retrieverChain.getRetrieverLevels()) {
                HashMap<FunctionDTO, HashSet<? extends Serializable>> levelFilterSelection = filterSelection.get(retrieverLevel);
                if (levelFilterSelection != null && !levelFilterSelection.isEmpty()) {
                    String levelFilterSelectionVariable = "retrieverlevel" + retrieverLevel.getLevel() + "_FilterSelection";
                    builder.appendText(hashMapClassName + "<" + functionClassName + ", " + hashSetClassName
                            + "<? extends " + serializableClassName + ">> " + levelFilterSelectionVariable + " = new "
                            + hashMapClassName + "<>();").appendLineBreak();

                    int filterDimensionCounter = 0;
                    for (FunctionDTO filterDimension : levelFilterSelection.keySet()) {
                        String filterDimensionVariable = "filterDimension" + filterDimensionCounter;
                        builder.appendText(functionToCode(filterDimensionVariable, filterDimension, typeStrategy))
                                .appendLineBreak();

                        String dimensionFilterSelectionVariable = filterDimensionVariable + "_Selection";
                        builder.appendText(hashSetClassName + "<" + serializableClassName + "> "
                                + dimensionFilterSelectionVariable + " = new " + hashSetClassName + "<>();")
                                .appendLineBreak();
                        for (Serializable filterValue : levelFilterSelection.get(filterDimension)) {
                            builder.appendText(dimensionFilterSelectionVariable + ".add(" + literal(filterValue) + ");")
                                    .appendLineBreak();
                        }

                        builder.appendText(levelFilterSelectionVariable + ".put(" + filterDimensionVariable + ", "
                                + dimensionFilterSelectionVariable + ");").appendLineBreak();
                        filterDimensionCounter++;
                    }

                    builder.appendText(queryDefinitionVariable + ".setFilterSelectionFor(" + retrieverChainVariable
                            + ".getRetrieverLevel(" + retrieverLevel.getLevel() + "), " + levelFilterSelectionVariable
                            + ");").appendLineBreak().appendLineBreak();
                }

            }
        }
        
        // Dimensions to Group By separated by two line breaks
        ArrayList<FunctionDTO> dimensionsToGroupBy = queryDefinition.getDimensionsToGroupBy();
        if (dimensionsToGroupBy != null) {
            int dimensionToGroupByCounter = 0;
            boolean first = true;
            for (FunctionDTO dimensionToGroupBy : dimensionsToGroupBy) {
                if (!first) {
                    builder.appendLineBreak().appendLineBreak();
                }

                String dimensionToGroupByVariable = "dimensionToGroupBy" + dimensionToGroupByCounter;
                builder.appendText(functionToCode(dimensionToGroupByVariable, dimensionToGroupBy, typeStrategy))
                        .appendLineBreak();
                builder.appendText(
                        queryDefinitionVariable + ".appendDimensionToGroupBy(" + dimensionToGroupByVariable + ");");

                first = false;
                dimensionToGroupByCounter++;
            }
        }
        
        return builder.toString();
    }
    
    private String functionToCode(String functionVariable, FunctionDTO function, TypeToCodeStrategy typeStrategy) {
        String functionClassName = FunctionDTO.class.getSimpleName();
        String functionConstruction;
        if (function != null) {
            functionConstruction = "new " + functionClassName + "(" + function.isDimension() + ", " + literal(function.getFunctionName()) + ", " +
                    typeStrategy.toCode(function.getSourceTypeName()) + ", " + typeStrategy.toCode(function.getReturnTypeName()) + ", " +
                    functionParametersToCode(function, typeStrategy) + ", \"\", 0)";
        } else {
            functionConstruction = NullComponentCode;
        }
        return functionClassName + " " + functionVariable + " = " + functionConstruction + ";"; 
    }
    
    private String functionParametersToCode(FunctionDTO function, TypeToCodeStrategy typeStrategy) {
        List<String> parameterTypeNames = function.getParameterTypeNames();
        if (parameterTypeNames.isEmpty()) {
            return "new " + ArrayList.class.getSimpleName() + "<" + String.class.getSimpleName() + ">()";
        }
        
        StringBuilder builder = new StringBuilder(Arrays.class.getSimpleName() + ".asList(");
        boolean first = true;
        for (String parameterTypeName : parameterTypeNames) {
            if (!first) {
                builder.append(", ");
            }
            builder.append(typeStrategy.toCode(parameterTypeName));
            first = false;
        }
        builder.append(")");
        return builder.toString();
    }
    
    public enum TypeToCodeStrategy {
        CLASS_GET_NAME {
            @Override
            public String toCode(String fullQualifiedTypeName) {
                String[] splittedTypeName = fullQualifiedTypeName.split("[.]");
                String typeName = splittedTypeName[splittedTypeName.length - 1];
                return typeName + ".class.getName()";
            }
        },
        STRING_LITERALS {
            @Override
            public String toCode(String fullQualifiedTypeName) {
                return literal(fullQualifiedTypeName);
            }
        };

        public abstract String toCode(String fullQualifiedTypeName);
    }
    
    private static String literal(Serializable value) {
        if (value instanceof String) {
            return literal((String) value);
        }
        if (value instanceof Integer) {
            return value.toString();
        }
        if (value instanceof Enum) {
            return value.getClass().getName()+".valueOf(\""+((Enum<?>) value).name()+"\")";
        }
        if (value instanceof ClusterDTO) {
            ClusterDTO cluster = (ClusterDTO) value;
            StringBuilder builder = new StringBuilder();
            builder.append("new ").append(ClusterDTO.class.getSimpleName()).append("(")
                       .append("\"").append(cluster.getSignifier()).append("\"").append(", ")
                       .append("\"").append(cluster.getLocalizedName()).append("\"")
                   .append(")");
            return builder.toString();
        }
        throw new IllegalArgumentException("Can't create literal for values of type " + value.getClass().getName());
    }
    
    private static String literal(String value) {
        return "\"" + value + "\"";
    }

    private static final String HTML_LINE_BREAK = "<br />";
    private static final String HTML_TAB = "&emsp;";
    
    private static final String TEXT_LINE_BREAK = "\n";
    private static final String TEXT_TAB = "\t";
    
    private interface Builder {
        
        Builder appendText(String text);
        Builder appendLineBreak();
        Builder appendTab(int tabAmount);
        
        String toString();
        
    }
    
    private class HtmlBuilder implements Builder {
        
        private final SafeHtmlBuilder builder;
        public HtmlBuilder() {
            builder = new SafeHtmlBuilder();
        }

        @Override
        public Builder appendText(String text) {
            builder.appendEscaped(text);
            return this;
        }
        @Override
        public Builder appendLineBreak() {
            builder.appendHtmlConstant(HTML_LINE_BREAK);
            return this;
        }
        @Override
        public Builder appendTab(int tabAmount) {
            builder.appendHtmlConstant(TAB(HTML_TAB, tabAmount));
            return this;
        }
        
        @Override
        public String toString() {
            return builder.toSafeHtml().asString();
        }
        
    }
    
    private class TextBuilder implements Builder {
        
        private final StringBuilder builder;
        public TextBuilder() {
            builder = new StringBuilder();
        }

        @Override
        public Builder appendText(String text) {
            builder.append(text);
            return this;
        }
        @Override
        public Builder appendLineBreak() {
            builder.append(TEXT_LINE_BREAK);
            return this;
        }
        @Override
        public Builder appendTab(int tabAmount) {
            builder.append(TAB(TEXT_TAB, tabAmount));
            return this;
        }
        
        @Override
        public String toString() {
            return builder.toString();
        }
        
    }
    
    private static String TAB(String tabCharacter, int tabAmount) {
        StringBuilder tabBuilder = new StringBuilder();
        for (int i = 0; i < tabAmount; i++) {
            tabBuilder.append(tabCharacter);
        }
        return tabBuilder.toString();
    }

}
