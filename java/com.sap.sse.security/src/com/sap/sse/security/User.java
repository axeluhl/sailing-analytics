package com.sap.sse.security;

import java.io.Serializable;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.shiro.crypto.hash.Sha256Hash;

import com.sap.sse.common.NamedWithID;
import com.sap.sse.common.WithID;
import com.sap.sse.security.shared.Account;
import com.sap.sse.security.shared.Account.AccountType;

public class User implements NamedWithID {
    private static final long serialVersionUID = 1788215575606546042L;

    /**
     * The ID for this user; usually a nickname or short name. Implements the {@link WithID} key
     */
    private String name;

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

    private final Set<String> roles;
    private final Set<String> permissions;
    private final Map<AccountType, Account> accounts;

    public User(String name, String email, Account... accounts) {
        this(name, email, Arrays.asList(accounts));
    }

    public User(String name, String email, Collection<Account> accounts) {
        this(name, email, /* fullName */ null, /* company */ null, /* locale */ null, /* is email validated */ false,
             /* password reset secret */ null, /* validation secret */ null, accounts);
    }

    public User(String name, String email, String fullName, String company, Locale locale, Boolean emailValidated,
            String passwordResetSecret, String validationSecret, Collection<Account> accounts) {
        super();
        this.name = name;
        this.fullName = fullName;
        this.company = company;
        this.locale = locale;
        this.roles = new HashSet<>();
        this.permissions = new HashSet<>();
        this.email = email;
        this.passwordResetSecret = passwordResetSecret;
        this.validationSecret = validationSecret;
        this.emailValidated = emailValidated;
        this.accounts = new HashMap<>();
        for (Account a : accounts) {
            this.accounts.put(a.getAccountType(), a);
        }
    }

    /**
     * For the time being, the user {@link #getName() name} is used as ID
     */
    @Override
    public Serializable getId() {
        return getName();
    }

    public String getName() {
        return name;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }
    
    public Locale getLocale() {
        return locale;
    }
    
    public void setLocale(Locale locale) {
        this.locale = locale;
    }
    
    public Locale getLocaleOrDefault() {
        return locale == null ? Locale.ENGLISH : locale;
    }

    public Iterable<String> getRoles() {
        return roles;
    }

    public void addRole(String role) {
        roles.add(role);
    }

    public boolean hasRole(String role) {
        return roles.contains(role);
    }
    
    public void removeRole(String role) {
        roles.remove(role);
    }

    public Iterable<String> getPermissions() {
        return permissions;
    }

    public void addPermission(String permission) {
        permissions.add(permission);
    }
    
    public boolean hasPermission(String permission) {
        return permissions.contains(permission);
    }
    
    public void removePermission(String permission) {
        permissions.remove(permission);
    }
    
    public Account getAccount(AccountType type) {
        return accounts.get(type);
    }

    public void removeAccount(AccountType type) {
        accounts.remove(type);
    }

    public Map<AccountType, Account> getAllAccounts() {
        return accounts;
    }

    public String getEmail() {
        return email;
    }
    
    /**
     * Sets an e-mail address for this user. The address is considered not yet validated, therefore the
     * {@link #emailValidated} flag is reset, and a new {@link #validationSecret} is generated and returned which
     * can be used in a call to {@link #validate(String)} to validate the e-mail address.
     */
    public String setEmail(String email) {
        this.email = email;
        return startEmailValidation();
    }

    /**
     * The email address is set to not yet validated by resetting the
     * {@link #emailValidated} flag. A new {@link #validationSecret} is generated and returned which
     * can be used in a call to {@link #validate(String)} to validate the e-mail address.
     */
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
    public String startPasswordReset() {
        passwordResetSecret = createRandomSecret();
        return passwordResetSecret;
    }
    
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
    public void passwordWasReset() {
        passwordResetSecret = null;
    }

    public boolean isEmailValidated() {
        return emailValidated;
    }

    @Override
    public String toString() {
        return "User [name=" + name + ", email=" + email + ", fullName=" + fullName + ", company=" + company
                + ", locale=" + locale + (isEmailValidated() ? " (validated)" : ")") + ", roles="
                + Arrays.toString(roles.toArray(new String[roles.size()])) + ", accounts="
                + Arrays.toString(accounts.keySet().toArray(new AccountType[accounts.size()])) + "]";
    }

    public String getValidationSecret() {
        return validationSecret;
    }

}
