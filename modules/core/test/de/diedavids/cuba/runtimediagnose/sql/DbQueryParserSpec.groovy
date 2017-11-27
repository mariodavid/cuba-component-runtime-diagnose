package de.diedavids.cuba.runtimediagnose.sql

import com.haulmont.cuba.core.global.Messages
import de.diedavids.cuba.runtimediagnose.RuntimeDiagnoseConfiguration
import de.diedavids.cuba.runtimediagnose.SqlConsoleSecurityException
import de.diedavids.cuba.runtimediagnose.diagnose.DiagnoseType
import net.sf.jsqlparser.statement.SetStatement
import net.sf.jsqlparser.statement.Statements
import net.sf.jsqlparser.statement.drop.Drop
import net.sf.jsqlparser.statement.insert.Insert
import spock.lang.Specification

class DbQueryParserSpec extends Specification {


    DbQueryParser sut
    RuntimeDiagnoseConfiguration runtimeDiagnoseConfiguration
    Messages messages

    def setup() {

        runtimeDiagnoseConfiguration = Mock(RuntimeDiagnoseConfiguration)
        messages = Mock(Messages)
        sut = new DbQueryParser(
                configuration: runtimeDiagnoseConfiguration,
                messages: messages
        )
    }

    def "analyseQueryString throws a security exception if the SQL statement is data manipulation and the config does not allow data manipulation"() {

        given:
        runtimeDiagnoseConfiguration.getSqlAllowDataManipulation() >> false

        and:
        messages.getMessage(DbQueryParser, 'dataManipulationNotAllowed') >> 'nope for data manipulation'

        when:
        sut.analyseQueryString("INSERT INTO SEC_USER(ID) VALUES ('e54f6d8d-29b1-439e-846c-b6180495c066');", DiagnoseType.SQL)

        then:
        def ex = thrown SqlConsoleSecurityException

        and:
        ex.message == 'nope for data manipulation'
    }

    def "analyseQueryString creates a insert statement if the SQL statement is data manipulation and the config does allow data manipulation"() {

        given:
        runtimeDiagnoseConfiguration.getSqlAllowDataManipulation() >> true

        when:
        Statements result = sut.analyseQueryString("INSERT INTO SEC_USER (ID) VALUES ('e54f6d8d-29b1-439e-846c-b6180495c066')", DiagnoseType.SQL)
        def insertStatement = result.statements[0]

        then:
        result.statements.size() == 1

        and:
        insertStatement instanceof Insert
        insertStatement.toString() == "INSERT INTO SEC_USER (ID) VALUES ('e54f6d8d-29b1-439e-846c-b6180495c066')"
    }



    def "analyseQueryString throws a security exception if the SQL statement is schema manipulation and the config does not allow schema manipulation"() {

        given:
        runtimeDiagnoseConfiguration.getSqlAllowSchemaManipulation() >> false

        and:
        messages.getMessage(DbQueryParser, 'schemaManipulationNotAllowed') >> 'nope for schema manipulation'

        when:
        sut.analyseQueryString("DROP TABLE SEC_USER", DiagnoseType.SQL)

        then:
        def ex = thrown SqlConsoleSecurityException

        and:
        ex.message == 'nope for schema manipulation'
    }

    def "analyseQueryString creates a drop statement if the SQL statement is schema manipulation and the config does allow schema manipulation"() {

        given:
        runtimeDiagnoseConfiguration.getSqlAllowSchemaManipulation() >> true

        when:
        Statements result = sut.analyseQueryString("DROP TABLE SEC_USER", DiagnoseType.SQL)
        def dropStatement = result.statements[0]

        then:
        result.statements.size() == 1

        and:
        dropStatement instanceof Drop
        dropStatement.toString() == "DROP TABLE SEC_USER"
    }



    def "analyseQueryString throws a security exception if the SQL statement is an execute operation and the config does not allow execution operations"() {

        given:
        runtimeDiagnoseConfiguration.getSqlAllowExecuteOperations() >> false

        and:
        messages.getMessage(DbQueryParser, 'executeOperationNotAllowed') >> 'nope for execute operations'

        when:
        sut.analyseQueryString("EXECUTE MY_STORED_PROCEDURE", DiagnoseType.SQL)

        then:
        def ex = thrown SqlConsoleSecurityException

        and:
        ex.message == 'nope for execute operations'
    }

    def "analyseQueryString creates a execute statement if the SQL statement is an execute operation and the config does allow execute operations"() {

        given:
        runtimeDiagnoseConfiguration.getSqlAllowExecuteOperations() >> true

        when:
        Statements result = sut.analyseQueryString("SET OPTION = VALUE", DiagnoseType.SQL)
        def dropStatement = result.statements[0]

        then:
        result.statements.size() == 1

        and:
        dropStatement instanceof SetStatement
        dropStatement.toString() == "SET OPTION = VALUE"
    }
}
