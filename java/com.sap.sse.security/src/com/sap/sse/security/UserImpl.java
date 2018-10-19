package com.sap.sse.security;

import java.io.Serializable;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import org.apache.shiro.crypto.hash.Sha256Hash;

import com.sap.sse.security.shared.Account;
import com.sap.sse.security.shared.Account.AccountType;
import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.QualifiedObjectIdentifier;
import com.sap.sse.security.shared.User;
import com.sap.sse.security.shared.UserGroup;
import com.sap.sse.security.shared.impl.SecuredSecurityTypes;
import com.sap.sse.security.shared.impl.SecurityUserImpl;

public class UserImpl extends SecurityUserImpl implements User {
    private static final long serialVersionUID = 1788215575606546042L;

    /**
     * An optional clear-text user name, used to address the user, e.g., in the UI ("Hello ...")
     */
    private String fullName;

    /**
     * An optional company affiliation. May be used, e.g., to better understand the statistics of
     * corporate vs. private users, if used as a marketing tool.
     */
    private String company;
    
    /**
     * An optional field specifying the locale preference of the user. This can be used to internationalize User
     * specific elements as UIs or notification mails.
     */
    private Locale locale;

    private String email;
    
    /**
     * When a new e-mail is set for the user, a validation process should be started.
     * The validation generates a secret which is then put into a URL which is sent to
     * the new e-mail address. When the user follows the URL, the URL parameter will be
     * used to validate against the secret stored here. If the secret matches, the
     * email address is {@link #emailValidated marked as validated}.
     */
    private String validationSecret;
    
    /**
     * When someone has requested a password reset, only the owner of the validated e-mail address is
     * permitted to actually carry out the reset. This is verified by sending a "reset secret" to the
     * validated e-mail address, giving the user a link to an entry point for actually carrying out the
     * reset. The reset is only accepted if the reset secret was provided correctly.
     */
    private String passwordResetSecret;
    
    private boolean emailValidated;

    private final Map<AccountType, Account> accounts;

    private transient UserGroupProvider userGroupProvider;

    public UserImpl(String name, String email, UserGroup defaultTenant, UserGroupProvider userGroupProvider, Account... accounts) {
        this(name, email, defaultTenant, Arrays.asList(accounts), userGroupProvider);
    }

    public UserImpl(String name, String email, UserGroup defaultTenant, Collection<Account> accounts, UserGroupProvider userGroupProvider) {
        this(name, email, /* fullName */ null, /* company */ null, /* locale */ null, /* is email validated */ false,
             /* password reset secret */ null, /* validation secret */ null, defaultTenant, accounts, userGroupProvider);
    }

    public UserImpl(String name, String email, String fullName, String company, Locale locale, Boolean emailValidated,
            String passwordResetSecret, String validationSecret, UserGroup defaultTenant, Collection<Account> accounts, UserGroupProvider userGroupProvider) {
        super(name, defaultTenant);
        this.fullName = fullName;
        this.company = company;
        this.locale = locale;
        this.email = email;
        this.passwordResetSecret = passwordResetSecret;
        this.validationSecret = validationSecret;
        this.emailValidated = emailValidated;
        this.accounts = new HashMap<>();
        this.userGroupProvider = userGroupProvider;
        for (Account a : accounts) {
            this.accounts.put(a.getAccountType(), a);
        }
    }
    
    /**
     * The main use case for this method is to restore the link to a {@link UserStore} after de-serialization, e.g.,
     * on a replica.
     */
    public void setUserGroupProvider(UserGroupProvider userGroupProvider) {
        this.userGroupProvider = userGroupProvider;
    }

    /**
     * For the time being, the user {@link #getName() name} is used as ID
     */
    @Override
    public Serializable getId() {
        return getName();
    }

    @Override
    public String getFullName() {
        return fullName;
    }

    @Override
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    @Override
    public String getCompany() {
        return company;
    }

    @Override
    public void setCompany(String company) {
        this.company = company;
    }
    
    @Override
    public Locale getLocale() {
        return locale;
    }
    
    @Override
    public void setLocale(Locale locale) {
        this.locale = locale;
    }
    
    @Override
    public Locale getLocaleOrDefault() {
        return locale == null ? Locale.ENGLISH : locale;
    }

    @Override
    public Account getAccount(AccountType type) {
        return accounts.get(type);
    }

    @Override
    public void removeAccount(AccountType type) {
        accounts.remove(type);
    }

    @Override
    public Map<AccountType, Account> getAllAccounts() {
        return Collections.unmodifiableMap(accounts);
    }

    @Override
    public String getEmail() {
        return email;
    }
    
    /**
     * Sets an e-mail address for this user. The address is considered not yet validated, therefore the
     * {@link #emailValidated} flag is reset, and a new {@link #validationSecret} is generated and returned which
     * can be used in a call to {@link #validate(String)} to validate the e-mail address.
     */
    @Override
    public String setEmail(String email) {
        this.email = email;
        return startEmailValidation();
    }

    /**
     * The email address is set to not yet validated by resetting the
     * {@link #emailValidated} flag. A new {@link #validationSecret} is generated and returned which
     * can be used in a call to {@link #validate(String)} to validate the e-mail address.
     */
    @Override
    public String startEmailValidation() {
        validationSecret = createRandomSecret();
        emailValidated = false;
        return validationSecret;
    }
    
    /**
     * Creates, remembers and returns a new password reset secret. This secret can later again be obtained
     * by calling {@link #getPasswordResetSecret()}. A user store should only allow a service call to reset
     * a user's password in case the service can provide the correct password reset secret.
     */
    @Override
    public String startPasswordReset() {
        passwordResetSecret = createRandomSecret();
        return passwordResetSecret;
    }
    
    @Override
    public String getPasswordResetSecret() {
        return passwordResetSecret;
    }
    
    private String createRandomSecret() {
        final byte[] bytes1 = new byte[64];
        new SecureRandom().nextBytes(bytes1);
        final byte[] bytes2 = new byte[64];
        new Random().nextBytes(bytes2);
        return new Sha256Hash(bytes1, bytes2, 1024).toBase64();
    }
    
    /**
     * If the <code>validationSecret</code> passed matches {@link #validationSecret}, the e-mail is
     * {@link #emailValidated marked as validated}, and <code>true</code> is returned. Otherwise, the validation secret
     * on this user remains in place, and the e-mail address is not marked as validated.
     */
    @Override
    public boolean validate(final String validationSecret) {
        final boolean result;
        if (emailValidated) {
            result = true;
        } else if (validationSecret.equals(this.validationSecret)) {
            emailValidated = true;
            this.validationSecret = null;
            result = true;
        } else {
            result = false;
        }
        return result;
    }
    
    /**
     * Clears the {@link #passwordResetSecret}.
     */
    @Override
    public void passwordWasReset() {
        passwordResetSecret = null;
    }

    @Override
    public boolean isEmailValidated() {
        return emailValidated;
    }

    /**
     * Uses the {@link #userGroupProvider} passed to this object's constructor to dynamically
     * query the groups that this user is member of.
     */
    @Override
    public Iterable<UserGroup> getUserGroups() {
        return userGroupProvider.getUserGroupsOfUser(this);
    }

    @Override
    public String toString() {
        return "User [name=" + getName() + ", email=" + email + (isEmailValidated() ? " (validated)" : "")
                + ", fullName=" + fullName + ", company=" + company + ", locale=" + locale + ", permissions="
                + getPermissions() + ", roles=" + getRoles() + ", defaultTenant="
                + (getDefaultTenant() == null ? "null" : getDefaultTenant().getName()) + ", accounts="
                + Arrays.toString(accounts.keySet().toArray(new AccountType[accounts.size()])) + "]";
    }

    @Override
    public String getValidationSecret() {
        return validationSecret;
    }

    @Override
    public QualifiedObjectIdentifier getIdentifier() {
        return getType().getQualifiedObjectIdentifier(getName());
    }

    @Override
    public HasPermissions getType() {
        return SecuredSecurityTypes.USER;
    }
}
