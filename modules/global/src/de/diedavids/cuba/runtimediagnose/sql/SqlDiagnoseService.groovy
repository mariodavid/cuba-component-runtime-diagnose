package de.diedavids.cuba.runtimediagnose.sql

import de.diedavids.cuba.runtimediagnose.diagnose.DiagnoseExecution

interface SqlDiagnoseService {
    String NAME = 'ddcrd_SqlConsoleService'

    SqlSelectResult runSqlDiagnose(String sqlString)
    DiagnoseExecution runSqlDiagnose(DiagnoseExecution diagnoseExecution)
}