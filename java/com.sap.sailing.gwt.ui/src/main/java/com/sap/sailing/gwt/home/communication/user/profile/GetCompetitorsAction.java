package com.sap.sailing.gwt.home.communication.user.profile;

import java.util.ArrayList;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CompetitorAndBoatStore;
import com.sap.sailing.gwt.home.communication.SailingAction;
import com.sap.sailing.gwt.home.communication.SailingDispatchContext;
import com.sap.sailing.gwt.home.communication.event.SimpleCompetitorWithIdDTO;
import com.sap.sse.common.Util;
import com.sap.sse.gwt.dispatch.shared.commands.SortedSetResult;
import com.sap.sse.gwt.dispatch.shared.exceptions.DispatchException;

/**
 * {@link SailingAction} implementation to load competitor data to be shown on the preferences page for the
 * {@link #GetCompetitorSuggestionAction(String, int) given ids}.
 */
public class GetCompetitorsAction implements SailingAction<SortedSetResult<SimpleCompetitorWithIdDTO>> {
    
    private ArrayList<String> ids;
    
    @SuppressWarnings("unused")
    private GetCompetitorsAction() {
    }

    /**
     * Creates a {@link GetCompetitorsAction} instance with the given ids.
     * 
     * @param ids
     *            ids to load competitors for
     */
    public GetCompetitorsAction(Iterable<String> ids) {
        this.ids = (ArrayList<String>) Util.addAll(ids, new ArrayList<String>());
    }

    @Override
    @GwtIncompatible
    public SortedSetResult<SimpleCompetitorWithIdDTO> execute(SailingDispatchContext ctx) throws DispatchException {
        CompetitorAndBoatStore competitorStore = ctx.getRacingEventService().getCompetitorAndBoatStore();
        SortedSetResult<SimpleCompetitorWithIdDTO> result = new SortedSetResult<>();
        for (String id : ids) {
            Competitor competitor = competitorStore.getExistingCompetitorByIdAsString(id);
            result.addValue(competitor == null ? new SimpleCompetitorWithIdDTO(id, id, "", null, null)
                    : new SimpleCompetitorWithIdDTO(competitor));
        }
        return result;
    }
    
}
