package com.sap.sailing.selenium.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@TestTemplate
@ExtendWith(SeleniumTestInvocationProvider.class)
@ExtendWith(ScreenShotRule.class)
public @interface SeleniumTestTemplate {
}
