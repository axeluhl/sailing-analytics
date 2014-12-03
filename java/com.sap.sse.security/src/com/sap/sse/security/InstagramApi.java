package com.sap.sse.security;

import java.util.UUID;

import org.scribe.builder.api.DefaultApi20;
import org.scribe.extractors.AccessTokenExtractor;
import org.scribe.extractors.JsonTokenExtractor;
import org.scribe.model.OAuthConfig;
import org.scribe.model.Verb;
import org.scribe.utils.Preconditions;

/**
 * @author muquit@muquit.com
 */
public class InstagramApi extends DefaultApi20
{
    private static final String AUTHORIZATION_URL = "https://api.instagram.com/oauth/authorize?client_id=%s&redirect_uri=%s&state=%s&response_type=code";
    @Override
    public String getAccessTokenEndpoint()
    {
        return "https://api.instagram.com/oauth/access_token";
    }
    
    @Override
    public Verb getAccessTokenVerb()
    {
        return Verb.POST;
    }
    
    @Override
    public AccessTokenExtractor getAccessTokenExtractor()
    {
        return new JsonTokenExtractor();
    }

    @Override
    public String getAuthorizationUrl(OAuthConfig config)
    {
        Preconditions.checkValidUrl(config.getCallback(), "Must provide a valid url as callback for Instagram");
        String uuid = UUID.randomUUID().toString();
        return String.format(AUTHORIZATION_URL, config.getApiKey(), config.getCallback(),uuid);
    }
}
