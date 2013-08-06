package com.sap.sailing.datamining;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import com.sap.sailing.datamining.impl.AverageAggregator;
import com.sap.sailing.datamining.impl.GroupByDimension;
import com.sap.sailing.datamining.impl.QueryImpl;
import com.sap.sailing.datamining.impl.SelectorImpl;
import com.sap.sailing.datamining.impl.SumAggregator;
import com.sap.sailing.datamining.impl.criterias.BoatClassSelectionCriteria;
import com.sap.sailing.datamining.impl.criterias.CompetitorNameSelectionCriteria;
import com.sap.sailing.datamining.impl.criterias.CompoundSelectionCriteria;
import com.sap.sailing.datamining.impl.criterias.LegNumberSelectionCriteria;
import com.sap.sailing.datamining.impl.criterias.LegTypeSelectionCriteria;
import com.sap.sailing.datamining.impl.criterias.NationalitySelectionCriteria;
import com.sap.sailing.datamining.impl.criterias.RaceSelectionCriteria;
import com.sap.sailing.datamining.impl.criterias.RegattaSelectionCriteria;
import com.sap.sailing.datamining.impl.criterias.SailIDSelectionCriteria;
import com.sap.sailing.datamining.impl.criterias.WildcardSelectionCriteria;
import com.sap.sailing.datamining.impl.criterias.WindStrengthSelectionCriteria;
import com.sap.sailing.datamining.impl.criterias.YearSelectionCriteria;
import com.sap.sailing.datamining.shared.AggregatorType;
import com.sap.sailing.datamining.shared.Dimension;
import com.sap.sailing.datamining.shared.StatisticType;
import com.sap.sailing.datamining.shared.WindStrength;
import com.sap.sailing.domain.common.LegType;

public class DataMiningFactory {
    
    public static Query createQuery(Map<Dimension, Collection<?>> selection) {
        return new QueryImpl(createSelector(selection), null, null, null);
    }
    
    protected static Grouper createGrouper(Dimension dimension) {
        return new GroupByDimension(dimension);
    }
    
    protected static Extractor createExtractor(StatisticType statisticType) {
        switch (statisticType) {
        case FixAmount:
            //TODO: This will be removed after some development.
            return new Extractor() {
                @Override
                public Collection<Double> extract(Collection<GPSFixWithContext> data) {
                    Collection<Double> extracedData = new ArrayList<Double>();
                    extracedData.add(new Double(data.size()));
                    return extracedData;
                }
            };
        }
        throw new IllegalArgumentException("Not yet implemented for the given statistic type: " + statisticType.toString());
    }
    
    protected static Aggregator createAggregator(AggregatorType aggregatorType) {
        switch (aggregatorType) {
        case Average:
            return new AverageAggregator();
        case Sum:
            return new SumAggregator();
            
        }
        throw new IllegalArgumentException("Not yet implemented for the given aggregator type: " + aggregatorType.toString());
    }

    protected static Selector createSelector(Map<Dimension, Collection<?>> selection) {
        return new SelectorImpl(createSelectionCriteria(selection));
    }

    protected static SelectionCriteria createSelectionCriteria(Map<Dimension, Collection<?>> selection) {
        if (selection.isEmpty()) {
            return new WildcardSelectionCriteria();
        }
        
        CompoundSelectionCriteria criteria = new CompoundSelectionCriteria();
        for (Entry<Dimension, Collection<?>> selectionEntry : selection.entrySet()) {
            if (selectionEntry.getValue() != null && !selectionEntry.getValue().isEmpty()) {
                criteria.addCriteria(createSelectionCriteria(selectionEntry.getKey(), selectionEntry.getValue()));
            }
        }
        return criteria;
    }
    
    @SuppressWarnings("unchecked")
    protected static <T> SelectionCriteria createSelectionCriteria(Dimension dimension, Collection<T> selection) {
        switch (dimension) {
        case RegattaName:
            return new RegattaSelectionCriteria((Collection<String>) selection);
        case BoatClassName:
            return new BoatClassSelectionCriteria((Collection<String>) selection);
        case CompetitorName:
            return new CompetitorNameSelectionCriteria((Collection<String>) selection);
        case LegNumber:
            return new LegNumberSelectionCriteria((Collection<Integer>) selection);
        case LegType:
            return new LegTypeSelectionCriteria((Collection<LegType>) selection);
        case Nationality:
            return new NationalitySelectionCriteria((Collection<String>) selection);
        case RaceName:
            return new RaceSelectionCriteria((Collection<String>) selection);
        case SailID:
            return new SailIDSelectionCriteria((Collection<String>) selection);
        case WindStrength:
            return new WindStrengthSelectionCriteria((Collection<WindStrength>) selection);
        case Year:
            return new YearSelectionCriteria((Collection<Integer>) selection);
        }
        throw new IllegalArgumentException("Not yet implemented for the given dimension: " + dimension.toString());
    }
}
