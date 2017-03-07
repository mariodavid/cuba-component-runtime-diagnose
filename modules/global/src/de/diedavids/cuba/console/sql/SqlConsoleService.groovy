package de.diedavids.cuba.console.sql

import de.diedavids.cuba.console.sql.SqlSelectResult

interface SqlConsoleService {
    String NAME = "console_SqlConsoleService";

    SqlSelectResult executeSql(String sqlString)
}