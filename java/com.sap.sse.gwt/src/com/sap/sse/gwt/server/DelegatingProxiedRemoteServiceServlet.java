package com.sap.sse.gwt.server;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import com.google.gwt.user.client.rpc.IncompatibleRemoteServiceException;
import com.google.gwt.user.client.rpc.RpcTokenException;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.server.rpc.RPC;
import com.google.gwt.user.server.rpc.RPCRequest;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.gwt.user.server.rpc.SerializationPolicy;
import com.sap.sse.common.TimePoint;

public abstract class DelegatingProxiedRemoteServiceServlet extends ProxiedRemoteServiceServlet {
    private static final long serialVersionUID = -5543378343472849437L;

    /**
     * Overwritten version of {@link RemoteServiceServlet#processCall(RPCRequest)} that calls
     * {@link #invokeAndEncodeResponse(Object, Method, Object[], SerializationPolicy, int)} instead of
     * {@link RPC#invokeAndEncodeResponse(Object, Method, Object[], SerializationPolicy, int)}.
     * {@link #invokeAndEncodeResponse(Object, Method, Object[], SerializationPolicy, int)} in turn calls
     * {@link #encodeResponseForSuccess(Method, SerializationPolicy, int, Object)} which subclasses can override
     * in order to customize result serialization, e.g., by picking a cached serialized version.
     */
    @Override
    public String processCall(RPCRequest rpcRequest) throws SerializationException {
        final TimePoint startOfProcessCall = beforeProcessCall();
        try {
            final Object delegate = this;
            try {
                onAfterRequestDeserialized(rpcRequest);
                return invokeAndEncodeResponse(delegate, rpcRequest.getMethod(), rpcRequest.getParameters(),
                        rpcRequest.getSerializationPolicy(), rpcRequest.getFlags());
            } catch (IncompatibleRemoteServiceException ex) {
                log("An IncompatibleRemoteServiceException was thrown while processing this call.", ex);
                return RPC.encodeResponseForFailedRequest(rpcRequest, ex);
            } catch (RpcTokenException tokenException) {
                log("An RpcTokenException was thrown while processing this call.", tokenException);
                return RPC.encodeResponseForFailedRequest(rpcRequest, tokenException);
            }
        } finally {
            afterProcessCall(rpcRequest, startOfProcessCall);
        }
    }

    /**
     * Similar to {@link RPC#invokeAndEncodeResponse(Object, Method, Object[], SerializationPolicy, int)}, but
     * calls {@link #encodeResponseForSuccess(Method, SerializationPolicy, int, Object)}, giving subclasses
     * a way to override result serialization.
     */
    private String invokeAndEncodeResponse(Object target, Method serviceMethod, Object[] args,
            SerializationPolicy serializationPolicy, int flags) throws SerializationException {
        if (serviceMethod == null) {
            throw new NullPointerException("serviceMethod");
        }
        if (serializationPolicy == null) {
            throw new NullPointerException("serializationPolicy");
        }
        String responsePayload;
        try {
            Object result = serviceMethod.invoke(target, args);
            responsePayload = encodeResponseForSuccess(serviceMethod, serializationPolicy, flags, result);
        } catch (IllegalAccessException e) {
            SecurityException securityException = new SecurityException(formatIllegalAccessErrorMessage(target, serviceMethod));
            securityException.initCause(e);
            throw securityException;
        } catch (IllegalArgumentException e) {
            SecurityException securityException = new SecurityException(formatIllegalArgumentErrorMessage(target, serviceMethod, args));
            securityException.initCause(e);
            throw securityException;
        } catch (InvocationTargetException e) {
            // Try to encode the caught exception
            Throwable cause = e.getCause();
            responsePayload = RPC.encodeResponseForFailure(serviceMethod, cause, serializationPolicy, flags);
        }
        return responsePayload;
    }

    /**
     * To be overwritten by subclasses to customize the serialization of the result object.
     */
    protected String encodeResponseForSuccess(Method serviceMethod, SerializationPolicy serializationPolicy, int flags,
            Object result) throws SerializationException {
        final String responsePayload;
        responsePayload = RPC.encodeResponseForSuccess(serviceMethod, result, serializationPolicy, flags);
        return responsePayload;
    }

    /**
     * Taken from {@link RPC}.
     */
    private String formatIllegalAccessErrorMessage(Object target, Method serviceMethod) {
        StringBuffer sb = new StringBuffer();
        sb.append("Blocked attempt to access inaccessible method '");
        sb.append(getSourceRepresentation(serviceMethod));
        sb.append("'");
        if (target != null) {
            sb.append(" on target '");
            sb.append(printTypeName(target.getClass()));
            sb.append("'");
        }
        sb.append("; this is either misconfiguration or a hack attempt");
        return sb.toString();
    }

    /**
     * Taken from {@link RPC}.
     */
    private String formatIllegalArgumentErrorMessage(Object target, Method serviceMethod, Object[] args) {
        StringBuffer sb = new StringBuffer();
        sb.append("Blocked attempt to invoke method '");
        sb.append(getSourceRepresentation(serviceMethod));
        sb.append("'");
        if (target != null) {
            sb.append(" on target '");
            sb.append(printTypeName(target.getClass()));
            sb.append("'");
        }
        sb.append(" with invalid arguments");
        if (args != null && args.length > 0) {
            sb.append(Arrays.asList(args));
        }
        return sb.toString();
    }

    private String getSourceRepresentation(Method method) {
        return method.toString().replace('$', '.');
    }

    /**
     * Taken from {@link RPC}.
     */
    private String printTypeName(Class<?> type) {
        // Primitives
        if (type.equals(Integer.TYPE)) {
            return "int";
        } else if (type.equals(Long.TYPE)) {
            return "long";
        } else if (type.equals(Short.TYPE)) {
            return "short";
        } else if (type.equals(Byte.TYPE)) {
            return "byte";
        } else if (type.equals(Character.TYPE)) {
            return "char";
        } else if (type.equals(Boolean.TYPE)) {
            return "boolean";
        } else if (type.equals(Float.TYPE)) {
            return "float";
        } else if (type.equals(Double.TYPE)) {
            return "double";
        }
        // Arrays
        if (type.isArray()) {
            Class<?> componentType = type.getComponentType();
            return printTypeName(componentType) + "[]";
        }
        // Everything else
        return type.getName().replace('$', '.');
    }
}
