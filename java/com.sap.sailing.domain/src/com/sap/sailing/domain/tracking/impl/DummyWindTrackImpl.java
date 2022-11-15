package com.sap.sailing.domain.tracking.impl;

/**
 * @author Vladislav Chumak (D069712)
 *
 */
public class DummyWindTrackImpl extends WindTrackImpl {

    private static final long serialVersionUID = 1577541245541605202L;

    public DummyWindTrackImpl() {
        super(1000, 0.0, false, DummyWindTrackImpl.class.getName(), true);
    }

}
