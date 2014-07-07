package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Different tracking provider connectors can use this to represent their DTOs for race records
 * representing loadable / trackable races.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public interface RaceRecordDTO extends IsSerializable {
    Iterable<String> getBoatClassNames();

    boolean hasRememberedRegatta();

    String getName();
}
