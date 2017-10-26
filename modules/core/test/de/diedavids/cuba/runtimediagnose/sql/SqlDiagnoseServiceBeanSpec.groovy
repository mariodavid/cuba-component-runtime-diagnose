package de.diedavids.cuba.runtimediagnose.sql

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
import spock.lang.Specification

import javax.sql.DataSource

class SqlDiagnoseServiceBeanSpec extends Specification {


    MockableSqlDiagnoseServiceBean sqlConsoleService

    SqlConsoleParser sqlConsoleParser
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
        sqlConsoleParser = Mock(SqlConsoleParser)
        selectResultFactory = Mock(SqlSelectResultFactory)
        transaction = Mock(Transaction)
        persistence = Mock(Persistence)
        sql = Mock(Sql){
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
        sqlConsoleService = new MockableSqlDiagnoseServiceBean(
                sqlConsoleParser: sqlConsoleParser,
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

    def "executeSql uses SqlConsoleParser to analyse the sql statements"() {

        given:
        def sqlString = 'SELECT * FROM SEC_USER;'
        when:
        sqlConsoleService.runSqlDiagnose(sqlString, DiagnoseType.SQL)
        then:
        1 * sqlConsoleParser.analyseSql(sqlString)
    }

    def "executeJpql uses SqlConsoleParser to analyse the jpql statements"() {

        given:
        def sqlString = 'select u from sec$User u'
        when:
        sqlConsoleService.runSqlDiagnose(sqlString, DiagnoseType.JPQL)
        then:
        1 * sqlConsoleParser.analyseSql(sqlString)
    }

    def "executeSql creates a SQL object with the datasource from persistence"() {
        given:
        def sqlString = 'SELECT * FROM SEC_USER;'

        and: "the sql parser returns one sql statement"
        def statements = Mock(Statements)
        def sqlStatement = Mock(Statement)
        sqlStatement.toString() >> sqlString
        statements.getStatements() >> [sqlStatement]

        sqlConsoleParser.analyseSql(_) >> statements

        and:
        diagnoseExecutionFactory.createAdHocDiagnoseExecution(_ as String,_ as DiagnoseType) >> new DiagnoseExecution()

        when:
        sqlConsoleService.runSqlDiagnose(sqlString, DiagnoseType.SQL)

        then:
        sqlConsoleService.actualDataSource == dataSource
    }

    def "executeSql executes the sql script if there is at least one result of the sql parser"() {

        given:
        def sqlString = 'SELECT * FROM SEC_USER;'

        and: "the sql parser returns one sql statement"
        def statements = Mock(Statements)
        def sqlStatement = Mock(Statement)
        sqlStatement.toString() >> sqlString
        statements.getStatements() >> [sqlStatement]

        sqlConsoleParser.analyseSql(_) >> statements

        and:
        diagnoseExecutionFactory.createAdHocDiagnoseExecution(_,_) >> new DiagnoseExecution()

        when:
        sqlConsoleService.runSqlDiagnose(sqlString, DiagnoseType.SQL)

        then:
        1 * sql.rows(sqlString)
    }

    def "executeJpql executes the jpql script if there is at least one result of the jpql parser"() {

        given:
        def sqlString = 'select u from sec$User u'

        and: "the sql parser returns one jpql statement"
        def statements = Mock(Statements)
        def sqlStatement = Mock(Statement)
        sqlStatement.toString() >> sqlString
        statements.getStatements() >> [sqlStatement]

        sqlConsoleParser.analyseSql(_ as String) >> statements

        and:
        diagnoseExecutionFactory.createAdHocDiagnoseExecution(_ as String,_ as DiagnoseType) >> new DiagnoseExecution()

        when:
        sqlConsoleService.runSqlDiagnose(sqlString, DiagnoseType.JPQL)

        then:
        1 * persistence.callInTransaction(_ as Transaction.Callable)
    }

    def "executeSql executes no sql script if there is no result of the sql parser"() {

        given:
        def sqlString = 'SELECT * FROM SEC_USER;'

        and: "the sql parser returns one sql statement"
        def statements = Mock(Statements)
        statements.getStatements() >> []

        sqlConsoleParser.analyseSql(_) >> statements

        when:
        sqlConsoleService.runSqlDiagnose(sqlString, DiagnoseType.SQL)

        then:
        0 * sql.rows(_)
    }

    def "executeJpql executes no sql script if there is no result of the jpql parser"() {

        given:
        def sqlString = 'select u from sec$User u'

        and: "the sql parser returns one sql statement"
        def statements = Mock(Statements)
        statements.getStatements() >> []

        sqlConsoleParser.analyseSql(_) >> statements

        when:
        sqlConsoleService.runSqlDiagnose(sqlString, DiagnoseType.JPQL)

        then:
        0 * sql.rows(_)
    }

    def "executeSql executes no jpql script if there is no result of the jpql parser"() {

        given:
        def sqlString = 'select u from sec$User u'

        and: "the sql parser returns one sql statement"
        def statements = Mock(Statements)
        statements.getStatements() >> []

        sqlConsoleParser.analyseSql(_) >> statements

        when:
        sqlConsoleService.runSqlDiagnose(sqlString, DiagnoseType.JPQL)

        then:
        0 * sql.rows(_)
    }

    def "runSqlDiagnose adds metainformation to the diagnoseExecution"() {

        given:
        def diagnoseExecution = new DiagnoseExecution()

        when:
        sqlConsoleService.runSqlDiagnose(diagnoseExecution, DiagnoseType.SQL)

        then:
        diagnoseExecution.executionTimestamp == currentDate
        diagnoseExecution.executionUser == "admin"
    }

    def "runJpqlDiagnose adds metainformation to the diagnoseExecution"() {

        given:
        def diagnoseExecution = new DiagnoseExecution()

        when:
        sqlConsoleService.runSqlDiagnose(diagnoseExecution, DiagnoseType.JPQL)

        then:
        diagnoseExecution.executionTimestamp == currentDate
        diagnoseExecution.executionUser == "admin"
    }

    def "runSqlDiagnose logs the diagnose execution"() {

        given:
        def diagnoseExecution = new DiagnoseExecution()

        when:
        sqlConsoleService.runSqlDiagnose(diagnoseExecution, DiagnoseType.SQL)

        then:
        1 * diagnoseExecutionLogService.logDiagnoseExecution(diagnoseExecution)
    }

    def "runJpqlDiagnose logs the diagnose execution"() {

        given:
        def diagnoseExecution = new DiagnoseExecution()

        when:
        sqlConsoleService.runSqlDiagnose(diagnoseExecution, DiagnoseType.JPQL)

        then:
        1 * diagnoseExecutionLogService.logDiagnoseExecution(diagnoseExecution)
    }

    def "getQueryResult with argument different from SQL or JPQL throw IllegalArgumentException"() {

        given:
        def diagnoseType = DiagnoseType.GROOVY

        when:
        sqlConsoleService.getQueryResult(diagnoseType, _ as String)

        then:
        thrown(IllegalArgumentException)
    }
}

class MockableSqlDiagnoseServiceBean extends SqlDiagnoseServiceBean {

    Sql sql

    DataSource actualDataSource

    @Override
    protected Sql createSqlConnection(DataSource dataSource) {
        actualDataSource = dataSource
        sql
    }
}