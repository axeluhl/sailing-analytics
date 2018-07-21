package com.sap.sse.gwt.client;

import java.util.Map;

import com.sap.sse.gwt.settings.UrlBuilderUtil;

public class AbstractEntryPointLinkFactory {
    protected static String createEntryPointLink(String path, Map<String, String> parameters) {
        return UrlBuilderUtil.createUrlBuilderWithPathAndParameters(path, parameters).buildString();
    }
    protected static String createEntryPointLink(String path, String hash, Map<String, String> parameters) {
        return UrlBuilderUtil.createUrlBuilderWithPathAndHashAndParameters(path, hash, parameters).buildString();
    }
}
