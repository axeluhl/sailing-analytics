package com.sap.sailing.domain.igtimiadapter;

import com.sap.sailing.domain.igtimiadapter.datatypes.Fix;

/**
 * Notifies a {@link IgtimiFixReceiver receiver} about each fix received.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class FixVisitor implements BulkFixReceiver {
    private final IgtimiFixReceiver receiver;
    
    public FixVisitor(IgtimiFixReceiver receiver) {
        this.receiver = receiver;
    }

    @Override
    public void received(Iterable<Fix> fixes) {
        for (Fix fix : fixes) {
            fix.notify(receiver);
        }
    }

}
