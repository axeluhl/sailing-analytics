package com.sap.sailing.datamining;

import java.util.Collection;

public interface Extractor<DataType, ExtractedType> {

    public Collection<ExtractedType> extract(Collection<DataType> data);

    public ExtractedType extract(DataType dataEntry);

}
