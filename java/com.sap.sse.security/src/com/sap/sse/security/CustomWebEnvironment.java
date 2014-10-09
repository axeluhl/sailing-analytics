package com.sap.sse.security;

import org.apache.shiro.config.Ini;
import org.apache.shiro.web.env.IniWebEnvironment;

public class CustomWebEnvironment extends IniWebEnvironment {
    @Override
    public void init() {
        Ini ini = SecurityServiceImpl.getShiroConfiguration();
        setIni(ini);
        configure();
    }
}
