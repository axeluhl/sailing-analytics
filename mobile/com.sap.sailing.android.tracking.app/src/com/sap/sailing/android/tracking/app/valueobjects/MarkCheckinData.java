package com.sap.sailing.android.tracking.app.valueobjects;

import com.sap.sailing.android.shared.data.CheckinUrlInfo;
import com.sap.sailing.domain.base.Mark;

public class MarkCheckinData extends CheckinData {

    public Mark mark;

    public MarkCheckinData(MarkUrlData markUrlData) {
        super(markUrlData);
        mark = markUrlData.getMark();
    }

    public Mark getMark() {
        return mark;
    }

    @Override
    public int getCheckinType() {
        return CheckinUrlInfo.TYPE_MARK;
    }
}
