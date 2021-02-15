package com.sap.sailing.gwt.managementconsole.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.event.shared.EventBus;
import com.sap.sailing.gwt.managementconsole.events.regatta.RegattaListResponseEvent;
import com.sap.sailing.gwt.managementconsole.events.regatta.RegattaListUpdateEvent;
import com.sap.sailing.gwt.ui.client.SailingServiceWriteAsync;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sse.gwt.client.ErrorReporter;

public class RegattaService {
    
    private static final Logger LOG = Logger.getLogger(RegattaService.class.getName());
    
    private final SailingServiceWriteAsync sailingService;
    private final ErrorReporter errorReporter;
    private final EventBus eventBus;
    
    private Map<String, RegattaDTO> regattaMap;
    
    public RegattaService(final SailingServiceWriteAsync sailingService,
            final ErrorReporter errorReporter,
            final EventBus eventBus) {
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        this.eventBus = eventBus;
        this.regattaMap = new HashMap<String, RegattaDTO>();
    }
    
    public List<RegattaDTO> getRegattas() {
        return new ArrayList<RegattaDTO>(regattaMap.values());
    }
    
    public void updateEvents(final List<RegattaDTO> regattas) {
        setRegattas(regattas);
    }
    
    public void requestRegattaList(UUID eventId, boolean forceRequestFromService) {
        if (forceRequestFromService || regattaMap.isEmpty()) {
            sailingService.getRegattasForEvent(eventId, new AsyncCallback<List<RegattaDTO>>() {
                @Override
                public void onFailure(Throwable caught) {
                    LOG.severe("requestRegattaList :: Cannot load regattas!");
                    errorReporter.reportError("Error", "Cannot load regattas!");
                }
    
                @Override
                public void onSuccess(List<RegattaDTO> result) {
                    LOG.info("requestRegattaList :: onSuccess");
                    setRegattas(result);
                    eventBus.fireEvent(new RegattaListResponseEvent(result));
                }
            });
        } else {
            eventBus.fireEvent(new RegattaListUpdateEvent(getRegattas()));
        }
    }
    
    private void setRegattas(final List<RegattaDTO> regattaList) {
        this.regattaMap = regattaList.stream().collect(Collectors.toMap(regatta -> regatta.getName(), Function.identity()));
    }
    
}
