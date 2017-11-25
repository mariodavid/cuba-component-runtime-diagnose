package de.diedavids.cuba.runtimediagnose.diagnose

import groovy.transform.CompileStatic

@CompileStatic
enum DiagnoseType {
    GROOVY,
    SQL,
    JPQL
}