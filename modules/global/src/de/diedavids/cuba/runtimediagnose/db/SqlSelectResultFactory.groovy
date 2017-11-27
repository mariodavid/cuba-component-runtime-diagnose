package de.diedavids.cuba.runtimediagnose.db

interface SqlSelectResultFactory {

    public static final String NAME = 'ddcrd_SqlSelectResultFactory'

    DbQueryResult createFromRows(List<Object> rows)

}
