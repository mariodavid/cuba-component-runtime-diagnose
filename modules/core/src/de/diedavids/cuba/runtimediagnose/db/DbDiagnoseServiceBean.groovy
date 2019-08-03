package de.diedavids.cuba.runtimediagnose.db

import com.haulmont.cuba.core.Persistence
import com.haulmont.cuba.core.Query
import com.haulmont.cuba.core.global.Stores
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
    DbQueryResult runSqlDiagnose(
            String queryString,
            DiagnoseType diagnoseType,
            String dataStore
    ) {
        Statements queryStatements = dbQueryParser.analyseQueryString(queryString, diagnoseType)

        if (!statementsAvailable(queryStatements)) {
            return selectResultFactory.createFromRows([])
        }

        def queryStatement = queryStatements.statements[0].toString()
        DiagnoseExecution diagnoseExecution = createAdHocDiagnose(queryStatement, diagnoseType, dataStore)
        tryToRunSqlDiagnose(diagnoseType, queryStatement, queryStatements, dataStore, diagnoseExecution)
    }

    private DbQueryResult tryToRunSqlDiagnose(DiagnoseType diagnoseType, String queryStatement, Statements queryStatements, String dataStore, DiagnoseExecution diagnoseExecution) {
        DbQueryResult dbQueryResult
        try {
            dbQueryResult = getQueryResult(diagnoseType, queryStatement, queryStatements, dataStore ?: Stores.MAIN)
            diagnoseExecution.handleSuccessfulExecution(dbQueryResult.resultMessage())
            diagnoseExecutionLogService.logDiagnoseExecution(diagnoseExecution)
        } catch (Exception e) {
            dbQueryResult = selectResultFactory.createFromRows([])
            diagnoseExecution.handleErrorExecution(e)
            diagnoseExecutionLogService.logDiagnoseExecution(diagnoseExecution)
        }
        dbQueryResult
    }

    protected DbQueryResult getQueryResult(
            DiagnoseType diagnoseType,
            String queryStatement,
            Statements queryStatements,
            String dataStore
    ) {
        DbQueryResult dbQueryResult
        switch (diagnoseType) {
            case DiagnoseType.JPQL:
                dbQueryResult = executeJpqlStatement(queryStatement, queryStatements, dataStore)
                break
            case DiagnoseType.SQL:
                dbQueryResult = executeSqlStatement(queryStatements, dataStore)
                break
            default:
                throw new IllegalArgumentException('DiagnoseType is not supported (' + diagnoseType + ')')
        }
        dbQueryResult
    }

    protected DbQueryResult executeJpqlStatement(String queryStatement, Statements queryStatements, String storeName) {
        persistence.callInTransaction {
            Query q = persistence.getEntityManager(storeName).createQuery(queryStatement)

            if (dbQueryParser.containsDataManipulation(queryStatements)) {
                q.executeUpdate()
                new DbQueryResult()
            } else {
                selectResultFactory.createFromRows(q.resultList)
            }
        }
    }

    protected DbQueryResult executeSqlStatement(Statements queryStatements, String dataStore) {
        Sql sql = createSqlConnection(persistence.getDataSource(dataStore))
        dbSqlExecutor.executeStatement(sql, queryStatements.statements[0])
    }

    private DiagnoseExecution createAdHocDiagnose(String sqlStatement, DiagnoseType diagnoseType, String dataStore) {
        def diagnoseExecution = diagnoseExecutionFactory.createAdHocDiagnoseExecution(sqlStatement, diagnoseType, dataStore)
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
                def sqlSelectResult = runSqlDiagnose(diagnoseExecution.diagnoseScript, diagnoseType, diagnoseExecution.manifest.dataStore)
                diagnoseExecution.handleSuccessfulExecution(sqlSelectResult.toCSV())
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