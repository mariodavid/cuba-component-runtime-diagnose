package de.diedavids.cuba.runtimediagnose.sql

import de.diedavids.cuba.runtimediagnose.diagnose.DiagnoseExecution
import de.diedavids.cuba.runtimediagnose.diagnose.DiagnoseType

interface DatabaseDiagnoseService {
    String NAME = 'ddcrd_SqlConsoleService'

    DatabaseQueryResult runSqlDiagnose(String queryString, DiagnoseType diagnoseType)
    DiagnoseExecution runSqlDiagnose(DiagnoseExecution diagnoseExecution, DiagnoseType diagnoseType)
}