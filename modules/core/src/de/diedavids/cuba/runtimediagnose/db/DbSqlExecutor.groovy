package de.diedavids.cuba.runtimediagnose.db

import groovy.sql.Sql
import net.sf.jsqlparser.statement.Statement
import org.springframework.stereotype.Component

import javax.inject.Inject

@Component
class DbSqlExecutor {

    @Inject
    DbQueryParser dbQueryParser

    @Inject
    SqlSelectResultFactory selectResultFactory


    DbQueryResult executeStatement(Sql sql, Statement sqlStatement) {

        def rows = []

        def queryString = sqlStatement.toString()

        if (dbQueryParser.isSelect(sqlStatement)) {
            rows = sql.rows(queryString)
        }
        else if (dbQueryParser.isDataManipulation(sqlStatement)) {
            sql.executeUpdate(queryString)
        }
        else {
            rows = sql.execute(queryString)
        }
        selectResultFactory.createFromRows(rows)
    }

}