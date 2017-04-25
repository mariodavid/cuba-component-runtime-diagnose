package de.diedavids.cuba.runtimediagnose

import com.haulmont.cuba.core.global.SupportedByClient
import groovy.transform.InheritConstructors


@SupportedByClient
@InheritConstructors
class SqlConsoleSecurityException extends RuntimeException {


    String sqlStatement

    String statementType

}
