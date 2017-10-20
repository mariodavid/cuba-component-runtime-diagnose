package de.diedavids.cuba.runtimediagnose.sql

import com.haulmont.cuba.core.Persistence
import com.haulmont.cuba.core.Query
import com.haulmont.cuba.core.Transaction
import com.haulmont.cuba.core.global.TimeSource
import com.haulmont.cuba.core.global.UserSessionSource
import de.diedavids.cuba.runtimediagnose.diagnose.DiagnoseExecution
import de.diedavids.cuba.runtimediagnose.diagnose.DiagnoseExecutionFactory
import de.diedavids.cuba.runtimediagnose.diagnose.DiagnoseExecutionLogService
import de.diedavids.cuba.runtimediagnose.diagnose.DiagnoseType
import groovy.sql.Sql
import net.sf.jsqlparser.parser.CCJSqlParserUtil
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
    SqlSelectResult runSqlDiagnose(String queryString, DiagnoseType diagnoseType) {

        def queryStatements = sqlConsoleParser.analyseSql(queryString)

        if (statementsAvailable(queryStatements)) {
            def queryStatement = queryStatements.statements[0].toString()

            DiagnoseExecution diagnoseExecution
            try {
                SqlSelectResult sqlSelectResult
                switch (diagnoseType) {
                    case DiagnoseType.JPQL:
                        sqlSelectResult = executeJpqlStatement(queryStatement)
                        diagnoseExecution = createAdHocDiagnose(queryStatement, DiagnoseType.JPQL)
                        break
                    case DiagnoseType.SQL:
                        def sql = createSqlConnection(persistence.dataSource)
                        sqlSelectResult = executeStatement(sql, queryStatement)
                        diagnoseExecution = createAdHocDiagnose(queryStatement, DiagnoseType.SQL)
                        break
                    default:
                        throw new IllegalArgumentException("DiagnoseType is not supported (" + diagnoseType + ")")
                }

                diagnoseExecution.handleSuccessfulExecution(sqlSelectResult.entities[0].toString())
                diagnoseExecutionLogService.logDiagnoseExecution(diagnoseExecution)
                return sqlSelectResult
            } catch (Exception e) {
                diagnoseExecution.handleErrorExecution(e)
                diagnoseExecutionLogService.logDiagnoseExecution(diagnoseExecution)
                selectResultFactory.createFromRows([])
            }
        }
    }

    private SqlSelectResult executeJpqlStatement(String queryStatement) {
        Statements statements = CCJSqlParserUtil.parseStatements(queryStatement)

        return persistence.callInTransaction {
            Query q = persistence.getEntityManager().createQuery(queryStatement)

            if (sqlConsoleParser.containsDataManipulation(statements)) {
                q.executeUpdate()
                return new SqlSelectResult()
            } else {
                return selectResultFactory.createFromRows(q.getResultList())
            }
        }
    }

    private SqlSelectResult executeStatement(Sql sql, String queryString) {
        def rows = sql.rows(queryString)
        selectResultFactory.createFromRows(rows)
    }

    private DiagnoseExecution createAdHocDiagnose(String sqlStatement, DiagnoseType diagnoseType) {
        def diagnoseExecution = diagnoseExecutionFactory.createAdHocDiagnoseExecution(sqlStatement, diagnoseType)
        setDiagnoseExecutionMetadata(diagnoseExecution)
        diagnoseExecution
    }

    private void setDiagnoseExecutionMetadata(DiagnoseExecution diagnoseExecution) {
        diagnoseExecution.executionTimestamp = timeSource.currentTimestamp()
        diagnoseExecution.executionUser = userSessionSource.userSession.currentOrSubstitutedUser.login
    }

    @Override
    DiagnoseExecution runSqlDiagnose(DiagnoseExecution diagnoseExecution, DiagnoseType diagnoseType) {
        if (diagnoseExecution) {
            setDiagnoseExecutionMetadata(diagnoseExecution)

            try {
                def sqlSelectResult = runSqlDiagnose(diagnoseExecution.diagnoseScript, diagnoseType)
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