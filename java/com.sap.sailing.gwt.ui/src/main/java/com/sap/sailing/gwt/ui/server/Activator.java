package com.sap.sailing.gwt.ui.server;

import java.util.logging.Logger;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.sap.sse.security.SecurityService;

public class Activator implements BundleActivator {
    private static final Logger logger = Logger.getLogger(Activator.class.getName());
    private static BundleContext context;
    private SailingServiceImpl sailingServiceToStopWhenStopping;
    private static Activator INSTANCE;

    public Activator() {
        INSTANCE = this;
    }
    
    @Override
    public void start(BundleContext context) throws Exception {
        Activator.context = context;
    }

    private void ensureAdminConsoleRoles(SailingServiceImpl sailingServiceImpl) {
        final SecurityService securityService = sailingServiceImpl.getSecurityService();
        final AdminConsoleRole adminConsoleRolePrototype = AdminConsoleRole.getInstance();
        if (securityService.getRoleDefinition(adminConsoleRolePrototype.getId()) == null) {
            logger.info("No adminconsole role found. Creating default role \""+adminConsoleRolePrototype.getName()+"\" with permission \""+
                    adminConsoleRolePrototype.getPermissions()+"\"");
            securityService.createRoleDefinition(adminConsoleRolePrototype.getId(), adminConsoleRolePrototype.getName());
            securityService.updateRoleDefinition(adminConsoleRolePrototype);
        }
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        if (sailingServiceToStopWhenStopping != null) {
            sailingServiceToStopWhenStopping.stop();
        }
    }
    
    public static Activator getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Activator();
        }
        return INSTANCE;
    }
    
    public static BundleContext getDefault() {
        return context;
    }

    public void setSailingService(SailingServiceImpl sailingServiceImpl) {
        sailingServiceToStopWhenStopping = sailingServiceImpl;
        ensureAdminConsoleRoles(sailingServiceImpl);
    }

}
