package de.diedavids.cuba.console.sql

interface SqlSelectResultFactory {

    public static final String NAME = 'console_SqlSelectResultFactory'

    SqlSelectResult createFromRows(List<Map> rows)

}
