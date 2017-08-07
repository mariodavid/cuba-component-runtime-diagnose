package de.diedavids.cuba.runtimediagnose.sql

import com.haulmont.cuba.core.Persistence
import com.haulmont.cuba.core.global.TimeSource
import com.haulmont.cuba.core.global.UserSessionSource
import de.diedavids.cuba.runtimediagnose.diagnose.DiagnoseExecution
import de.diedavids.cuba.runtimediagnose.diagnose.DiagnoseExecutionFactory
import de.diedavids.cuba.runtimediagnose.diagnose.DiagnoseExecutionLogService
import de.diedavids.cuba.runtimediagnose.diagnose.DiagnoseType
import groovy.sql.Sql
import net.sf.jsqlparser.statement.Statements
import org.springframework.stereotype.Service

import javax.inject.Inject
import javax.sql.DataSource

@Service(SqlDiagnoseService.NAME)
class SqlDiagnoseServiceBean implements SqlDiagnoseService {

    @Inject
    Persistence persistence

    @Inject
    SqlSelectResultFactory selectResultFactory

    @Inject
    SqlConsoleParser sqlConsoleParser

    @Inject
    TimeSource timeSource

    @Inject
    DiagnoseExecutionLogService diagnoseExecutionLogService

    @Inject
    UserSessionSource userSessionSource

    @Inject
    DiagnoseExecutionFactory diagnoseExecutionFactory

    @Override
    SqlSelectResult runSqlDiagnose(String sqlString) {
        def sql = createSqlConnection(persistence.dataSource)
        def sqlStatements = sqlConsoleParser.analyseSql(sqlString)

        if (statementsAvailable(sqlStatements)) {
            def sqlStatement = sqlStatements.statements[0].toString()
            DiagnoseExecution diagnoseExecution = createAdHocDiagnose(sqlStatement)

            try {
                SqlSelectResult sqlSelectResult = executeSqlStatement(sql, sqlStatement)
                diagnoseExecution.handleSuccessfulExecution(sqlSelectResult.entities[0].toString())
                diagnoseExecutionLogService.logDiagnoseExecution(diagnoseExecution)
                sqlSelectResult
            }
            catch (Exception e) {
                diagnoseExecution.handleErrorExecution(e)
                diagnoseExecutionLogService.logDiagnoseExecution(diagnoseExecution)
                selectResultFactory.createFromRows([])
            }
        }
    }

    private SqlSelectResult executeSqlStatement(Sql sql, String sqlStatement) {
        def rows = sql.rows(sqlStatement)
        selectResultFactory.createFromRows(rows)
    }

    private DiagnoseExecution createAdHocDiagnose(String sqlStatement) {
        def diagnoseExecution = diagnoseExecutionFactory.createAdHocDiagnoseExecution(sqlStatement, DiagnoseType.SQL)
        setDiagnoseExecutionMetadata(diagnoseExecution)
        diagnoseExecution
    }

    private void setDiagnoseExecutionMetadata(DiagnoseExecution diagnoseExecution) {
        diagnoseExecution.executionTimestamp = timeSource.currentTimestamp()
        diagnoseExecution.executionUser = userSessionSource.userSession.currentOrSubstitutedUser.login
    }

    @Override
    DiagnoseExecution runSqlDiagnose(DiagnoseExecution diagnoseExecution) {
        if (diagnoseExecution) {
            setDiagnoseExecutionMetadata(diagnoseExecution)

            try {
                def sqlSelectResult = runSqlDiagnose(diagnoseExecution.diagnoseScript)
                // TODO: create CSV file with content
                diagnoseExecution.handleSuccessfulExecution(sqlSelectResult.entities[0].toString())

            }
            catch (Exception e) {
                diagnoseExecution.handleErrorExecution(e)
            }

            diagnoseExecutionLogService.logDiagnoseExecution(diagnoseExecution)

            diagnoseExecution
        }
    }

    protected boolean statementsAvailable(Statements sqlStatements) {
        sqlStatements && sqlStatements.statements
    }

    protected Sql createSqlConnection(DataSource dataSource) {
        new Sql(dataSource)
    }

}