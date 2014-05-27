package com.sap.sse.security.ui.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.subject.Subject;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.sap.sse.security.SecurityService;
import com.sap.sse.security.ui.Activator;
import com.sap.sse.security.ui.shared.SuccessInfo;
import com.sap.sse.security.ui.shared.UserDTO;
import com.sap.sse.security.ui.shared.UserManagementService;
import com.sap.sse.security.userstore.shared.SimpleUser;
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
            UserDTO userDTO = new UserDTO(u.getName(), u.getAccountType().getName());
            userDTO.addRoles(u.getRoles());
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
            userDTO = new UserDTO(u.getName(), u.getAccountType().getName());
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
    public UserDTO createSimpleUser(String name, String password) {
        SimpleUser su = null;
        try {
            su = securityService.createSimpleUser(name, password);
        } catch (UserManagementException e) {
            e.printStackTrace();
        }
        if (su == null) {
            return null;
        }
        return new UserDTO(su.getName(), su.getAccountType().getName());
    }

    @Override
    public Collection<UserDTO> getFilteredSortedUserList(String filter) {
        List<UserDTO> users = new ArrayList<>();
        for (User u : securityService.getUserList()) {
            if (filter != null && !"".equals(filter)) {
                if (u.getName().contains(filter)) {
                    UserDTO userDTO = new UserDTO(u.getName(), u.getAccountType().getName());
                    userDTO.addRoles(u.getRoles());
                    users.add(userDTO);
                }
            } else {
                UserDTO userDTO = new UserDTO(u.getName(), u.getAccountType().getName());
                userDTO.addRoles(u.getRoles());
                users.add(userDTO);
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

}
