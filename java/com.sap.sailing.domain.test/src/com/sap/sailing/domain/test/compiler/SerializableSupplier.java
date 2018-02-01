package com.sap.sailing.domain.test.compiler;

import java.io.Serializable;
import java.util.function.Supplier;

@FunctionalInterface
public interface SerializableSupplier extends Serializable, Supplier<TypeDefinition> {
}
