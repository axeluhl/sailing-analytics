package com.sap.sse.security.ui.userprofile.shared.userdetails;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.google.gwt.dom.client.InputElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.security.ui.client.i18n.StringMessages;
import com.sap.sse.security.ui.shared.UserDTO;

/**
 * Base view class of the user account details page. This class implements the shared logic of the desktop and mobile
 * version of the page.
 * 
 * {@link UiField}s and {@link UiHandler}s are intentionally marked as public to make it visible to UiBinder in concrete
 * subclasses. These fields should not be accessed manually.
 */
public class AbstractUserDetails extends Composite implements UserDetailsView {
    
    @UiField public InputElement usernameUi;
    @UiField public TextBox nameUi;
    @UiField public TextBox companyUi;
    
    @UiField(provided = true)
    public ValueListBox<Pair<String, String>> localeUi = new ValueListBox<Pair<String, String>>(
            new Renderer<Pair<String, String>>() {
                @Override
                public String render(Pair<String, String> object) {
                    if (object == null) {
                        return "";
                    }
                    if (object.getB() != null) {
                        return object.getB();
                    }
                    return object.getA();
                }

                @Override
                public void render(Pair<String, String> object, Appendable appendable) throws IOException {
                    appendable.append(render(object));
                }
            });
    
    @UiField public TextBox emailUi;
    @UiField public PasswordTextBox oldPasswordUi;
    @UiField public PasswordTextBox newPasswordUi;
    @UiField public PasswordTextBox newPasswordConfirmationUi;
    
    private Presenter presenter;
    
    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }
    
    protected Presenter getPresenter() {
        return presenter;
    }
    
    @Override
    protected void onLoad() {
        super.onLoad();
        StringMessages i18n = StringMessages.INSTANCE;
        setPlaceholder(oldPasswordUi, i18n.oldPasswordPlaceholder());
        setPlaceholder(newPasswordUi, i18n.newPasswordPlaceholder());
        setPlaceholder(newPasswordConfirmationUi, i18n.passwordRepeatPlaceholder());
    }

    public void setUser(UserDTO currentUser) {
        nameUi.setValue(currentUser.getFullName());
        companyUi.setValue(currentUser.getCompany());
        usernameUi.setValue(currentUser.getName());
        emailUi.setValue(currentUser.getEmail());
        
        String currentLocale = currentUser.getLocale();
        Collection<Pair<String, String>> values = new ArrayList<>();
        Pair<String, String> selectedValue = null;
        for (String localeName : getAvailableLocales()) {
            Pair<String, String> value = new Pair<String, String>(localeName, LocaleInfo.getLocaleNativeDisplayName(localeName));
            values.add(value);
            if (currentLocale != null && currentLocale.equals(localeName)) {
                selectedValue = value;
            }
        }
        localeUi.setValue(selectedValue);
        localeUi.setAcceptableValues(values);
        
        clearPasswordFields();
    }
    
    // FIXME taken from GWTLocaleUtil -> move GWTLocaleUtil somwhere else so it can be seen here
    public static Iterable<String> getAvailableLocales() {
        List<String> result = new ArrayList<>();
        result.addAll(Arrays.asList(LocaleInfo.getAvailableLocaleNames()));
        result.remove("default");
        return result;
    }

    public void clearPasswordFields() {
        oldPasswordUi.setValue("");
        newPasswordUi.setValue("");
        newPasswordConfirmationUi.setValue("");
    }
    
    private void setPlaceholder(Widget widget, String placeholderText) {
        widget.getElement().setAttribute("placeholder", placeholderText);
    }
    
    @UiHandler("saveChangesUi")
    public void onSaveChangesClicked(ClickEvent event) {
        Pair<String, String> selectedLocaleValue = localeUi.getValue();
        String locale = selectedLocaleValue != null ? selectedLocaleValue.getA() : null;
        presenter.handleSaveChangesRequest(nameUi.getValue(), companyUi.getValue(), locale);
    }
    
    @UiHandler("changeEmailUi")
    public void onChangeEmailClicked(ClickEvent event) {
        presenter.handleEmailChangeRequest(emailUi.getValue());
    }
    
    @UiHandler("changePasswordUi")
    public void onChangePasswordClicked(ClickEvent event) {
        presenter.handlePasswordChangeRequest(oldPasswordUi.getValue(), newPasswordUi.getValue(),
                newPasswordConfirmationUi.getValue());
    }
}
