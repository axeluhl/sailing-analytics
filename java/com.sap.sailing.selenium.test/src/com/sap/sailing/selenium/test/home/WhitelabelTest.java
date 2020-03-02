package com.sap.sailing.selenium.test.home;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import org.hamcrest.Matchers;
import org.junit.Test;

import com.sap.sailing.selenium.pages.home.HomePage;
import com.sap.sailing.selenium.test.AbstractSeleniumTest;

public class WhitelabelTest extends AbstractSeleniumTest {

    @Test
    public void test() {
        HomePage homePage = HomePage.goToPage(getWebDriver(), getContextRoot(), true);

        assertThat(homePage.getPageTitle(), not(containsString("SAP")));
        assertThat(homePage.getFavicon().isDisplayed(), Matchers.equalTo(false));
        assertThat(homePage.getSolutionsPageLink().isDisplayed(), Matchers.equalTo(false));

    }
}
