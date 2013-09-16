package com.sap.sailing.gwt.ui.server;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.sap.sailing.gwt.ui.client.UserManagementService;
import com.sap.sailing.gwt.ui.shared.UserDTO;
import com.sap.sailing.gwt.ui.usermanagement.UserRoles;

/*
 * This is a extremly simple implementation of a user management service. 
 */
public class UserManagementServiceImpl  extends ProxiedRemoteServiceServlet implements UserManagementService {
    private static final long serialVersionUID = -6360444730702283475L;
//    private static final Logger logger = Logger.getLogger(UserManagementServiceImpl.class.getName());

    @Override
    public boolean isUserInRole(String userRole) {
        return getThreadLocalRequest().isUserInRole(userRole);
    }

    @Override
    public void logoutUser() {
        HttpSession session = getThreadLocalRequest().getSession();
        if(session != null) {
            session.invalidate();
            session = null;
        }
    }

    @Override
    public UserDTO getUser() {
        HttpServletRequest threadLocalRequest = getThreadLocalRequest();
        Principal userPrincipal = threadLocalRequest.getUserPrincipal();
        List<String> roles = new ArrayList<String>();
        
        if (userPrincipal != null) {
           UserDTO userDTO = new UserDTO(userPrincipal.getName());
           
           for(UserRoles role: UserRoles.values()) {
               if (threadLocalRequest.isUserInRole(role.name())) {
                   roles.add(role.name());
               }
           }
           userDTO.roles = roles;
           
           return userDTO;
        }
        
        return null;
    }
    
    @Override
    /**
     * Override of function to prevent exception "Blocked request without GWT permutation header (XSRF attack?)" when testing the GWT sites
     */
    protected void checkPermutationStrongName() throws SecurityException {
    }
}
