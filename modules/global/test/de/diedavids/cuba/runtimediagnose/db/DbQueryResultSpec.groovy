package de.diedavids.cuba.runtimediagnose.db

import com.haulmont.cuba.core.entity.KeyValueEntity
import spock.lang.Specification

class DbQueryResultSpec extends Specification {

    DbQueryResult sut

    def setup() {
        sut = new DbQueryResult()
    }

    def "addColumn adds an entry to the columns"() {
        when:
        sut.addColumn('column1')
        then:
        sut.columns.size() == 1
    }

    def "addEntity adds an entry to the entities"() {
        when:
        sut.addEntity(new KeyValueEntity())
        then:
        sut.entities.size() == 1
    }

    def "toCSV renders all entries as CSV list"() {

        given:
        sut.addColumn("attribute1")
        sut.addColumn("attribute2")
        sut.addEntity(kvEntity(attribute1: "value-1.1", attribute2: "value-1.2"))
        sut.addEntity(kvEntity(attribute1: "value-2.1", attribute2: "value-2.2"))

        expect:
        sut.toCSV() == '''"attribute1","attribute2"
"value-1.1","value-1.2"
"value-2.1","value-2.2"'''
    }
    def 'toCSV renders not existing entries as ""'() {

        given:
        sut.addColumn("attribute1")
        sut.addColumn("attribute2")
        sut.addEntity(kvEntity(attribute1: "value-1.1", attribute2: null))
        sut.addEntity(kvEntity(attribute2: "value-2.2"))

        expect:
        sut.toCSV() == '''"attribute1","attribute2"
"value-1.1",""
"","value-2.2"'''
    }

    private KeyValueEntity kvEntity(LinkedHashMap<String, String> attributes) {
        def entity1 = new KeyValueEntity()

        attributes.each { k, v ->
            entity1.setValue(k, v)
        }

        entity1
    }
}
