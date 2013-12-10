package com.sap.sailing.selenium.pages.gwt.query.path;

import com.sap.sailing.selenium.pages.gwt.query.Path;
import com.sap.sailing.selenium.pages.gwt.query.PathMetadata;
import com.sap.sailing.selenium.pages.gwt.query.expr.SimpleExpression;

public class SimplePath<T> extends SimpleExpression<T> implements Path<T> {
    private final PathImpl<T> pathMixin;

//    public SimplePath(Class<? extends T> type, Path<?> parent, String property) {
//        this(type, PathMetadataFactory.forProperty(parent, property));
//    }

    @SuppressWarnings("unchecked")
    public SimplePath(Class<? extends T> type, PathMetadata<?> metadata) {
        super(new PathImpl<>(type, metadata));
        
        this.pathMixin = (PathImpl<T>) this.mixin;
    }
    
//    public SimplePath(Class<? extends T> type, String var) {
//        this(type, PathMetadataFactory.forVariable(var));
//    }

    @Override
    public PathMetadata<?> getMetadata() {
        return this.pathMixin.getMetadata();
    }

    @Override
    public Path<?> getRoot() {
        return this.pathMixin.getRoot();
    }
}
