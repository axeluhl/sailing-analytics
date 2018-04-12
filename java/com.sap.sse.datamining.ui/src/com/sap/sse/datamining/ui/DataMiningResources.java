package com.sap.sse.datamining.ui;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

public interface DataMiningResources extends ClientBundle { // TODO extract resource files

    @Source("com/sap/sailing/gwt/ui/client/images/close.png")
    ImageResource closeIcon();

    @Source("com/sap/sailing/gwt/ui/client/images/arrow_left.png")
    ImageResource arrowLeftIcon();

    @Source("com/sap/sailing/gwt/ui/client/images/arrow_right.png")
    ImageResource arrowRightIcon();

    @Source("com/sap/sailing/gwt/ui/client/images/plusicon_small.png")
    ImageResource plusIcon();

    @Source("com/sap/sailing/gwt/ui/client/images/magnifier_small.png")
    ImageResource searchIcon();

}
