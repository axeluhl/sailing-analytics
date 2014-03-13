package com.sap.sailing.selenium.pages.gwt.query.expr;

import java.util.Objects;

import com.sap.sailing.selenium.pages.gwt.query.Constant;

/**
 * <p>ConstantExpression is the default implementation of the Constant interface.</p>
 * 
 * @author D049941
 *
 * @param <T>
 *   The type the constant is bound to.
 */
public class ConstantExpression<T> extends ImmutableExpression<T> implements Constant<T> {
    private static final Constant<Boolean> FALSE = new ConstantExpression<>(Boolean.FALSE);
    
    private static final Constant<Boolean> TRUE = new ConstantExpression<>(Boolean.TRUE);
    
    public static Constant<Boolean> create(boolean value) {
        return (value ? TRUE : FALSE);
    }
    
    public static Constant<Boolean> create(Boolean value) {
        return create(value.booleanValue());
    }
    
    public static Constant<String> create(String value) {
        return new ConstantExpression<>(value);
    }
    
    // TODO [D049941]: Add factory methods for other primitive types and wrappers!
    
    private final T constant;
    
    
    /**
     * Creates a new Constant for the given object
     * 
     * @param constant
     */
    @SuppressWarnings("unchecked")
    public ConstantExpression(T constant) {
        this((Class<T>) constant.getClass(), constant);
    }
    
    /**
     * Creates a new Constant of the given type for the given object
     * 
     * @param type
     * @param constant
     */
    public ConstantExpression(Class<T> type, T constant) {
        super(type);
        
        this.constant = constant;
    }
    
    @Override
    public T getConstant() {
        return this.constant;
    }
    
    @Override
    public boolean equals(Object object) {
        if (object == this)
            return true;
        
        if (!(object instanceof Constant<?>))
            return false;
        
        Constant<?> other = (Constant<?>) object;
        
        return Objects.equals(other.getConstant(), this.constant);
    }
    
    @Override
    public int hashCode() {
        return (this.constant == null ? 0 : this.constant.hashCode());
    }
    
    @Override
    public T evaluate(Object argument) {
        return getConstant();
    }
}
