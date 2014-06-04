package com.sap.sse.security.ui.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.subject.Subject;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.sap.sse.security.SecurityService;
import com.sap.sse.security.ui.Activator;
import com.sap.sse.security.ui.shared.AccountDTO;
import com.sap.sse.security.ui.shared.SuccessInfo;
import com.sap.sse.security.ui.shared.UserDTO;
import com.sap.sse.security.ui.shared.UserManagementService;
import com.sap.sse.security.userstore.shared.Account;
import com.sap.sse.security.userstore.shared.Account.AccountType;
import com.sap.sse.security.userstore.shared.User;
import com.sap.sse.security.userstore.shared.UserManagementException;
import com.sap.sse.security.userstore.shared.UserStore;

public class UserManagementServiceImpl extends RemoteServiceServlet implements UserManagementService {

    private static final long serialVersionUID = 4458564336368629101L;

    private final BundleContext context;
    private SecurityService securityService;

    public UserManagementServiceImpl() {
        context = Activator.getContext();
        ServiceReference<?> serviceReference = context.getServiceReference(SecurityService.class.getName());
        securityService = (SecurityService) context.getService(serviceReference);
        SecurityUtils.setSecurityManager(securityService.getSecurityManager());
    }

    @Override
    public String sayHello() {
        return "Hello";
    }

    @Override
    public Collection<UserDTO> getUserList() {
        List<UserDTO> users = new ArrayList<>();
        for (User u : securityService.getUserList()) {
            UserDTO userDTO = createUserDTOFromUser(u);
            users.add(userDTO);
        }
        return users;
    }

    @Override
    public UserDTO getCurrentUser() {
        Subject subject = SecurityUtils.getSubject();
        if (subject == null) {
            return null;
        }
        Object principal = subject.getPrincipal();
        UserDTO userDTO;
        if (principal != null) {
            User u = securityService.getUserByName(principal.toString());
            userDTO = createUserDTOFromUser(u);
        } else {
            userDTO = null;
        }
        return userDTO;
    }

    @Override
    public SuccessInfo login(String username, String password) {
        try {
            return new SuccessInfo(true, securityService.login(username, password));
        } catch (AuthenticationException e) {
            return new SuccessInfo(false, "Failed to login.");
        }
    }

    @Override
    public SuccessInfo logout() {
        securityService.logout();
        return new SuccessInfo(true, "Logged out.");
    }

    @Override
    public UserDTO createSimpleUser(String name, String email, String password) {
        User u = null;
        try {
            u = securityService.createSimpleUser(name, email, password);
        } catch (UserManagementException e) {
            e.printStackTrace();
        }
        if (u == null) {
            return null;
        }
        return createUserDTOFromUser(u);
    }

    @Override
    public Collection<UserDTO> getFilteredSortedUserList(String filter) {
        List<UserDTO> users = new ArrayList<>();
        for (User u : securityService.getUserList()) {
            if (filter != null && !"".equals(filter)) {
                if (u.getName().contains(filter)) {
                    users.add(createUserDTOFromUser(u));
                }
            } else {
                users.add(createUserDTOFromUser(u));
            }
        }

        Collections.sort(users, new Comparator<UserDTO>() {

            @Override
            public int compare(UserDTO u1, UserDTO u2) {
                return u1.getName().compareTo(u2.getName());
            }
        });
        return users;
    }

    @Override
    public SuccessInfo addRoleForUser(String username, String role) {
        Subject currentUser = SecurityUtils.getSubject();

        if (currentUser.hasRole(UserStore.DefaultRoles.ADMIN.getName())) {
            User u = securityService.getUserByName(username);
            if (u == null) {
                return new SuccessInfo(false, "User does not exist.");
            }
            try {
                securityService.addRoleForUser(username, role);
                return new SuccessInfo(true, "Added role: " + role + ".");
            } catch (UserManagementException e) {
                return new SuccessInfo(false, e.getMessage());
            }
        } else {
            return new SuccessInfo(false, "You don't have the required permissions to add a role.");
        }
    }

    @Override
    public SuccessInfo deleteUser(String username) {
        try {
            securityService.deleteUser(username);
            return new SuccessInfo(true, "Deleted user: " + username + ".");
        } catch (UserManagementException e) {
            return new SuccessInfo(false, "Could not delete user.");
        }
    }

    private UserDTO createUserDTOFromUser(User user){
        UserDTO userDTO;
        Map<AccountType, Account> accounts = user.getAllAccounts();
        AccountDTO[] accountDTOs = new AccountDTO[accounts.size()];
        int i = 0;
        for (Account a : accounts.values()){
            // TODO [D056866] Create specific AccountDTOs, not from abstract class
            accountDTOs[i] = new AccountDTO(a.getAccountType().getName()) {
            };
            i++;
        }
        userDTO = new UserDTO(user.getName(), accountDTOs);
        userDTO.addRoles(user.getRoles());
        return userDTO;
    }

    @Override
    public Map<String, String> getSettings() {
        Map<String, String> settings = new HashMap<String, String>();
        for (Entry<String, Object> e : securityService.getAllSettings().entrySet()){
            settings.put(e.getKey(), e.getValue().toString());
        }
        return settings;
    }

    @Override
    public void setSetting(String key, String clazz, String setting) {
        if (clazz.equals(Boolean.class.getName())){
            securityService.setSettings(key, Boolean.parseBoolean(setting));
        }
        else if (clazz.equals(Integer.class.getName())){
            securityService.setSettings(key, Integer.parseInt(setting));
        }
        else {
            securityService.setSettings(key, setting);
        }
    }

    @Override
    public Map<String, String> getSettingTypes() {
        Map<String, String> settingTypes = new HashMap<String, String>();
        for (Entry<String, Class<?>> e : securityService.getAllSettingTypes().entrySet()){
            settingTypes.put(e.getKey(), e.getValue().getName());
        }
        return settingTypes;
    }
}
