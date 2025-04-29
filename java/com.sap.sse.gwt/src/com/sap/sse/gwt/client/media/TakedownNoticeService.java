package com.sap.sse.gwt.client.media;

import com.sap.sse.common.media.TakedownNoticeRequestContext;

public interface TakedownNoticeService {

    void fileTakedownNotice(TakedownNoticeRequestContext takedownNoticeRequestContext);

    boolean isEmailAddressOfCurrentUserValidated();
}
