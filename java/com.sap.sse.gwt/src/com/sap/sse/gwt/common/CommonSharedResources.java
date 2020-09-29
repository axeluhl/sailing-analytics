package com.sap.sse.gwt.common;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.DataResource;
import com.google.gwt.resources.client.DataResource.MimeType;


public interface CommonSharedResources extends ClientBundle {
    public static final CommonSharedResources INSTANCE = GWT.create(CommonSharedResources.class);
    
    public static final String RESET = "com/sap/sse/gwt/common/common-reset.gss";
    public static final String MAIN = "com/sap/sse/gwt/common/common-main.gss";
    public static final String MEDIA = "com/sap/sse/gwt/common/common-media.gss";
    
    @Source({RESET, MAIN})
    CommonMainCss mainCss();

    @Source(MEDIA)
    CommonMediaCss mediaCss();

    @Source("buttonarrowrightwhite.png")
    @MimeType("image/png")
    DataResource buttonarrowrightwhite();
    
    public interface CommonMainCss extends CssResource {
        String button();
        String buttonprimary();
        String buttonprimaryoutlined();
        String buttonarrowrightwhite();
        
        String mainsection();
        String mainsection_header();
        String mainsection_header_title();
        String input();
        String input_label();
        String input_input();
        String input_inputerror();
        String input_errortext();
    }

    public interface CommonMediaCss extends CssResource {
        String grid();
        String stackenblochen();
        String column();
        String columns();
        String small1();
        String small2();
        String small3();
        String small4();
        String small5();
        String small6();
        String small7();
        String small8();
        String small9();
        String small10();
        String small11();
        String small12();
        String smalloffset0();
        String smalloffset1();
        String smalloffset2();
        String smalloffset3();
        String smalloffset4();
        String smalloffset5();
        String smalloffset6();
        String smalloffset7();
        String smalloffset8();
        String smalloffset9();
        String smalloffset10();
        String smalloffset11();
        String showonsmall();
        String hideonsmall();
        String showonmedium();
        String hideonmedium();
        String showonlarge();
        String hideonlarge();
        String smallcentered();
        String smalluncentered();
        String medium1();
        String medium2();
        String medium3();
        String medium4();
        String medium5();
        String medium6();
        String medium7();
        String medium8();
        String medium9();
        String medium10();
        String medium11();
        String medium12();
        String mediumoffset0();
        String mediumoffset1();
        String mediumoffset2();
        String mediumoffset3();
        String mediumoffset4();
        String mediumoffset5();
        String mediumoffset6();
        String mediumoffset7();
        String mediumoffset8();
        String mediumoffset9();
        String mediumoffset10();
        String mediumoffset11();
        String mediumcentered();
        String mediumuncentered();
        String large1();
        String large2();
        String large3();
        String large4();
        String large5();
        String large6();
        String large7();
        String large8();
        String large9();
        String large10();
        String large11();
        String large12();
        String largeoffset0();
        String largeoffset1();
        String largeoffset2();
        String largeoffset3();
        String largeoffset4();
        String largeoffset5();
        String largeoffset6();
        String largeoffset7();
        String largeoffset8();
        String largeoffset9();
        String largeoffset10();
        String largeoffset11();
        String largecentered();
        String largeuncentered();
    }

}