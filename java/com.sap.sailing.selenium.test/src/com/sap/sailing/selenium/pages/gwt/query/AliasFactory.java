package com.sap.sailing.selenium.pages.gwt.query;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

public class AliasFactory {
    private interface ManagedObject {
        /**
         * <p></p>
         * 
         * @return
         *   
         */
        /*Expression<?>*/ Object __mappedPath();
    }
    
    private class MethodAccessTracker implements MethodInterceptor {
        private static final String TO_STRING = "toString";
        private static final String HASH_CODE = "hashCode";
        private static final String MAPPED_PATH = "__mappedPath";
        
        private final Expression<?> hostExpression;
        
        public MethodAccessTracker(Expression<?> host) {
            this.hostExpression = host;
        }
        
        @Override
        public Object intercept(Object object, Method method, Object[] args, MethodProxy proxy) throws Throwable {
            String methodName = method.getName();
            Class<?>[] parameterTypes = method.getParameterTypes();
            Class<?> returnType = method.getReturnType();
            //Type genericReturnType = method.getGenericReturnType();
            
            if (MAPPED_PATH.equals(methodName) && parameterTypes.length == 0)
                return this.hostExpression;

            if (TO_STRING.equals(methodName) && parameterTypes.length == 0)
                return this.hostExpression.toString();

            if (HASH_CODE.equals(methodName) && parameterTypes.length == 0)
                return Integer.valueOf(this.hostExpression.hashCode());
            
            PathMetadata<?> metadata = getOrCreatePathMetadata(method, args);
            Path<?> path = PathFactory.createPath(returnType, metadata);
            
            AliasFactory.this.setCurrentExpression(path);
            
            return null;
        }
        
        private PathMetadata<?> getOrCreatePathMetadata(Method method, Object[] args) {
            return new PathMetadata<>((Path<?>) this.hostExpression, method, args);
        }
    }
    
    private final ThreadLocal<Expression<?>> current = new ThreadLocal<>();
    
//    private final PathFactory pathFactory;
        
    @SuppressWarnings("unchecked")
    public <A extends Expression<?>> A getCurrentExpression() {
        return (A) this.current.get();
    }
    
    /**
     * <p>Returns the current thread bound expression and resets it.</p>
     * 
     * @param <A>
     *   
     * @return
     *   The current thread bound expression.
     */
    public <A extends Expression<?>> A getCurrentExpressionAndReset() {
        A current = getCurrentExpression();
        
        reset();
        
        return current;
    }

    /**
     * Reset the thread bound expression
     */
    public void reset() {
        setCurrentExpression(null);
    }
    
//    public <A> A createAlias(Class<A> clazz) {
//        return null;
//    }
    
    public <A> A createAlias(Class<A> clazz) {
        return createProxy(clazz, new Class[0], new Object[0], null);
    }
    
//    @SuppressWarnings("unchecked")
//    public <A> A createAliasForVariable(Class<A> clazz, String var) {
//        try {
//            Expression<?> path = this.pathCache.get(Pair.<Class<?>, String>of(clazz, var));
//            ManagedObject proxy = this.proxyCache.get(Pair.<Class<?>, Expression<?>>of(clazz, path));
//            
//            return (A) proxy;
//        } catch (ExecutionException exception) {
//            throw new QueryException(exception);
//        }        
//    }
//    
//    @SuppressWarnings("unchecked")
//    public <A> A createAliasForExpression(Class<A> clazz, Expression<?> path) {
//        // TODO [D049941]: Use the proxy cache
//        return createProxy(clazz, new Class[0], new Object[0], path);
//    }
    
    protected void setCurrentExpression(Expression<?> expression) {
        this.current.set(expression);
    }
    
    @SuppressWarnings("unchecked")
    protected <A> A createProxy(Class<A> clazz, Class<?>[] argumentTypes, Object[] arguments, Expression<?> path) {
        Enhancer enhancer = new Enhancer();
        enhancer.setClassLoader(AliasFactory.class.getClassLoader());
        
        if (clazz.isInterface()) {
            enhancer.setInterfaces(new Class[] {clazz, ManagedObject.class});
        } else {
            enhancer.setSuperclass(clazz);
            enhancer.setInterfaces(new Class[] {ManagedObject.class});
        }
        
        enhancer.setCallback(new MethodAccessTracker(path));
        
        return (A) enhancer.create(argumentTypes, arguments);
    }
}
