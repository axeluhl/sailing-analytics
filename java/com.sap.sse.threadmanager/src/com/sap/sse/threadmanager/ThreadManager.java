package com.sap.sse.threadmanager;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

@Path("/threads")
public class ThreadManager {
    @Context ServletContext servletContext; 

    @Path("")
    @GET
    @Produces("application/json;charset=UTF-8")
    public Response getThreads() {
        JSONArray threadsJson = new JSONArray();
        Thread[] threads = new Thread[10000];
        Thread.enumerate(threads);
        for (Thread t : threads) {
            if (t != null) {
                JSONObject threadJson = new JSONObject();
                threadJson.put("name", t.getName());
                threadJson.put("id", t.getId());
                threadJson.put("daemon", t.isDaemon());
                threadJson.put("alive", t.isAlive());
                threadJson.put("threadgroup", t.getThreadGroup().getName());
                threadsJson.add(threadJson);
            }
        }
        String json = threadsJson.toJSONString();
        return Response.ok(json, MediaType.APPLICATION_JSON).build();
    }

    @SuppressWarnings("deprecation") // using Thread.suspend()
    @Path("{name}/suspend")
    @GET
    @Produces("application/json;charset=UTF-8")
    public Response suspend(@PathParam("name") String name) {
        final Response response;
        JSONObject result = new JSONObject();
        Thread[] threads = new Thread[10000];
        boolean found = false;
        Thread.enumerate(threads);
        for (Thread t : threads) {
            if (t != null && t.getName().equals(name)) {
                t.stop();
                result.put("status", "OK");
                found = true;
            }
        }
        String json = result.toJSONString();
        if (!found) {
            result.put("status", "Not found");
            response = Response.status(Status.NOT_FOUND).entity(json).build();
        } else {
            response = Response.ok(json, MediaType.APPLICATION_JSON).build();
        }
        return response;
    }

    @SuppressWarnings("deprecation") // using Thread.stop()
    @Path("{name}/stop")
    @GET
    @Produces("application/json;charset=UTF-8")
    public Response stop(@PathParam("name") String name) {
        final Response response;
        JSONObject result = new JSONObject();
        Thread[] threads = new Thread[10000];
        boolean found = false;
        Thread.enumerate(threads);
        for (Thread t : threads) {
            if (t != null && t.getName().equals(name)) {
                t.stop();
                result.put("status", "OK");
                found = true;
            }
        }
        String json = result.toJSONString();
        if (!found) {
            result.put("status", "Not found");
            response = Response.status(Status.NOT_FOUND).entity(json).build();
        } else {
            response = Response.ok(json, MediaType.APPLICATION_JSON).build();
        }
        return response;
    }

    @Path("{name}/interrupt")
    @GET
    @Produces("application/json;charset=UTF-8")
    public Response interrupt(@PathParam("name") String name) {
        final Response response;
        JSONObject result = new JSONObject();
        Thread[] threads = new Thread[10000];
        boolean found = false;
        Thread.enumerate(threads);
        for (Thread t : threads) {
            if (t != null && t.getName().equals(name)) {
                t.interrupt();
                result.put("status", "OK");
                found = true;
            }
        }
        String json = result.toJSONString();
        if (!found) {
            result.put("status", "Not found");
            response = Response.status(Status.NOT_FOUND).entity(json).build();
        } else {
            response = Response.ok(json, MediaType.APPLICATION_JSON).build();
        }
        return response;
    }

    @SuppressWarnings("deprecation") // using Thread.resume()
    @Path("{name}/resume")
    @GET
    @Produces("application/json;charset=UTF-8")
    public Response resume(@PathParam("name") String name) {
        final Response response;
        JSONObject result = new JSONObject();
        Thread[] threads = new Thread[10000];
        boolean found = false;
        Thread.enumerate(threads);
        for (Thread t : threads) {
            if (t != null && t.getName().equals(name)) {
                t.resume();
                result.put("status", "OK");
                found = true;
            }
        }
        String json = result.toJSONString();
        if (!found) {
            result.put("status", "Not found");
            response = Response.status(Status.NOT_FOUND).entity(json).build();
        } else {
            response = Response.ok(json, MediaType.APPLICATION_JSON).build();
        }
        return response;
    }
}
