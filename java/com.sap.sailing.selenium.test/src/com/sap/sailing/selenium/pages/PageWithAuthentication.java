package com.sap.sailing.selenium.pages;

import com.sap.sailing.selenium.pages.authentication.AuthenticationMenuPO;

/**
 * Marks a {@link HostPage} to have a menu for user authentication.
 */
public interface PageWithAuthentication {

    /**
     * Access the pages {@link AuthenticationMenuPO authentication menu}.
     * 
     * @return the pages {@link AuthenticationMenuPO authentication menu} handle
     */
    AuthenticationMenuPO getAuthenticationMenu();
    
}
