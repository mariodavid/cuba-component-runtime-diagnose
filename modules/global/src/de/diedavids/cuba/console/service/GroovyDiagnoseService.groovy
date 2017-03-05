package de.diedavids.cuba.console.service

import de.diedavids.cuba.console.DiagnoseExecution


public interface GroovyDiagnoseService {
    String NAME = "console_GroovyConsoleService";

    DiagnoseExecution runGroovyDiagnose(DiagnoseExecution diagnoseExecution)
}