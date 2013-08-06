package com.sap.sailing.datamining;

import java.util.Collection;

public interface Extractor {

    public Collection<Double> extract(Collection<GPSFixWithContext> data);

}
