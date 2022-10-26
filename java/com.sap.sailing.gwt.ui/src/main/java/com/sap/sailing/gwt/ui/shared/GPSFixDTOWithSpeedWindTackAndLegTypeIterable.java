package com.sap.sailing.gwt.ui.shared;

import java.util.Iterator;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sse.common.Util;

/**
 * Instances of this class are proxy objects that can enumerate objects of type {@link GPSFixDTOWithSpeedWindTackAndLegType}
 * dynamically. The design goal is to preserve memory by only actually producing the {@link GPSFixDTOWithSpeedWindTackAndLegType}
 * objects when de-serializing on the client. All fields are {@code transient}, and a custom field serializer is used for a
 * highly proprietary, very compact and memory-conserving serialization process.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class GPSFixDTOWithSpeedWindTackAndLegTypeIterable implements IsSerializable, Iterable<GPSFixDTOWithSpeedWindTackAndLegType> {
    private final transient Iterable<GPSFixDTOWithSpeedWindTackAndLegType> list;

    public GPSFixDTOWithSpeedWindTackAndLegTypeIterable(Iterable<GPSFixDTOWithSpeedWindTackAndLegType> list) {
        this.list = list;
    }
    
    public boolean isEmpty() {
        final boolean result;
        if (list != null) {
            result = Util.isEmpty(list);
        } else {
            result = iterator().hasNext();
        }
        return result;
    }

    @Override
    public Iterator<GPSFixDTOWithSpeedWindTackAndLegType> iterator() {
        final Iterator<GPSFixDTOWithSpeedWindTackAndLegType> result;
        if (list != null) {
            result = list.iterator();
        } else {
            // TODO compute from proxy parameters
            result = null;
        }
        return result;
    }

    public GPSFixDTOWithSpeedWindTackAndLegType last() {
        final GPSFixDTOWithSpeedWindTackAndLegType result;
        if (list != null) {
            result = Util.last(list);
        } else {
            result = Util.last(this);
        }
        return result;
    }

}
