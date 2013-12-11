package com.sap.sailing.selenium.pages.gwt.query.path;

import com.sap.sailing.selenium.pages.gwt.query.Path;
import com.sap.sailing.selenium.pages.gwt.query.PathMetadata;
import com.sap.sailing.selenium.pages.gwt.query.expr.BooleanExpression;

/**
 * <p>BooleanPath represents boolean path expressions</p>
 * 
 * @author
 *   D049941
 */
public class BooleanPath extends BooleanExpression implements Path<Boolean> {
    private final PathImpl<Boolean> pathMixin;
    
    @SuppressWarnings("unchecked")
    public BooleanPath(PathMetadata<?> metadata) {
        super(new PathImpl<>(Boolean.class, metadata));
        
        this.pathMixin = (PathImpl<Boolean>) this.mixin;
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
