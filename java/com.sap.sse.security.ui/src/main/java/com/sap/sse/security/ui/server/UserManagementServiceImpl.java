package com.sap.sse.security.ui.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.logging.Logger;

import javax.servlet.http.HttpSession;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ByteSource;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.sap.sse.security.Credential;
import com.sap.sse.security.SecurityService;
import com.sap.sse.security.SessionUtils;
import com.sap.sse.security.ui.Activator;
import com.sap.sse.security.ui.oauth.client.CredentialDTO;
import com.sap.sse.security.ui.oauth.client.SocialUserDTO;
import com.sap.sse.security.ui.oauth.shared.OAuthException;
import com.sap.sse.security.ui.shared.AccountDTO;
import com.sap.sse.security.ui.shared.SuccessInfo;
import com.sap.sse.security.ui.shared.UserDTO;
import com.sap.sse.security.ui.shared.UserManagementService;
import com.sap.sse.security.ui.shared.UsernamePasswordAccountDTO;
import com.sap.sse.security.userstore.shared.Account;
import com.sap.sse.security.userstore.shared.Account.AccountType;
import com.sap.sse.security.userstore.shared.FieldNames.Social;
import com.sap.sse.security.userstore.shared.SocialUserAccount;
import com.sap.sse.security.userstore.shared.User;
import com.sap.sse.security.userstore.shared.UserManagementException;
import com.sap.sse.security.userstore.shared.UserStore;
import com.sap.sse.security.userstore.shared.UsernamePasswordAccount;

public class UserManagementServiceImpl extends RemoteServiceServlet implements UserManagementService {

    private static final long serialVersionUID = 4458564336368629101L;
    
    private static final Logger logger = Logger.getLogger(UserManagementServiceImpl.class.getName());

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
        logger.info("Request: " + getThreadLocalRequest().getRequestURL());
        User user = securityService.getCurrentUser();
        if (user == null){
            return null;
        }
        return createUserDTOFromUser(user);
    }

    @Override
    public SuccessInfo login(String username, String password) {
        try {
            String success = securityService.login(username, password);
            return new SuccessInfo(true, success);
        } catch (UserManagementException e) {
            return new SuccessInfo(false, "Failed to login.");
        }
    }

    @Override
    public SuccessInfo logout() {
        logger.info("Logging out user: " + SessionUtils.loadUsername());
        securityService.logout();
        getHttpSession().invalidate();
        logger.info("Invalidated HTTP session");
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
        List<AccountDTO> accountDTOs = new ArrayList<>();
        for (Account a : accounts.values()){
            switch (a.getAccountType()) {
            case SOCIAL_USER:
                accountDTOs.add(createSocialUserDTO((SocialUserAccount) a));
                break;

            default:
                UsernamePasswordAccount upa = (UsernamePasswordAccount) a;
                accountDTOs.add(new UsernamePasswordAccountDTO(upa.getName(), upa.getSaltedPassword(), ((ByteSource) upa.getSalt()).getBytes()));
                break;
            }
        }
        userDTO = new UserDTO(user.getName(), user.getEmail(), accountDTOs);
        userDTO.addRoles(user.getRoles());
        return userDTO;
    }

    @Override
    public Map<String, String> getSettings() {
        Map<String, String> settings = new TreeMap<String, String>();
        for (Entry<String, Object> e : securityService.getAllSettings().entrySet()){
            settings.put(e.getKey(), e.getValue().toString());
        }
        return settings;
    }

    @Override
    public void setSetting(String key, String clazz, String setting) {
        if (clazz.equals(Boolean.class.getName())){
            securityService.setSetting(key, Boolean.parseBoolean(setting));
        }
        else if (clazz.equals(Integer.class.getName())){
            securityService.setSetting(key, Integer.parseInt(setting));
        }
        else {
            securityService.setSetting(key, setting);
        }
        securityService.refreshSecurityConfig(getServletContext());
    }

    @Override
    public Map<String, String> getSettingTypes() {
        Map<String, String> settingTypes = new TreeMap<String, String>();
        for (Entry<String, Class<?>> e : securityService.getAllSettingTypes().entrySet()){
            settingTypes.put(e.getKey(), e.getValue().getName());
        }
        return settingTypes;
    }
    
    
    
    
    
    
    
    //--------------------------------------------------------- OAuth Implementations -------------------------------------------------------------------------

    @Override
    public String getAuthorizationUrl(CredentialDTO credential) throws OAuthException {
        logger.info("callback url: " + credential.getRedirectUrl());
        String authorizationUrl = null;
        
        try {
            authorizationUrl = securityService.getAuthenticationUrl(createCredentialFromDTO(credential));
        } catch (UserManagementException e) {
            throw new OAuthException(e.getMessage());
        }

        return authorizationUrl;
    }

    @Override
    public UserDTO verifySocialUser(CredentialDTO credentialDTO) {
        
        User user = null;
        try {
            user = securityService.verifySocialUser(createCredentialFromDTO(credentialDTO));
        } catch (UserManagementException e) {
            e.printStackTrace();
        }
        return createUserDTOFromUser(user);
    }

    private HttpSession getHttpSession() {
        return getThreadLocalRequest().getSession();
    }

    private Credential createCredentialFromDTO(CredentialDTO credentialDTO){
        Credential credential = new Credential();
        credential.setAuthProvider(credentialDTO.getAuthProvider());
        credential.setAuthProviderName(credentialDTO.getAuthProviderName());
        credential.setEmail(credentialDTO.getEmail());
        credential.setLoginName(credentialDTO.getLoginName());
        credential.setPassword(credentialDTO.getPassword());
        credential.setRedirectUrl(credentialDTO.getRedirectUrl());
        credential.setState(credentialDTO.getState());
        credential.setVerifier(credentialDTO.getVerifier());
        credential.setOauthToken(credentialDTO.getOauthToken());
        return credential;
    }
    
    private SocialUserDTO createSocialUserDTO(SocialUserAccount socialUser){
        SocialUserDTO socialUserDTO = new SocialUserDTO(socialUser.getProperty(Social.PROVIDER.name()));
        socialUserDTO.setSessionId(socialUser.getSessionId());
        
        for (Social s : Social.values()){
            socialUserDTO.setProperty(s.name(), socialUser.getProperty(s.name()));
        }
        return socialUserDTO;
    }

    @Override
    public void addSetting(String key, String clazz, String setting) {
        try {
            securityService.addSetting(key, Class.forName(clazz));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (UserManagementException e) {
            e.printStackTrace();
        }
        if (clazz.equals(Boolean.class.getName())){
            securityService.setSetting(key, Boolean.parseBoolean(setting));
        }
        else if (clazz.equals(Integer.class.getName())){
            securityService.setSetting(key, Integer.parseInt(setting));
        }
        else {
            securityService.setSetting(key, setting);
        }
        securityService.refreshSecurityConfig(getServletContext());
    }
}
