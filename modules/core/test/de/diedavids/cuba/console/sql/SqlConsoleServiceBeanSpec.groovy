package de.diedavids.cuba.console.sql

import com.haulmont.cuba.core.Persistence
import groovy.sql.Sql
import net.sf.jsqlparser.statement.Statement
import net.sf.jsqlparser.statement.Statements
import spock.lang.Specification

import javax.sql.DataSource
import java.sql.SQLException

class SqlConsoleServiceBeanSpec extends Specification {


    MockableSqlConsoleServiceBean sqlConsoleService

    SqlConsoleParser sqlConsoleParser
    SqlSelectResultFactory selectResultFactory
    Persistence persistence
    DataSource dataSource
    Sql sql

    def setup() {
        sqlConsoleParser = Mock(SqlConsoleParser)
        selectResultFactory = Mock(SqlSelectResultFactory)

        persistence = Mock(Persistence)
        sql = Mock(Sql)
        sqlConsoleService = new MockableSqlConsoleServiceBean(
            sqlConsoleParser: sqlConsoleParser,
                selectResultFactory: selectResultFactory,
                persistence: persistence,
                sql: this.sql
        )

        dataSource = Mock(DataSource)
        persistence.getDataSource() >> dataSource

    }

    def "executeSql uses SqlConsoleParser to analyse the sql statements"() {

        given:
        def sqlString = 'SELECT * FROM SEC_USER;'
        when:
        sqlConsoleService.executeSql(sqlString)
        then:
        1 * sqlConsoleParser.analyseSql(sqlString)
    }

    def "executeSql creates a SQL object with the datasource from persistence"() {

        when:
        sqlConsoleService.executeSql(null)
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

        when:
        sqlConsoleService.executeSql(sqlString)

        then:
        1 * sql.rows(sqlString)
    }

    def "executeSql executes no sql script if there is no result of the sql parser"() {

        given:
        def sqlString = 'SELECT * FROM SEC_USER;'

        and: "the sql parser returns one sql statement"
        def statements = Mock(Statements)
        statements.getStatements() >> []

        sqlConsoleParser.analyseSql(_) >> statements

        when:
        sqlConsoleService.executeSql(sqlString)

        then:
        0 * sql.rows(_)
    }


    def "executeSql will delegate a SQLException to a RuntimeException to push it to the client"() {

        given:
        def sqlString = 'SELECT * FROM SEC_USER;'

        and: "the sql parser returns one sql statement"
        def statements = Mock(Statements)
        def sqlStatement = Mock(Statement)
        sqlStatement.toString() >> sqlString
        statements.getStatements() >> [sqlStatement]

        sqlConsoleParser.analyseSql(_) >> statements

        and:
        def mySqlException = new SQLException("the SQL is bad")
        sql.rows(sqlString) >> { throw mySqlException }

        when:
        sqlConsoleService.executeSql(sqlString)

        then: "the runtime exception is a wrapped SQL exception"
        def runtimeException = thrown RuntimeException
        runtimeException.cause == mySqlException
    }


}

class MockableSqlConsoleServiceBean extends SqlConsoleServiceBean {

    Sql sql

    DataSource actualDataSource

    @Override
    protected Sql createSqlConnection(DataSource dataSource) {
        actualDataSource = dataSource
        sql
    }
}