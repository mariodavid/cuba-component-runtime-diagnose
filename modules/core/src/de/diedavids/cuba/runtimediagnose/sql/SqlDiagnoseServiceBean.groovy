package de.diedavids.cuba.runtimediagnose.sql

import com.haulmont.cuba.core.Persistence
import com.haulmont.cuba.core.Query
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

    @Inject
    JpqlConsoleParser jpqlConsoleParser

    @Override
    SqlSelectResult runSqlDiagnose(String queryString, DiagnoseType diagnoseType) {
        Statements queryStatements = sqlConsoleParser.analyseSql(queryString)
        // analyzeSql are gonna return null in some cases
        if (!statementsAvailable(queryStatements)) {
            return selectResultFactory.createFromRows([])
        }

        if (DiagnoseType.JPQL == diagnoseType) {
            jpqlConsoleParser.analyseJpql(queryString)
        }

        def queryStatement = queryStatements.statements[0].toString()
        DiagnoseExecution diagnoseExecution = createAdHocDiagnose(queryStatement, diagnoseType)
        executeAdHocDiagnose(diagnoseType, queryStatement, queryStatements, diagnoseExecution)
    }

    private void executeAdHocDiagnose(DiagnoseType diagnoseType, String queryStatement, Statements queryStatements, DiagnoseExecution diagnoseExecution) {
        SqlSelectResult sqlSelectResult
        try {
            sqlSelectResult = getQueryResult(diagnoseType, queryStatement, queryStatements)
            diagnoseExecution.handleSuccessfulExecution(sqlSelectResult.entities[0].toString())
            diagnoseExecutionLogService.logDiagnoseExecution(diagnoseExecution)
        } catch (Exception e) {
            sqlSelectResult = selectResultFactory.createFromRows([])
            diagnoseExecution.handleErrorExecution(e)
            diagnoseExecutionLogService.logDiagnoseExecution(diagnoseExecution)
        }

        sqlSelectResult
    }

    protected SqlSelectResult getQueryResult(DiagnoseType diagnoseType, String queryStatement, Statements queryStatements) {
        SqlSelectResult sqlSelectResult
        switch (diagnoseType) {
            case DiagnoseType.JPQL:
                sqlSelectResult = executeJpqlStatement(queryStatement, queryStatements)
                break
            case DiagnoseType.SQL:
                def sql = createSqlConnection(persistence.dataSource)
                sqlSelectResult = executeStatement(sql, queryStatement)
                break
            default:
                throw new IllegalArgumentException('DiagnoseType is not supported (' + diagnoseType + ')')
        }
        sqlSelectResult
    }

    protected SqlSelectResult executeJpqlStatement(String queryStatement, Statements queryStatements) {
        persistence.callInTransaction {
            Query q = persistence.entityManager.createQuery(queryStatement)

            if (sqlConsoleParser.containsDataManipulation(queryStatements)) {
                q.executeUpdate()
                new SqlSelectResult()
            } else {
                selectResultFactory.createFromRows(q.resultList)
            }
        }
    }

    protected SqlSelectResult executeStatement(Sql sql, String queryString) {
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