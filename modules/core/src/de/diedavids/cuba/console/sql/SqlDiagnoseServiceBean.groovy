package de.diedavids.cuba.console.sql

import com.haulmont.cuba.core.Persistence
import com.haulmont.cuba.core.global.TimeSource
import de.diedavids.cuba.console.diagnose.DiagnoseExecution
import groovy.sql.Sql
import net.sf.jsqlparser.statement.Statements
import org.springframework.stereotype.Service

import javax.inject.Inject
import javax.sql.DataSource

@Service(SqlDiagnoseService.NAME)
class SqlDiagnoseServiceBean implements SqlDiagnoseService {

    @Inject
    Persistence persistence

    @Inject
    SqlSelectResultFactory selectResultFactory

    @Inject
    SqlConsoleParser sqlConsoleParser


    @Inject
    TimeSource timeSource

    @Override
    SqlSelectResult runSqlDiagnose(String sqlString) {

        def sqlStatements = sqlConsoleParser.analyseSql(sqlString)
        def sql = createSqlConnection(persistence.dataSource)

        if (statementsAvailable(sqlStatements)) {
            def rows = sql.rows(sqlStatements.statements[0].toString())
            selectResultFactory.createFromRows(rows)
        }
    }

    @Override
    DiagnoseExecution runSqlDiagnose(DiagnoseExecution diagnoseExecution) {
        if (diagnoseExecution) {


            diagnoseExecution.executionTimestamp = timeSource.currentTimestamp()
            try {
                def sqlSelectResult = runSqlDiagnose(diagnoseExecution.diagnoseScript)
                // TODO: create CSV file with content
                diagnoseExecution.handleSuccessfulExecution(sqlSelectResult.entities[0].toString())

            }
            catch (Exception e) {
                diagnoseExecution.handleErrorExecution(e)
            }

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