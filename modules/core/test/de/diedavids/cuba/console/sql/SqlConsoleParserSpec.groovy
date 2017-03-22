package de.diedavids.cuba.console.sql

import com.haulmont.cuba.core.global.Messages
import de.diedavids.cuba.console.ConsoleConfiguration
import de.diedavids.cuba.console.SqlConsoleSecurityException
import net.sf.jsqlparser.statement.SetStatement
import net.sf.jsqlparser.statement.Statements
import net.sf.jsqlparser.statement.drop.Drop
import net.sf.jsqlparser.statement.execute.Execute
import net.sf.jsqlparser.statement.insert.Insert
import spock.lang.Specification

class SqlConsoleParserSpec extends Specification {


    SqlConsoleParser sut
    ConsoleConfiguration consoleConfiguration
    Messages messages

    def setup() {

        consoleConfiguration = Mock(ConsoleConfiguration)
        messages = Mock(Messages)
        sut = new SqlConsoleParser(
                configuration: consoleConfiguration,
                messages: messages
        )
    }

    def "analyseSql throws a security exception if the SQL statement is data manipulation and the config does not allow data manipulation"() {

        given:
        consoleConfiguration.getSqlAllowDataManipulation() >> false

        and:
        messages.getMessage(SqlConsoleParser, 'dataManipulationNotAllowed') >> 'nope for data manipulation'

        when:
        sut.analyseSql("INSERT INTO SEC_USER(ID) VALUES ('e54f6d8d-29b1-439e-846c-b6180495c066');")

        then:
        def ex = thrown SqlConsoleSecurityException

        and:
        ex.message == 'nope for data manipulation'
    }

    def "analyseSql creates a insert statement if the SQL statement is data manipulation and the config does allow data manipulation"() {

        given:
        consoleConfiguration.getSqlAllowDataManipulation() >> true

        when:
        Statements result = sut.analyseSql("INSERT INTO SEC_USER (ID) VALUES ('e54f6d8d-29b1-439e-846c-b6180495c066')")
        def insertStatement = result.statements[0]

        then:
        result.statements.size() == 1

        and:
        insertStatement instanceof Insert
        insertStatement.toString() == "INSERT INTO SEC_USER (ID) VALUES ('e54f6d8d-29b1-439e-846c-b6180495c066')"
    }



    def "analyseSql throws a security exception if the SQL statement is schema manipulation and the config does not allow schema manipulation"() {

        given:
        consoleConfiguration.getSqlAllowSchemaManipulation() >> false

        and:
        messages.getMessage(SqlConsoleParser, 'schemaManipulationNotAllowed') >> 'nope for schema manipulation'

        when:
        sut.analyseSql("DROP TABLE SEC_USER")

        then:
        def ex = thrown SqlConsoleSecurityException

        and:
        ex.message == 'nope for schema manipulation'
    }

    def "analyseSql creates a drop statement if the SQL statement is schema manipulation and the config does allow schema manipulation"() {

        given:
        consoleConfiguration.getSqlAllowSchemaManipulation() >> true

        when:
        Statements result = sut.analyseSql("DROP TABLE SEC_USER")
        def dropStatement = result.statements[0]

        then:
        result.statements.size() == 1

        and:
        dropStatement instanceof Drop
        dropStatement.toString() == "DROP TABLE SEC_USER"
    }



    def "analyseSql throws a security exception if the SQL statement is an execute operation and the config does not allow execution operations"() {

        given:
        consoleConfiguration.getSqlAllowExecuteOperations() >> false

        and:
        messages.getMessage(SqlConsoleParser, 'executeOperationNotAllowed') >> 'nope for execute operations'

        when:
        sut.analyseSql("EXECUTE MY_STORED_PROCEDURE")

        then:
        def ex = thrown SqlConsoleSecurityException

        and:
        ex.message == 'nope for execute operations'
    }

    def "analyseSql creates a execute statement if the SQL statement is an execute operation and the config does allow execute operations"() {

        given:
        consoleConfiguration.getSqlAllowExecuteOperations() >> true

        when:
        Statements result = sut.analyseSql("SET OPTION = VALUE")
        def dropStatement = result.statements[0]

        then:
        result.statements.size() == 1

        and:
        dropStatement instanceof SetStatement
        dropStatement.toString() == "SET OPTION = VALUE"
    }
}
