package com.sap.sailing.selenium.pages.gwt.query.path;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.sap.sailing.selenium.pages.gwt.query.Path;
import com.sap.sailing.selenium.pages.gwt.query.PathMetadata;
import com.sap.sailing.selenium.pages.gwt.query.QueryException;
import com.sap.sailing.selenium.pages.gwt.query.expr.ImmutableExpression;

/**
 * <p>PathImpl defines a default implementation of the Path interface</p>
 * 
 * @author
 *   D049941
 * @param <T>
 */
public class PathImpl<T> extends ImmutableExpression<T> implements Path<T> {
    private final PathMetadata<?> metadata;
    
    // QUESTION [D049941]: What is the root good for?
    private final Path<?> root;
    
    public PathImpl(Class<? extends T> type, PathMetadata<?> metadata) {
        super(type);
        
        this.metadata = metadata;
        this.root = metadata.getRoot() != null ? metadata.getRoot() : this;
    }
    
    @Override
    public final PathMetadata<?> getMetadata() {
        return this.metadata;
    }
    
    @Override
    public final Path<?> getRoot() {
        return this.root;
    }

    @Override
    public final boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        
        if (!(object instanceof Path<?>)) {
            return false;
        }
        Path<?> other = (Path<?>) object;
        
        return this.metadata.equals(other.getMetadata());
    }
    
    @Override
    public int hashCode() {
        return this.metadata.hashCode();
    }

    @Override
    @SuppressWarnings("unchecked")
    public T evaluate(Object context) {
        Method method = this.metadata.getMethod();
        Object[] arguments = this.metadata.getArguments();
        
        try {
            return (T) method.invoke(context, arguments);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException exception) {
            throw new QueryException(exception);
        }
    }
}
