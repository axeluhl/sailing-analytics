package com.sap.sailing.gwt.home.desktop.places.user.profile;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;

public interface UserProfileResources extends ClientBundle {

    public static final UserProfileResources INSTANCE = GWT.create(UserProfileResources.class);
    public static final UserProfileDesktopTemplates TEMPLATE = GWT.create(UserProfileDesktopTemplates.class);

    @Source("UserProfile.gss")
    UserProfileStyle css();

    public interface UserProfileDesktopTemplates extends SafeHtmlTemplates {

        @Template("<button type='button' tabindex='-1' class='gwt-Button' style='background-color: #e94a1b'>{0}</button>")
        SafeHtml removeButtonCell(SafeHtml title);

        @Template("<button type='button' tabindex='-1' class='gwt-Button' disabled='disabled'>{0}</button>")
        SafeHtml disabledButtonCell(SafeHtml title);
    }

    public interface UserProfileStyle extends CssResource {

        String overviewTable();

        String overviewTableEmpty();

        String overviewTableFooter();
    }
}
