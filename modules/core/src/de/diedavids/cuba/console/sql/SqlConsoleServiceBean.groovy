package de.diedavids.cuba.console.sql

import com.haulmont.cuba.core.Persistence
import groovy.sql.Sql
import org.springframework.stereotype.Service

import javax.inject.Inject
import java.sql.SQLException

@Service(SqlConsoleService.NAME)
public class SqlConsoleServiceBean implements SqlConsoleService {

    @Inject
    Persistence persistence

    @Inject
    SqlSelectResultFactory selectResultFactory

    @Inject
    SqlConsoleParser sqlConsoleParser

    @Override
    SqlSelectResult executeSql(String sqlString) {

        def sqlStatements = sqlConsoleParser.analyseSql(sqlString)
        def sql = new Sql(persistence.dataSource)

        try {
            def rows = sql.rows(sqlStatements.statements[0].toString())
            selectResultFactory.createFromRows(rows)
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}