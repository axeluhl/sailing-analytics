package com.sap.sse.gwt.client;

import com.google.gwt.i18n.client.Messages;

/**
 * Defines the text strings for i18n that are used by the SSE GWT bundle. 
 * @author Axel Uhl (D043530)
 *
 */
public interface StringMessages extends Messages {
    String makeDefault();
    String makeDefaultInProgress();
    String settingsSavedMessage();
    String settingsSaveErrorMessage();
    String save();
    String remove();
    String ok();
    String cancel();
    String add();
    String edit();
    String serverError(); 
    String close();
    String remoteProcedureCall();
    String serverReplies();
    String errorCommunicatingWithServer();
    String configuration();
    String settings();
    String settingsForComponent(String localizedShortName);
    String pleaseSelect();

    String sharedSettingsLink();
    String resetToDefault();
    String resetToDefaultInProgress();
    String settingsRemoved();
    String settingsRemovedError();
    String pagerStateInfo(int start , int end, int size, @Select boolean exact);
    String yes();
    String no();
}
