package de.diedavids.cuba.console.sql

interface SqlSelectResultFactory {

    public static final String NAME = 'ddrd_SqlSelectResultFactory'

    SqlSelectResult createFromRows(List<Map> rows)

}
