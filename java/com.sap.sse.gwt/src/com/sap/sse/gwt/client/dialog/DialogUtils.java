package com.sap.sse.gwt.client.dialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.gwt.event.dom.client.HasAllKeyHandlers;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SuggestBox;
import com.sap.sse.common.CountryCode;
import com.sap.sse.common.CountryCodeFactory;
import com.sap.sse.common.Util;

public abstract class DialogUtils {
    public static void linkEnterToButton(final Button button, HasAllKeyHandlers... widgets) {
        for (HasAllKeyHandlers widget : widgets) {
            widget.addKeyPressHandler(new KeyPressHandler() {
                @Override
                public void onKeyPress(KeyPressEvent event) {
                    if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
                        button.click();
                    }
                }
            });
        }
    }

    public static void linkEscapeToButton(final Button button, HasAllKeyHandlers... widgets) {
        for (HasAllKeyHandlers widget : widgets) {
            widget.addKeyPressHandler(new KeyPressHandler() {
                @Override
                public void onKeyPress(KeyPressEvent event) {
                    if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ESCAPE) {
                        button.click();
                    }
                }
            });
            widget.addKeyUpHandler(new KeyUpHandler() {
                @Override
                public void onKeyUp(KeyUpEvent event) {
                    if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ESCAPE) {
                        button.click();
                    }
                }
            });
        }
    }

    public static void addFocusUponKeyUpToggler(final SuggestBox focusable) {
        // this ensures that the value is copied into the TextBox.getValue() result and a ChangeEvent is fired
        focusable.addKeyUpHandler(e -> {
            focusable.setFocus(false);
            focusable.setFocus(true);
        });
    }

    public static void addFocusUponKeyUpToggler(final FocusWidget focusable) {
        focusable.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                focusable.setFocus(false);
                // this ensures that the value is copied into the TextBox.getValue() result and a ChangeEvent is fired
                focusable.setFocus(true);
            }
        });
    }
    
    public static void makeCountrySelection(final ListBox threeLetterIocCountryCode, String threeLetterIocCountryCodeToSelect) {
        CountryCodeFactory ccf = CountryCodeFactory.INSTANCE;
        int i=0;
        List<CountryCode> ccs = new ArrayList<CountryCode>();
        Util.addAll(ccf.getAll(), ccs);
        ccs.add(null); // representing no nationality (NONE / white flag)
        Collections.sort(ccs, new Comparator<CountryCode>() {
            @Override
            public int compare(CountryCode o1, CountryCode o2) {
                return Util.compareToWithNull(o1 == null ? null : o1.getThreeLetterIOCCode(), o2 == null ? null : o2.getThreeLetterIOCCode(), /* nullIsLess */ true);
            }
        });
        for (CountryCode cc : ccs) {
            if (cc == null) {
                threeLetterIocCountryCode.addItem("", ""); // the NONE country code that uses the empty, white flag
                if (threeLetterIocCountryCodeToSelect == null || threeLetterIocCountryCodeToSelect.isEmpty()) {
                    threeLetterIocCountryCode.setSelectedIndex(i);
                }
                i++;
            } else if (cc.getThreeLetterIOCCode() != null) {
                threeLetterIocCountryCode.addItem(cc.getThreeLetterIOCCode() + " " + cc.getName(), cc.getThreeLetterIOCCode());
                if (cc.getThreeLetterIOCCode().equals(threeLetterIocCountryCodeToSelect)) {
                    threeLetterIocCountryCode.setSelectedIndex(i);
                }
                i++;
            }
        }
    }

    public static CountryCode getSelectedCountry(ListBox issuingCountryListBox) {
        return issuingCountryListBox.getSelectedIndex() == -1 ? null :
            CountryCodeFactory.INSTANCE.getFromThreeLetterIOCName(issuingCountryListBox.getSelectedValue());
    }
}
