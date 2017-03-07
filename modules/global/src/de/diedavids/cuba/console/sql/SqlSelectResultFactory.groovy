package de.diedavids.cuba.console.sql

import java.sql.ResultSet

interface SqlSelectResultFactory {

    public static final String NAME = 'console_SqlSelectResultFactory'

    SqlSelectResult createFromResultSet(ResultSet resultSet)

}
