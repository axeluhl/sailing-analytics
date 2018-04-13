package com.sap.sse.gwt.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.Messages;
import com.google.gwt.i18n.client.LocalizableResource.DefaultLocale;

/**
 * Defines the text strings for i18n that are used by the SSE GWT bundle. 
 * @author Axel Uhl (D043530)
 *
 */
@DefaultLocale("en")
public interface StringMessages extends Messages {
    public static final StringMessages INSTANCE = GWT.create(StringMessages.class);
    
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
    
    String details();
    String filterBy();
    String noDataFound();
    String results();
    String analyze();
    String reload();
    String empty();
    String error();

    String kilometers();
    String meters();
    String nauticalMiles();
    String seaMiles();
    String geographicalMiles();
    String days();
    String hours();
    String minutes();
    String seconds();
    String milliseconds();
}
