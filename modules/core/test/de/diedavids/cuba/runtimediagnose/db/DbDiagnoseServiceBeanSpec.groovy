package de.diedavids.cuba.runtimediagnose.db

import com.haulmont.cuba.core.EntityManager
import com.haulmont.cuba.core.Persistence
import com.haulmont.cuba.core.Transaction
import com.haulmont.cuba.core.global.TimeSource
import com.haulmont.cuba.core.global.UserSessionSource
import com.haulmont.cuba.security.entity.User
import com.haulmont.cuba.security.global.UserSession
import de.diedavids.cuba.runtimediagnose.diagnose.DiagnoseExecution
import de.diedavids.cuba.runtimediagnose.diagnose.DiagnoseExecutionFactory
import de.diedavids.cuba.runtimediagnose.diagnose.DiagnoseExecutionLogService
import de.diedavids.cuba.runtimediagnose.diagnose.DiagnoseType
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import net.sf.jsqlparser.statement.Statement
import net.sf.jsqlparser.statement.Statements
import org.eclipse.persistence.internal.jpa.EJBQueryImpl
import org.eclipse.persistence.queries.DatabaseQuery
import spock.lang.Specification
import spock.lang.Unroll

import javax.sql.DataSource

class DbDiagnoseServiceBeanSpec extends Specification {


    MockableDbDiagnoseServiceBean dbDiagnoseServiceBean

    DbQueryParser dbQueryParser
    SqlSelectResultFactory selectResultFactory
    Transaction transaction
    Persistence persistence
    DataSource dataSource
    Sql sql
    DiagnoseExecutionLogService diagnoseExecutionLogService
    TimeSource timeSource
    UserSessionSource userSessionSource
    DiagnoseExecutionFactory diagnoseExecutionFactory
    Date currentDate

    def setup() {
        dbQueryParser = Mock(DbQueryParser)
        selectResultFactory = Mock(SqlSelectResultFactory)
        transaction = Mock(Transaction)
        persistence = Mock(Persistence)
        sql = Mock(Sql) {
            rows(_ as String) >> new ArrayList<GroovyRowResult>()
        }
        diagnoseExecutionLogService = Mock(DiagnoseExecutionLogService)

        timeSource = Mock(TimeSource)

        currentDate = new Date()
        timeSource.currentTimestamp() >> this.currentDate

        userSessionSource = Mock(UserSessionSource)
        def userSession = Mock(UserSession)
        userSession.getCurrentOrSubstitutedUser() >> new User(login: "admin")
        userSessionSource.getUserSession() >> userSession

        diagnoseExecutionFactory = Mock(DiagnoseExecutionFactory)
        dbDiagnoseServiceBean = new MockableDbDiagnoseServiceBean(
                dbQueryParser: dbQueryParser,
                selectResultFactory: selectResultFactory,
                persistence: persistence,
                sql: sql,
                diagnoseExecutionLogService: diagnoseExecutionLogService,
                timeSource: timeSource,
                userSessionSource: userSessionSource,
                diagnoseExecutionFactory: diagnoseExecutionFactory
        )

        dataSource = Mock(DataSource)
        persistence.getDataSource() >> dataSource
        persistence.getTransaction() >> transaction

    }

    def "runSqlDiagnose uses DbQueryParser to analyse the sql statements"() {

        given:
        def sqlString = 'SELECT * FROM SEC_USER;'
        when:
        dbDiagnoseServiceBean.runSqlDiagnose(sqlString, DiagnoseType.SQL)
        then:
        1 * dbQueryParser.analyseQueryString(sqlString, DiagnoseType.SQL)
    }

    def "runSqlDiagnose uses DbQueryParser to analyse the jpql statements"() {

        given:
        def sqlString = 'select u from sec$User u'
        when:
        dbDiagnoseServiceBean.runSqlDiagnose(sqlString, DiagnoseType.JPQL)
        then:
        1 * dbQueryParser.analyseQueryString(sqlString, DiagnoseType.JPQL)
    }

    def "runSqlDiagnose creates a SQL object with the datasource from persistence"() {
        given:
        def sqlString = 'SELECT * FROM SEC_USER;'

        and: "the db parser returns one db statement"
        def statements = Mock(Statements)
        def sqlStatement = Mock(Statement)
        sqlStatement.toString() >> sqlString
        statements.getStatements() >> [sqlStatement]

        dbQueryParser.analyseQueryString(_, _) >> statements

        and:
        diagnoseExecutionFactory.createAdHocDiagnoseExecution(_ as String, _ as DiagnoseType) >> new DiagnoseExecution()

        when:
        dbDiagnoseServiceBean.runSqlDiagnose(sqlString, DiagnoseType.SQL)

        then:
        dbDiagnoseServiceBean.actualDataSource == dataSource
    }

    def "runSqlDiagnose executes the sql script if there is at least one result of the sql parser"() {

        given:
        def sqlString = 'SELECT * FROM SEC_USER;'

        and: "the db parser returns one db statement"
        def statements = Mock(Statements)
        def sqlStatement = Mock(Statement)
        sqlStatement.toString() >> sqlString
        statements.getStatements() >> [sqlStatement]

        dbQueryParser.analyseQueryString(_, _) >> statements

        and:
        diagnoseExecutionFactory.createAdHocDiagnoseExecution(_, _) >> new DiagnoseExecution()

        when:
        dbDiagnoseServiceBean.runSqlDiagnose(sqlString, DiagnoseType.SQL)

        then:
        1 * sql.rows(sqlString)
    }

    def "runSqlDiagnose executes the jpql script if there is at least one result of the jpql parser"() {

        given:
        def sqlString = 'select u from sec$User u'

        and: "the db parser returns one jpql statement"
        def statements = Mock(Statements)
        def sqlStatement = Mock(Statement)
        sqlStatement.toString() >> sqlString
        statements.getStatements() >> [sqlStatement]

        dbQueryParser.analyseQueryString(_ as String, DiagnoseType.JPQL) >> statements

        and:
        diagnoseExecutionFactory.createAdHocDiagnoseExecution(_ as String, _ as DiagnoseType) >> new DiagnoseExecution()

        when:
        dbDiagnoseServiceBean.runSqlDiagnose(sqlString, DiagnoseType.JPQL)

        then:
        1 * persistence.callInTransaction(_ as Transaction.Callable)
    }

    def "runSqlDiagnose executes no sql script if there is no result of the sql parser"() {

        given:
        def sqlString = 'SELECT * FROM SEC_USER;'

        and: "the db parser returns one db statement"
        def statements = Mock(Statements)
        statements.getStatements() >> []

        dbQueryParser.analyseQueryString(_, DiagnoseType.SQL) >> statements

        when:
        dbDiagnoseServiceBean.runSqlDiagnose(sqlString, DiagnoseType.SQL)

        then:
        0 * sql.rows(_)
    }

    def "runSqlDiagnose executes no sql script if there is no result of the jpql parser"() {

        given:
        def sqlString = 'select u from sec$User u'

        and: "the db parser returns one db statement"
        def statements = Mock(Statements)
        statements.getStatements() >> []

        dbQueryParser.analyseQueryString(_, DiagnoseType.JPQL) >> statements

        when:
        dbDiagnoseServiceBean.runSqlDiagnose(sqlString, DiagnoseType.JPQL)

        then:
        0 * sql.rows(_)
    }

    def "runSqlDiagnose executes no jpql script if there is no result of the jpql parser"() {

        given:
        def sqlString = 'select u from sec$User u'

        and: "the db parser returns one db statement"
        def statements = Mock(Statements)
        statements.getStatements() >> []

        dbQueryParser.analyseQueryString(_, DiagnoseType.JPQL) >> statements

        when:
        dbDiagnoseServiceBean.runSqlDiagnose(sqlString, DiagnoseType.JPQL)

        then:
        0 * sql.rows(_)
    }

    def "runSqlDiagnose adds metainformation to the diagnoseExecution"() {

        given:
        def diagnoseExecution = new DiagnoseExecution()

        when:
        dbDiagnoseServiceBean.runSqlDiagnose(diagnoseExecution, DiagnoseType.SQL)

        then:
        diagnoseExecution.executionTimestamp == currentDate
        diagnoseExecution.executionUser == "admin"
    }

    def "runSqlDiagnose logs the diagnose execution"() {

        given:
        def diagnoseExecution = new DiagnoseExecution()

        when:
        dbDiagnoseServiceBean.runSqlDiagnose(diagnoseExecution, DiagnoseType.SQL)

        then:
        1 * diagnoseExecutionLogService.logDiagnoseExecution(diagnoseExecution)
    }

    def "getQueryResult with argument different from SQL or JPQL throw IllegalArgumentException"() {

        given:
        def diagnoseType = DiagnoseType.GROOVY
        Statements statements = Mock(Statements)

        when:
        dbDiagnoseServiceBean.getQueryResult(diagnoseType, _ as String, statements)

        then:
        thrown(IllegalArgumentException)
    }

    @Unroll
    def "getSqlQuery return null if argument is empty"() {

        given:
        EntityManager em = Mock()
        javax.persistence.EntityManager delegate = Mock()
        EJBQueryImpl query = Mock()
        DatabaseQuery databaseQuery = Mock()

        databaseQuery.getSQLString() >> sqlQuery
        query.databaseQuery >> databaseQuery
        delegate.createQuery(jpqlQuery) >> query
        em.delegate >> delegate
        persistence.getEntityManager() >> em


        when:
        String res = dbDiagnoseServiceBean.getSqlQuery(jpqlQuery)

        then:
        res == sqlQuery

        where:
        jpqlQuery | sqlQuery
        null      | null
        ''        | null
    }

    def "getSqlQuery thrown IllegalArgumentException if argument is not jpql"() {

        given:
        String noJpql = "this string is not JPQL"
        EntityManager em = Mock()
        javax.persistence.EntityManager delegate = Mock()
        delegate.createQuery(_) >> { throw new IllegalArgumentException() }
        em.delegate >> delegate
        persistence.getEntityManager() >> em

        when:
        dbDiagnoseServiceBean.getSqlQuery(noJpql)

        then:
        thrown(IllegalArgumentException)
    }

    def "getSqlQuery if argument is correct jpql"() {

        given:
        String jpqlQuery = 'select u from sec$User u'
        String sqlQuery = 'SELECT * FROM SEC_USER'
        EntityManager em = Mock()
        javax.persistence.EntityManager delegate = Mock()
        EJBQueryImpl query = Mock()
        DatabaseQuery databaseQuery = Mock()

        databaseQuery.getSQLString() >> sqlQuery
        query.databaseQuery >> databaseQuery
        delegate.createQuery(_) >> query
        em.delegate >> delegate
        persistence.getEntityManager() >> em

        when:
        String result = dbDiagnoseServiceBean.getSqlQuery(jpqlQuery)

        then:
        result == sqlQuery
    }
}

class MockableDbDiagnoseServiceBean extends DbDiagnoseServiceBean {

    Sql sql

    DataSource actualDataSource

    @Override
    protected Sql createSqlConnection(DataSource dataSource) {
        actualDataSource = dataSource
        sql
    }
}