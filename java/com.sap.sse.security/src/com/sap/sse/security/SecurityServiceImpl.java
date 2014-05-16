package com.sap.sse.security;

import java.util.Collection;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.Factory;
import org.apache.shiro.web.util.SavedRequest;
import org.apache.shiro.web.util.WebUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.sap.sse.security.userstore.shared.User;
import com.sap.sse.security.userstore.shared.UserStore;

public class SecurityServiceImpl  extends RemoteServiceServlet implements SecurityService {
    
    private static final long serialVersionUID = -3490163216601311858L;
    
    private SecurityManager securityManager;
    private UserStore store;
    
    public SecurityServiceImpl() {
        Factory<SecurityManager> factory = new IniSecurityManagerFactory("classpath:shiro.ini");
        System.out.println("Loaded shiro.ini file from: classpath:shiro.ini");
        SecurityManager securityManager = factory.getInstance();
        Logger.getLogger(SecurityServiceImpl.class.getName()).info("Created: " + securityManager);
        SecurityUtils.setSecurityManager(securityManager);
        this.securityManager = securityManager;
        
        BundleContext context = Activator.getContext();
        ServiceReference<?> serviceReference = context.
                getServiceReference(UserStore.class.getName());
        store = (UserStore) context.
                getService(serviceReference);
    }

    @Override
    public SecurityManager getSecurityManager() {
        return this.securityManager;
    }

    @Override
    public Collection<User> getUserList() {
        return store.getUserCollection();
    }

    @Override
    public String login(String username, String password) {
        String redirectUrl;
        UsernamePasswordToken token = new UsernamePasswordToken(username, password);
        System.out.println("Trying to login: " + username);
        Subject subject = SecurityUtils.getSubject();
        subject.login(token);
        HttpServletRequest httpRequest = WebUtils.getHttpRequest(subject);
        SavedRequest savedRequest = WebUtils.getSavedRequest(httpRequest);
        if (savedRequest != null){
            System.out.println("Found saved request");
            redirectUrl = savedRequest.getRequestUrl();
        }
        else {
            redirectUrl = "";
        }
        System.out.println("Redirecturl: " + redirectUrl);
        return redirectUrl;
    }

    @Override
    public void logout() {
        Subject subject = SecurityUtils.getSubject();
        subject.logout();
    }

    @Override
    public User getUserByName(String name) {
        return store.getUserByName(name);
    }

}
