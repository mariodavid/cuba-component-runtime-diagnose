package de.diedavids.cuba.runtimediagnose.sql

import com.haulmont.cuba.core.global.AppBeans
import com.haulmont.cuba.core.global.Messages
import com.haulmont.cuba.core.global.QueryParserAstBased
import com.haulmont.cuba.core.sys.jpql.DomainModel
import com.haulmont.cuba.core.sys.jpql.DomainModelBuilder
import de.diedavids.cuba.runtimediagnose.RuntimeDiagnoseConfiguration
import de.diedavids.cuba.runtimediagnose.SqlConsoleSecurityException
import de.diedavids.cuba.runtimediagnose.diagnose.DiagnoseType
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
class DbQueryParser {

    @Inject
    RuntimeDiagnoseConfiguration configuration

    @Inject
    Messages messages

    protected static final EXECUTE_OPERATIONS = [
        Execute, SetStatement
    ]
    protected static final DATA_MANIPULATION_OPERATIONS = [
        Insert, Update, Delete, Merge, Replace, Truncate
    ]
    protected static final SCHEMA_MANIPULATION_OPERATIONS = [
        Drop, CreateTable, CreateView, Alter, AlterView, CreateIndex
    ]

    Statements analyseQueryString(String queryString, DiagnoseType diagnoseType) {

        Statements statements = CCJSqlParserUtil.parseStatements(queryString)

        if (!configuration.sqlAllowDataManipulation && containsDataManipulation(statements)) {
            throw new SqlConsoleSecurityException(messages.getMessage(getClass(), 'dataManipulationNotAllowed'))
        }

        if (!configuration.sqlAllowSchemaManipulation && containsSchemaManipulation(statements)) {
            throw new SqlConsoleSecurityException(messages.getMessage(getClass(), 'schemaManipulationNotAllowed'))
        }

        if (!configuration.sqlAllowExecuteOperations && containsExecuteOperations(statements)) {
            throw new SqlConsoleSecurityException(messages.getMessage(getClass(), 'executeOperationNotAllowed'))
        }


        if (DiagnoseType.JPQL == diagnoseType) {
            analyseJpql(queryString)
        }

        statements
    }

    void analyseJpql(String queryString) {
        QueryParserAstBased parser = new QueryParserAstBased(ScriptManagerUtilsHolder.domainModelInstance, queryString)
        parser.queryPaths
    }

    private static class ScriptManagerUtilsHolder {
        private static class ScriptManagerUtilLazyHolder {
            public static final DomainModel DOMAIN_MODEL_INSTANCE = AppBeans.get(DomainModelBuilder).produce()
        }
        static DomainModel getDomainModelInstance() {
            ScriptManagerUtilLazyHolder.DOMAIN_MODEL_INSTANCE
        }
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