package com.sap.sailing.selenium.core;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import org.openqa.selenium.By;

import org.openqa.selenium.support.CacheLookup;

import org.openqa.selenium.support.pagefactory.ByChained;

public class Annotations {
    public static boolean isAnnotationPresent(Field field) {
        return (field.getAnnotation(FindBys.class) != null || field.getAnnotation(FindBy.class) != null);
    }

    private Field field;

    public Annotations(Field field) {
        this.field = field;
    }

    public boolean isLookupCached() {
        return (this.field.getAnnotation(CacheLookup.class) != null);
    }

    public By buildBy() {
        assertValidAnnotations();

        FindBys findBys = this.field.getAnnotation(FindBys.class);
        if (findBys != null) {
            return buildByFromFindBys(findBys);
        }

        FindBy findBy = this.field.getAnnotation(FindBy.class);
        if (findBy != null) {
            return buildByFromFindBy(findBy);
        }

        throw new IllegalArgumentException("Cannot determine how to locate element " + this.field); //$NON-NLS-1$
    }

    protected By buildByFromFindBy(FindBy findBy) {
        assertValidFindBy(findBy);

        try {
            Class<? extends By> how = findBy.how();
            Constructor<? extends By> constructor = how.getConstructor(String.class);
            String using = findBy.using();

            return constructor.newInstance(using);
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    protected By buildByFromFindBys(FindBys findBys) {
        assertValidFindBys(findBys);

        FindBy[] value = findBys.value();
        By[] bys = new By[value.length];
        
        for (int i = 0; i < value.length; i++) {
            bys[i] = buildByFromFindBy(value[i]);
        }

        return new ByChained(bys);
    }

    private void assertValidAnnotations() {
        int count = 0;

        if (this.field.getAnnotation(FindBys.class) != null) {
            count += 1;
        }

        if (this.field.getAnnotation(FindBy.class) != null) {
            count += 1;
        }

        if (count > 1) {
            throw new IllegalArgumentException("You may use only one of '@FindBy' and '@FindBys' annotations");
        }
    }

    private void assertValidFindBys(FindBys findBys) {
        for (FindBy findBy : findBys.value()) {
            assertValidFindBy(findBy);
        }
    }

    private void assertValidFindBy(FindBy findBy) {
        if (findBy.how() == null || findBy.using() == null) {
            throw new IllegalArgumentException("You must define the 'how' property as well as the 'using' property");
        }
    }

}
