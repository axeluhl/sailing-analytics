package com.sap.sailing.selenium.test.authentication;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.selenium.pages.adminconsole.AdminConsolePage;
import com.sap.sailing.selenium.pages.authentication.AuthenticationMenuPO;
import com.sap.sailing.selenium.test.AbstractSeleniumTest;

public class TestAuthenticationSignIn extends AbstractSeleniumTest {
    
    @Override
    @Before
    public void setUp() {
        clearState(getContextRoot());
        getWebDriver().manage().deleteCookieNamed("JSESSIONID");
    }
    
    @Test
    public void testSignInWithExistingUserAdmin() {
        AdminConsolePage adminConsolePage = AdminConsolePage.goToPage(getWebDriver(), getContextRoot());
        AuthenticationMenuPO authenticationMenu = adminConsolePage.getAuthenticationMenu();
        assertFalse(authenticationMenu.isOpen());
        assertFalse(authenticationMenu.isLoggedIn());
        authenticationMenu.doLogin("admin", "admin");
        assertTrue(authenticationMenu.isOpen());
        assertTrue(authenticationMenu.isLoggedIn());
    }

}
