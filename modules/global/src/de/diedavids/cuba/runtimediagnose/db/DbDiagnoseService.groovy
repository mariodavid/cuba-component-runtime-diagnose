package de.diedavids.cuba.runtimediagnose.db

import de.diedavids.cuba.runtimediagnose.diagnose.DiagnoseExecution
import de.diedavids.cuba.runtimediagnose.diagnose.DiagnoseType

interface DbDiagnoseService {
    String NAME = 'ddcrd_SqlConsoleService'

    DbQueryResult runSqlDiagnose(String queryString, DiagnoseType diagnoseType, String dataStore)

    DiagnoseExecution runSqlDiagnose(DiagnoseExecution diagnoseExecution, DiagnoseType diagnoseType)

    String getSqlQuery(String jpqlQuery)
}