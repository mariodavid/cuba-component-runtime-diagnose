package de.diedavids.cuba.runtimediagnose.sql

import com.haulmont.cuba.core.entity.KeyValueEntity
import spock.lang.Specification

class SqlSelectResultSpec extends Specification {


    DbQueryResult sqlSelectResult

    def setup() {

        sqlSelectResult = new DbQueryResult(

        )
    }

    def "addColumn adds an entry to the columns"() {
        when:
        sqlSelectResult.addColumn('column1')
        then:
        sqlSelectResult.columns.size() == 1
    }

    def "addEntity adds an entry to the entities"() {
        when:
        sqlSelectResult.addEntity(new KeyValueEntity())
        then:
        sqlSelectResult.entities.size() == 1
    }
}
