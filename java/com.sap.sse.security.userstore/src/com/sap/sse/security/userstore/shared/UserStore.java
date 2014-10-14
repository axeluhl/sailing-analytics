package com.sap.sse.security.userstore.shared;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface UserStore {

    String getName();

    Collection<User> getUserCollection();

    User getUserByName(String name);

    User createUser(String name, String email, Account... accounts) throws UserManagementException;

    Set<String> getRolesFromUser(String username) throws UserManagementException;

    void addRoleForUser(String name, String role) throws UserManagementException;

    void removeRoleFromUser(String name, String role) throws UserManagementException;

    void deleteUser(String name) throws UserManagementException;

    public void addSetting(String key, Class<?> type);

    public void setSetting(String key, Object setting);

    public <T> T getSetting(String key, Class<T> clazz);

    public Map<String, Object> getAllSettings();

    public Map<String, Class<?>> getAllSettingTypes();
}
