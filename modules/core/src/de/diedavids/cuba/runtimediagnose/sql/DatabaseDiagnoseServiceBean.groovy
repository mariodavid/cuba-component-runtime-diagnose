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

@Service(DatabaseDiagnoseService.NAME)
class DatabaseDiagnoseServiceBean implements DatabaseDiagnoseService {

    @Inject
    Persistence persistence

    @Inject
    SqlSelectResultFactory selectResultFactory

    @Inject
    DatabaseQueryParser databaseQueryParser

    @Inject
    TimeSource timeSource

    @Inject
    DiagnoseExecutionLogService diagnoseExecutionLogService

    @Inject
    UserSessionSource userSessionSource

    @Inject
    DiagnoseExecutionFactory diagnoseExecutionFactory


    @Override
    DatabaseQueryResult runSqlDiagnose(String queryString, DiagnoseType diagnoseType) {
        Statements queryStatements = databaseQueryParser.analyseQueryString(queryString, diagnoseType)

        if (!statementsAvailable(queryStatements)) {
            return selectResultFactory.createFromRows([])
        }

        def queryStatement = queryStatements.statements[0].toString()
        DiagnoseExecution diagnoseExecution = createAdHocDiagnose(queryStatement, diagnoseType)
        DatabaseQueryResult databaseQueryResult
        try {
            databaseQueryResult = getQueryResult(diagnoseType, queryStatement, queryStatements)
            diagnoseExecution.handleSuccessfulExecution(databaseQueryResult.entities[0].toString())
            diagnoseExecutionLogService.logDiagnoseExecution(diagnoseExecution)
        } catch (Exception e) {
            databaseQueryResult = selectResultFactory.createFromRows([])
            diagnoseExecution.handleErrorExecution(e)
            diagnoseExecutionLogService.logDiagnoseExecution(diagnoseExecution)
        }

        databaseQueryResult
    }

    protected DatabaseQueryResult getQueryResult(DiagnoseType diagnoseType, String queryStatement, Statements queryStatements) {
        DatabaseQueryResult sqlSelectResult
        switch (diagnoseType) {
            case DiagnoseType.JPQL:
                sqlSelectResult = executeJpqlStatement(queryStatement, queryStatements)
                break
            case DiagnoseType.SQL:
                def sql = createSqlConnection(persistence.dataSource)
                sqlSelectResult = executeSqlStatement(sql, queryStatement)
                break
            default:
                throw new IllegalArgumentException('DiagnoseType is not supported (' + diagnoseType + ')')
        }
        sqlSelectResult
    }

    protected DatabaseQueryResult executeJpqlStatement(String queryStatement, Statements queryStatements) {
        persistence.callInTransaction {
            Query q = persistence.entityManager.createQuery(queryStatement)

            if (databaseQueryParser.containsDataManipulation(queryStatements)) {
                q.executeUpdate()
                new DatabaseQueryResult()
            } else {
                selectResultFactory.createFromRows(q.resultList)
            }
        }
    }

    protected DatabaseQueryResult executeSqlStatement(Sql sql, String queryString) {
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