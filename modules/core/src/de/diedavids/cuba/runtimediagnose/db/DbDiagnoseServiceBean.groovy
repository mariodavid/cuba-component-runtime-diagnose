package de.diedavids.cuba.runtimediagnose.db

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
import org.apache.commons.lang3.StringUtils
import org.eclipse.persistence.internal.jpa.EJBQueryImpl
import org.springframework.stereotype.Service

import javax.inject.Inject
import javax.persistence.EntityManager
import javax.sql.DataSource
import javax.transaction.Transactional

@Service(DbDiagnoseService.NAME)
class DbDiagnoseServiceBean implements DbDiagnoseService {

    @Inject
    Persistence persistence

    @Inject
    SqlSelectResultFactory selectResultFactory

    @Inject
    DbQueryParser dbQueryParser

    @Inject
    DbSqlExecutor dbSqlExecutor

    @Inject
    TimeSource timeSource

    @Inject
    DiagnoseExecutionLogService diagnoseExecutionLogService

    @Inject
    UserSessionSource userSessionSource

    @Inject
    DiagnoseExecutionFactory diagnoseExecutionFactory


    @Override
    DbQueryResult runSqlDiagnose(String queryString, DiagnoseType diagnoseType) {
        Statements queryStatements = dbQueryParser.analyseQueryString(queryString, diagnoseType)

        if (!statementsAvailable(queryStatements)) {
            return selectResultFactory.createFromRows([])
        }

        def queryStatement = queryStatements.statements[0].toString()
        DiagnoseExecution diagnoseExecution = createAdHocDiagnose(queryStatement, diagnoseType)
        DbQueryResult dbQueryResult
        try {
            dbQueryResult = getQueryResult(diagnoseType, queryStatement, queryStatements)

            diagnoseExecution.handleSuccessfulExecution(getResultMessage(dbQueryResult))
            diagnoseExecutionLogService.logDiagnoseExecution(diagnoseExecution)
        } catch (Exception e) {
            dbQueryResult = selectResultFactory.createFromRows([])
            diagnoseExecution.handleErrorExecution(e)
            diagnoseExecutionLogService.logDiagnoseExecution(diagnoseExecution)
        }

        dbQueryResult
    }

    protected String getResultMessage(DbQueryResult dbQueryResult) {
        String resultMessage = ''
        if (dbQueryResult.empty) {
            resultMessage = 'Execution successful'
        } else {
            resultMessage = dbQueryResult.entities[0].toString()
        }
        resultMessage
    }

    protected DbQueryResult getQueryResult(DiagnoseType diagnoseType, String queryStatement, Statements queryStatements) {
        DbQueryResult dbQueryResult
        switch (diagnoseType) {
            case DiagnoseType.JPQL:
                dbQueryResult = executeJpqlStatement(queryStatement, queryStatements)
                break
            case DiagnoseType.SQL:
                def sql = createSqlConnection(persistence.dataSource)
                dbQueryResult = executeSqlStatement(sql, queryStatements)
                break
            default:
                throw new IllegalArgumentException('DiagnoseType is not supported (' + diagnoseType + ')')
        }
        dbQueryResult
    }

    protected DbQueryResult executeJpqlStatement(String queryStatement, Statements queryStatements) {
        persistence.callInTransaction {
            Query q = persistence.entityManager.createQuery(queryStatement)

            if (dbQueryParser.containsDataManipulation(queryStatements)) {
                q.executeUpdate()
                new DbQueryResult()
            } else {
                selectResultFactory.createFromRows(q.resultList)
            }
        }
    }

    protected DbQueryResult executeSqlStatement(Sql sql, Statements queryStatements) {
        dbSqlExecutor.executeStatement(sql, queryStatements.statements[0])
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

    @SuppressWarnings(['UnnecessaryGetter'])
    @Transactional
    @Override
    String getSqlQuery(String jpqlQuery) {
        if (StringUtils.isBlank(jpqlQuery)) {
            return null
        }

        EntityManager eclipseEm = persistence.entityManager.delegate
        EJBQueryImpl query = eclipseEm.createQuery(jpqlQuery) as EJBQueryImpl

        query.databaseQuery.getSQLString()
    }

    protected boolean statementsAvailable(Statements sqlStatements) {
        sqlStatements && sqlStatements.statements
    }

    protected Sql createSqlConnection(DataSource dataSource) {
        new Sql(dataSource)
    }

}