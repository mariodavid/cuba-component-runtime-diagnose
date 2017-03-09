package de.diedavids.cuba.console.sql

import com.haulmont.cuba.core.Persistence
import de.diedavids.cuba.console.ConsoleConfiguration
import de.diedavids.cuba.console.SqlConsoleSecurityException
import net.sf.jsqlparser.parser.CCJSqlParserUtil
import net.sf.jsqlparser.statement.SetStatement
import net.sf.jsqlparser.statement.Statements
import net.sf.jsqlparser.statement.alter.Alter
import net.sf.jsqlparser.statement.create.index.CreateIndex
import net.sf.jsqlparser.statement.create.table.CreateTable
import net.sf.jsqlparser.statement.create.view.AlterView
import net.sf.jsqlparser.statement.create.view.CreateView
import net.sf.jsqlparser.statement.delete.Delete
import net.sf.jsqlparser.statement.drop.Drop
import net.sf.jsqlparser.statement.execute.Execute
import net.sf.jsqlparser.statement.insert.Insert
import net.sf.jsqlparser.statement.merge.Merge
import net.sf.jsqlparser.statement.replace.Replace
import net.sf.jsqlparser.statement.truncate.Truncate
import net.sf.jsqlparser.statement.update.Update
import org.springframework.stereotype.Component

import javax.inject.Inject

@Component
class SqlConsoleParser {

    @Inject
    ConsoleConfiguration configuration

    protected static final EXECUTE_OPERATIONS = [
        Execute, SetStatement
    ]
    protected static final DATA_MANIPULATION_OPERATIONS = [
        Insert, Update, Delete, Merge, Replace, Truncate
    ]
    protected static final SCHEMA_MANIPULATION_OPERATIONS = [
        Drop, CreateTable, CreateView, Alter, AlterView, CreateIndex
    ]

    Statements analyseSql(String sqlString) {

        Statements statements = CCJSqlParserUtil.parseStatements(sqlString);

        if (!configuration.sqlAllowDataManipulation && containsDataManipulation(statements)) {
            throw new SqlConsoleSecurityException("Data manipulation SQL statements are not allowed")
        }

        if (!configuration.sqlAllowSchemaManipulation && containsSchemaManipulation(statements)) {
            throw new SqlConsoleSecurityException("Schema manipulation SQL statements are not allowed")
        }

        if (!configuration.sqlAllowExecuteOperations && containsExecuteOperations(statements)) {
            throw new SqlConsoleSecurityException("SQL Execute operations are not allowed")
        }

        statements
    }

    boolean containsDataManipulation(Statements statements) {
        containsIllegalOperation(statements, DATA_MANIPULATION_OPERATIONS)
    }

    boolean containsSchemaManipulation(Statements statements) {
        containsIllegalOperation(statements, SCHEMA_MANIPULATION_OPERATIONS)
    }

    boolean containsExecuteOperations(Statements statements) {
        containsIllegalOperation(statements, EXECUTE_OPERATIONS)
    }

    protected boolean containsIllegalOperation(Statements statements, dataManipulationOperations) {

        def containsIllegalOperation = false
        statements.statements.each { statement ->
            dataManipulationOperations.each { operationClass ->
                if (operationClass.isAssignableFrom(statement.class)) {
                    containsIllegalOperation = true
                }
            }
        }

        containsIllegalOperation
    }
}