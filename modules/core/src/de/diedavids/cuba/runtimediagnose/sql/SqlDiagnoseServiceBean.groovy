package de.diedavids.cuba.runtimediagnose.sql

import com.haulmont.cuba.core.Persistence
import com.haulmont.cuba.core.global.TimeSource
import com.haulmont.cuba.core.global.UserSessionSource
import de.diedavids.cuba.runtimediagnose.diagnose.DiagnoseExecution
import de.diedavids.cuba.runtimediagnose.diagnose.DiagnoseExecutionLogService
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

    @Override
    SqlSelectResult runSqlDiagnose(String sqlString) {

        def sqlStatements = sqlConsoleParser.analyseSql(sqlString)
        def sql = createSqlConnection(persistence.dataSource)

        if (statementsAvailable(sqlStatements)) {
            def rows = sql.rows(sqlStatements.statements[0].toString())
            selectResultFactory.createFromRows(rows)
        }
    }

    @Override
    DiagnoseExecution runSqlDiagnose(DiagnoseExecution diagnoseExecution) {
        if (diagnoseExecution) {


            diagnoseExecution.executionTimestamp = timeSource.currentTimestamp()
            diagnoseExecution.executionUser = userSessionSource.userSession.currentOrSubstitutedUser.login

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