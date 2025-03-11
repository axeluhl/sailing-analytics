package com.sap.sse.test;

import static org.mockito.Mockito.mock;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;

import com.sap.sse.rest.CORSFilter;
import com.sap.sse.rest.CORSFilterConfiguration;

public class CORSFilterConfigurationTest {
    @Test
    public void testWildcardFilter() throws IOException, ServletException {
        final com.sap.sse.impl.Activator activator = new com.sap.sse.impl.Activator();
        final CORSFilterConfiguration corsConfig = activator.getCORSFilterConfiguration();
        corsConfig.setWildcard();
        final HttpServletRequest request = mock(HttpServletRequest.class);
        final HttpServletResponse response = mock(HttpServletResponse.class);
        final FilterChain chain = mock(FilterChain.class);
        new CORSFilter().doFilter(request, response, chain);
        // TODO expect response to contain a * value for the allowed origins header
    }
}
