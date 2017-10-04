package com.sap.sailing.domain.common.tracking.impl;

import com.sap.sailing.domain.common.tracking.BravoExtendedFix;
import com.sap.sailing.domain.common.tracking.DoubleVectorFix;

public class BravoExtendedFixImpl extends BravoFixImpl implements BravoExtendedFix {
    private static final long serialVersionUID = 5622321493028301922L;

    public BravoExtendedFixImpl(DoubleVectorFix fix) {
        super(fix);
    }

}
