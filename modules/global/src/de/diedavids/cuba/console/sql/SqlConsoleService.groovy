package de.diedavids.cuba.console.sql

interface SqlConsoleService {
    String NAME = "console_SqlConsoleService";

    SqlSelectResult executeSql(String sqlString)
}