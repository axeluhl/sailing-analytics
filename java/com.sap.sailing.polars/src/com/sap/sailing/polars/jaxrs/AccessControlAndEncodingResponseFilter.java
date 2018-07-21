package com.sap.sailing.polars.jaxrs;

import javax.ws.rs.core.MultivaluedMap;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;

public class AccessControlAndEncodingResponseFilter implements ContainerResponseFilter {

    @Override
    public ContainerResponse filter(ContainerRequest request, ContainerResponse response) {
        MultivaluedMap<String, Object> httpHeaders = response.getHttpHeaders();
        httpHeaders.add("Access-Control-Allow-Origin", "*");
        httpHeaders.add("Access-Control-Allow-Headers", "Authorization, Origin, X-Requested-With, Content-Type");
        httpHeaders.add("Access-Control-Expose-Headers", "Location, Content-Disposition");
        httpHeaders.add("Access-Control-Allow-Methods", "POST, PUT, GET, DELETE, HEAD, OPTIONS");
        return response;
    }
}