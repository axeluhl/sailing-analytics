package com.sap.sailing.domain.orc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/***
 * A IgnoreInvalidOrcCerticates is a annotation which is used to identify whether we need to execute the
 * {@link FailIfNoValidOrcCertificateRule} for that particular test case. i.e. you can find
 * the usage of {@link IgnoreInvalidOrcCertificates} in
 * {@link TestORCPublicCertificateDatabase} class.
 * 
 * @author Usman Ali
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
@Inherited
public @interface IgnoreInvalidOrcCertificates {

}