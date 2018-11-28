package com.sap.sailing.gwt.home.communication.regatta;

import com.google.gwt.core.shared.GwtIncompatible;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.gwt.home.communication.SailingAction;
import com.sap.sailing.gwt.home.communication.SailingDispatchContext;
import com.sap.sse.gwt.dispatch.shared.exceptions.DispatchException;

/**
 * {@link SailingAction} to load regatta information to display on QRCode place for registering to an open regatta.
 * 
 *
 */
public class GetOpenRegattaByRegattaNameAction implements SailingAction<SimpleRegattaDTO> {

    private String regattaName;
    private String secret;

    @SuppressWarnings("unused")
    private GetOpenRegattaByRegattaNameAction() {
    }

    /**
     * Creates a {@link GetOpenRegattaByRegattaNameAction} instance with the given regatta name. The passed secret has
     * to match the secret of the open regatta.
     * 
     * @param id
     *            name of the regatta
     */
    public GetOpenRegattaByRegattaNameAction(String regattaName, String secret) {
        this.regattaName = regattaName;
        this.secret = secret;
    }

    @Override
    @GwtIncompatible
    public SimpleRegattaDTO execute(SailingDispatchContext ctx) throws DispatchException {
        Regatta regatta = ctx.getRacingEventService().getRegattaByName(regattaName);
        if (regatta == null || !regatta.getCompetitorRegistrationType().isOpen()
                || secret.equals(regatta.getRegistrationLinkSecret())) {
            throw new DispatchException("Regatta \"" + regattaName + "\" not found or not allowed for this action");
        }
        return new SimpleRegattaDTO(regatta.getName());
    }

}
