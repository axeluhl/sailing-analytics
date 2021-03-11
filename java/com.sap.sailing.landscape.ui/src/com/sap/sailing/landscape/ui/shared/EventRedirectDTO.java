package com.sap.sailing.landscape.ui.shared;

import java.util.Optional;
import java.util.UUID;

/**
 * Redirects to a specific event's landing page. The ID is the event's ID.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class EventRedirectDTO extends RedirectWithIdDTO {
    @Deprecated
    EventRedirectDTO() {} // for GWT RPC only

    public EventRedirectDTO(UUID id) {
        super(id);
    }

    @Override
    public Optional<String> getQuery() {
        // TODO _=_ is a workaround only; see https://console.aws.amazon.com/support/home#/case/?displayId=8094019001&language=en
        return Optional.of("_=_&#{query}#/event/:eventId="+getId().toString());
    }
}
