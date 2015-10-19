package com.sap.sse.datamining.impl.components.management;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.sap.sse.datamining.components.management.QueryDefinitionDTORegistry;
import com.sap.sse.datamining.exceptions.DataMiningComponentAlreadyRegisteredForKeyException;
import com.sap.sse.datamining.shared.dto.StatisticQueryDefinitionDTO;
import com.sap.sse.datamining.shared.impl.PredefinedQueryIdentifier;

public class QueryDefinitionDTOManager implements QueryDefinitionDTORegistry {

    private static final Logger logger = Logger.getLogger(QueryDefinitionDTOManager.class.getName());
    
    private final Map<PredefinedQueryIdentifier, StatisticQueryDefinitionDTO> definitionDTOsMappedByName;
    
    public QueryDefinitionDTOManager() {
        definitionDTOsMappedByName = new HashMap<>();
    }

    @Override
    public Iterable<PredefinedQueryIdentifier> getIdentifiers() {
        return Collections.unmodifiableSet(definitionDTOsMappedByName.keySet());
    }

    @Override
    public StatisticQueryDefinitionDTO get(PredefinedQueryIdentifier identifier) {
        return definitionDTOsMappedByName.get(identifier);
    }
    
    @Override
    public boolean register(PredefinedQueryIdentifier identifier, StatisticQueryDefinitionDTO queryDefinition) {
        if (definitionDTOsMappedByName.containsKey(identifier)) {
            StatisticQueryDefinitionDTO registeredQueryDefinition = definitionDTOsMappedByName.get(identifier);
            if (!registeredQueryDefinition.equals(queryDefinition)) {
                throw new DataMiningComponentAlreadyRegisteredForKeyException(identifier, queryDefinition, registeredQueryDefinition);
            } else {
                return false;
            }
        }
        
        definitionDTOsMappedByName.put(identifier, queryDefinition);
        logger.info("Registering the predefined query defintion " + queryDefinition + " for name " + identifier);
        return true;
    }
    
    @Override
    public boolean unregister(PredefinedQueryIdentifier identifier, StatisticQueryDefinitionDTO queryDefinition) {
        boolean changed = false;
        StatisticQueryDefinitionDTO registeredQueryDefinition = definitionDTOsMappedByName.get(identifier);
        if (registeredQueryDefinition == null) {
            logger.info("Can't unregister " + queryDefinition + " because there is no query definition registered for the name " + identifier);
        } else if (!registeredQueryDefinition.equals(queryDefinition)) {
            logger.info("Can't unregister " + queryDefinition + " because theres the different query definition " + registeredQueryDefinition +
                        " registered for the name " + identifier);
        } else {
            logger.info("Unregistering data query definition " + queryDefinition + " for the name " + identifier);
            definitionDTOsMappedByName.remove(identifier);
            changed = true;
        }
        return changed;
    }

}
