package com.sap.sse.landscape.aws.common.shared;

import java.util.Optional;
import java.util.UUID;

/**
 * Redirects to a specific event's landing page. The ID is the event's ID.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class EventRedirectDTO extends RedirectWithIdDTO {
    private static final long serialVersionUID = 3518358222589013410L;
    // TODO _=_ is a workaround only; see https://console.aws.amazon.com/support/home#/case/?displayId=8094019001&language=en
    private static final String QUERY_PREFIX = "_=_&#{query}#/event/:eventId=";

    @Deprecated
    EventRedirectDTO() {} // for GWT RPC only

    public EventRedirectDTO(UUID id) {
        super(id, Type.EVENT);
    }

    @Override
    public Optional<String> getQuery() {
        return Optional.of(QUERY_PREFIX+getId().toString());
    }
    
    @Override
    public void accept(RedirectVisitor visitor) throws Exception {
        visitor.visit(this);
    }

    static EventRedirectDTO parse(String redirectPath) {
        return parse(redirectPath, QUERY_PREFIX, EventRedirectDTO::new);
    }
}
