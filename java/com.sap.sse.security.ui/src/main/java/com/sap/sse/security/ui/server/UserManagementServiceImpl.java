package com.sap.sse.security.ui.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.subject.Subject;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.sap.sse.security.SecurityService;
import com.sap.sse.security.ui.Activator;
import com.sap.sse.security.ui.shared.UserDTO;
import com.sap.sse.security.ui.shared.UserManagementService;
import com.sap.sse.security.userstore.shared.User;

public class UserManagementServiceImpl extends RemoteServiceServlet implements UserManagementService {

    private static final long serialVersionUID = 4458564336368629101L;

    private final BundleContext context;
    private SecurityService securityService;
    
    public UserManagementServiceImpl() {
        context = Activator.getContext();
        ServiceReference<?> serviceReference = context.
                getServiceReference(SecurityService.class.getName());
        securityService = (SecurityService) context.
                getService(serviceReference);
        SecurityUtils.setSecurityManager(securityService.getSecurityManager());
    }
    
    @Override
    public String sayHello() {
        return "Hello";
    }

    @Override
    public Collection<UserDTO> getUserList() {
        List<UserDTO> users = new ArrayList<>();
        for (User u : securityService.getUserList()){
            users.add(new UserDTO(u.getName(), u.getAccountType().getName()));
        }
        return users;
    }

    @Override
    public UserDTO getCurrentUser() {
        Subject subject = SecurityUtils.getSubject();
        if (subject == null){
            return null;
        }
        Object principal = subject.getPrincipal();
        UserDTO userDTO;
        if (principal != null){
            User u = securityService.getUserByName(principal.toString());
            userDTO = new UserDTO(u.getName(), u.getAccountType().getName());
        }
        else {
            userDTO = null;
        }
        return userDTO;
    }

    @Override
    public String login(String username, String password) {
        try {
            return securityService.login(username, password);
        } catch (AuthenticationException e) {
            Logger.getLogger(UserManagementServiceImpl.class.getName()).info("Could not login: " + e);
        }
        return null;
    }

    @Override
    public void logout() {
        securityService.logout();
    }

    
}
