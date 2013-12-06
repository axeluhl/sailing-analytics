package com.sap.sailing.domain.igtimiadapter;

import com.sap.sailing.domain.igtimiadapter.datatypes.Fix;

/**
 * Allows listeners to receive a batch of fixes that the connector received in one transaction. This will usually
 * improve chances that sensor readings belonging together (such as an AWA and an AWS fix) will be contained in the
 * same batch.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface BulkFixReceiver {
    void received(Iterable<Fix> fixes);
}
