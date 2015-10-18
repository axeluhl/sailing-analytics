package com.sap.sse.datamining.impl.components.management;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.sap.sse.datamining.components.management.QueryDefinitionDTORegistry;
import com.sap.sse.datamining.exceptions.DataMiningComponentAlreadyRegisteredForKeyException;
import com.sap.sse.datamining.shared.dto.StatisticQueryDefinitionDTO;

public class QueryDefinitionDTOManager implements QueryDefinitionDTORegistry {

    private static final Logger logger = Logger.getLogger(QueryDefinitionDTOManager.class.getName());
    
    private final Map<String, StatisticQueryDefinitionDTO> definitionDTOsMappedByName;
    
    public QueryDefinitionDTOManager() {
        definitionDTOsMappedByName = new HashMap<>();
    }

    @Override
    public Iterable<String> getNames() {
        return Collections.unmodifiableSet(definitionDTOsMappedByName.keySet());
    }

    @Override
    public StatisticQueryDefinitionDTO get(String name) {
        return definitionDTOsMappedByName.get(name);
    }
    
    @Override
    public boolean register(String name, StatisticQueryDefinitionDTO queryDefinition) {
        if (definitionDTOsMappedByName.containsKey(name)) {
            StatisticQueryDefinitionDTO registeredQueryDefinition = definitionDTOsMappedByName.get(name);
            if (!registeredQueryDefinition.equals(queryDefinition)) {
                throw new DataMiningComponentAlreadyRegisteredForKeyException(name, queryDefinition, registeredQueryDefinition);
            } else {
                return false;
            }
        }
        
        definitionDTOsMappedByName.put(name, queryDefinition);
        logger.info("Registering the predefined query defintion " + queryDefinition + " for name " + name);
        return true;
    }
    
    @Override
    public boolean unregister(String name, StatisticQueryDefinitionDTO queryDefinition) {
        boolean changed = false;
        StatisticQueryDefinitionDTO registeredQueryDefinition = definitionDTOsMappedByName.get(name);
        if (registeredQueryDefinition == null) {
            logger.info("Can't unregister " + queryDefinition + " because there is no query definition registered for the name " + name);
        } else if (!registeredQueryDefinition.equals(queryDefinition)) {
            logger.info("Can't unregister " + queryDefinition + " because theres the different query definition " + registeredQueryDefinition +
                        " registered for the name " + name);
        } else {
            logger.info("Unregistering data query definition " + queryDefinition + " for the name " + name);
            definitionDTOsMappedByName.remove(name);
            changed = true;
        }
        return changed;
    }

}
