package com.sap.sailing.landscape.ui.client;

import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Triple;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;

import com.sap.sailing.landscape.ui.client.i18n.StringMessages;

public class CreateReverseProxyInClusterDialog extends DataEntryDialog<String>{
    public CreateReverseProxyInClusterDialog(StringMessages stringMessages,DialogCallback<String> callback) {
        super(stringMessages.reverseProxies(), stringMessages.reverseProxies(), stringMessages.ok(), stringMessages.cancel(), new Validator<String>() {

            @Override
            public String getErrorMessage(String valueToValidate) {

                if (!Util.hasLength(valueToValidate)) {
                    return stringMessages.pleaseProvideNonEmptyName();
                } else {
                    CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder();
                    try {
                        decoder.decode(ByteBuffer.wrap(valueToValidate.getBytes()));
                    } catch (CharacterCodingException ex) {
                        return stringMessages.pleaseProvideNonEmptyName();
                    }
                    return null;

                }
            }
            
        }, callback);
    }

    @Override
    protected String getResult() {
        // TODO Auto-generated method stub
        return null;
    }
}
