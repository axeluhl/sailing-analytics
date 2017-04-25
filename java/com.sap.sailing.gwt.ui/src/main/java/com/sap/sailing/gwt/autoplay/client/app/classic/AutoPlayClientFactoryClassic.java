package com.sap.sailing.gwt.autoplay.client.app.classic;

import com.sap.sailing.gwt.autoplay.client.app.AutoPlayClientFactory;
import com.sap.sailing.gwt.home.communication.SailingDispatchSystem;

public interface AutoPlayClientFactoryClassic extends AutoPlayClientFactory<ClassicPlaceNavigator> {

    void setSlideContext(ClassicContext configurationSixtyInch);

    ClassicContext getSlideCtx();

    SailingDispatchSystem getDispatch();
}
