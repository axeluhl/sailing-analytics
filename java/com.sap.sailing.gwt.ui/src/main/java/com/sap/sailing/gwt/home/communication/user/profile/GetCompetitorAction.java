package com.sap.sailing.gwt.home.communication.user.profile;

import java.util.UUID;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.CompetitorAndBoatStore;
import com.sap.sailing.gwt.home.communication.SailingAction;
import com.sap.sailing.gwt.home.communication.SailingDispatchContext;
import com.sap.sailing.gwt.home.communication.event.SimpleCompetitorWithIdDTO;
import com.sap.sse.gwt.dispatch.shared.exceptions.DispatchException;
import com.sap.sse.security.shared.impl.SecuredSecurityTypes;

/**
 * {@link SailingAction} implementation to load competitor data to be shown on the qr code page for the
 */
public class GetCompetitorAction implements SailingAction<SimpleCompetitorWithIdDTO> {

    private UUID id;

    @SuppressWarnings("unused")
    private GetCompetitorAction() {
    }

    /**
     * Creates a {@link GetCompetitorAction} instance with the given id.
     * 
     * @param id
     *            id to load the competitor for
     */
    public GetCompetitorAction(UUID id) {
        this.id = id;
    }

    @Override
    @GwtIncompatible
    public SimpleCompetitorWithIdDTO execute(SailingDispatchContext ctx) throws DispatchException {
        if (id != null) {
            CompetitorAndBoatStore competitorStore = ctx.getRacingEventService().getCompetitorAndBoatStore();
            Competitor competitor = competitorStore.getExistingCompetitorByIdAsString(id.toString());
            if (competitor != null) {
                ctx.getSecurityService().checkCurrentUserHasOneOfExplicitPermissions(competitor,
                        SecuredSecurityTypes.PublicReadableActions.READ_AND_READ_PUBLIC_ACTIONS);
            }
            return (competitor == null ? new SimpleCompetitorWithIdDTO(id.toString(), id.toString(), "", null, null)
                    : new SimpleCompetitorWithIdDTO(competitor));
        } else {
            return new SimpleCompetitorWithIdDTO("null", "null", "", null, null);
        }
    }

}
