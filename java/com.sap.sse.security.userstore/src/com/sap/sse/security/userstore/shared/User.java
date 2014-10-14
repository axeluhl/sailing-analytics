package com.sap.sse.security.userstore.shared;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.sap.sse.security.userstore.shared.Account.AccountType;

public class User {

    private String name;

    private String email;

    private final Set<String> roles;
    private final Map<AccountType, Account> accounts;

    public User(String name, String email, Account... accounts) {
        this(name, email, Arrays.asList(accounts));
    }

    public User(String name, String email, Collection<Account> accounts) {
        super();
        this.roles = new HashSet<>();
        this.accounts = new ConcurrentHashMap<>();
        this.name = name;
        for (Account a : accounts) {
            this.accounts.put(a.getAccountType(), a);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<String> getRoles() {
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

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "User [name=" + name + ", email=" + email + ", roles="
                + Arrays.toString(roles.toArray(new String[roles.size()])) + ", accounts="
                + Arrays.toString(accounts.keySet().toArray(new AccountType[accounts.size()])) + "]";
    }

}
