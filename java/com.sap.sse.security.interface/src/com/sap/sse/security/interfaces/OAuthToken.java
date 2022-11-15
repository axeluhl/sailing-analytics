package com.sap.sse.security.interfaces;

import org.apache.shiro.authc.AuthenticationToken;

public class OAuthToken implements AuthenticationToken {
	
    private static final long serialVersionUID = -1156345860394792503L;
    private Credential credential;
	private String principal;
	
	public OAuthToken(Credential credential, String principal) {
		this.credential = credential;
		this.principal = principal;
	}

	@Override
	public Object getCredentials() {
		// TODO Auto-generated method stub
		return credential;
	}

	@Override
	public Object getPrincipal() {
		// TODO Auto-generated method stub
		return principal;
	}

}
