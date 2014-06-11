package com.sap.sse.security.userstore.shared;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.sap.sse.security.userstore.shared.Account.AccountType;

public class User {
    
    private String name;
    
    private String email;
    
    private Set<String> roles = new HashSet<>();
    private Map<AccountType, Account> accounts = new ConcurrentHashMap<>();

    public User(String name, String email, Account... accounts) {
        super();
        this.name = name;
        for (Account a : accounts){
            this.accounts.put(a.getAccountType(), a);
        }
    }
    
    public User(String name, String email, Collection<Account> accounts) {
        super();
        this.name = name;
        for (Account a : accounts){
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
    
    public void addRole(String role){
        roles.add(role);
    }
    
    public void removeRole(String role){
        roles.remove(role);
    }
    
    public Account getAccount(AccountType type){
        return accounts.get(type);
    }
    
    public void setAccount(AccountType type, Account account){
        accounts.put(type, account);
    }
    
    public void removeAccount(AccountType type){
        accounts.remove(type);
    }

    public Map<AccountType, Account> getAllAccounts(){
        return accounts;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "User [name=" + name + ", email=" + email + ", roles=" + Arrays.toString(roles.toArray(new String[roles.size()])) + ", accounts=" + Arrays.toString(accounts.keySet().toArray(new AccountType[accounts.size()])) + "]";
    }
    
    
}
