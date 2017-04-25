package de.diedavids.cuba.runtimediagnose.groovy

import de.diedavids.cuba.runtimediagnose.diagnose.DiagnoseExecution


interface GroovyDiagnoseService {
    String NAME = 'ddcrd_GroovyConsoleService'

    DiagnoseExecution runGroovyDiagnose(DiagnoseExecution diagnoseExecution)
}