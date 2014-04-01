package com.sap.sailing.selenium.pages.gwt.query.path;

import com.sap.sailing.selenium.pages.gwt.query.Path;
import com.sap.sailing.selenium.pages.gwt.query.PathMetadata;
import com.sap.sailing.selenium.pages.gwt.query.expr.StringExpression;

public class StringPath extends StringExpression implements Path<String> {
    private final PathImpl<String> pathMixin;
    
    @SuppressWarnings("unchecked")
    public StringPath(PathMetadata<?> metadata) {
        super(new PathImpl<>(String.class, metadata));
        
        this.pathMixin = (PathImpl<String>) this.mixin;
    }

    @Override
    public PathMetadata<?> getMetadata() {
        return this.pathMixin.getMetadata();
    }

    @Override
    public Path<?> getRoot() {
        return this.pathMixin.getRoot();
    }

}
