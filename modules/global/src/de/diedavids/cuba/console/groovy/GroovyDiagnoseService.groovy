package de.diedavids.cuba.console.groovy

import de.diedavids.cuba.console.diagnose.DiagnoseExecution


interface GroovyDiagnoseService {
    String NAME = 'ddrd_GroovyConsoleService'

    DiagnoseExecution runGroovyDiagnose(DiagnoseExecution diagnoseExecution)
}