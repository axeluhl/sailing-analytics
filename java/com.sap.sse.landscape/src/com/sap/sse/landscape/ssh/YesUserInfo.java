package com.sap.sse.landscape.ssh;

import com.jcraft.jsch.UserInfo;

public class YesUserInfo implements UserInfo {
    @Override public void showMessage(String message) {}
    @Override public boolean promptYesNo(String message) { return true; } // accept host key
    @Override public boolean promptPassword(String message) { return false; } // we're using public key
    @Override public boolean promptPassphrase(String message) { return false; } // passphrase is provided programmatically
    @Override public String getPassword() { return null; }
    @Override public String getPassphrase() { return null; }
}