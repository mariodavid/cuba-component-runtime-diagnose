package de.diedavids.cuba.runtimediagnose.sql

interface SqlSelectResultFactory {

    public static final String NAME = 'ddcrd_SqlSelectResultFactory'

    SqlSelectResult createFromRows(List<Object> rows)

}
