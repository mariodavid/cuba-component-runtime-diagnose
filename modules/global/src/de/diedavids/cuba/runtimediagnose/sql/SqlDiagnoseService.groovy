package de.diedavids.cuba.runtimediagnose.sql

import de.diedavids.cuba.runtimediagnose.diagnose.DiagnoseExecution
import de.diedavids.cuba.runtimediagnose.diagnose.DiagnoseType

interface SqlDiagnoseService {
    String NAME = 'ddcrd_SqlConsoleService'

    SqlSelectResult runSqlDiagnose(String queryString, DiagnoseType diagnoseType)
    DiagnoseExecution runSqlDiagnose(DiagnoseExecution diagnoseExecution, DiagnoseType diagnoseType)
}