package de.diedavids.cuba.runtimediagnose.sql

import com.haulmont.chile.core.model.MetaClass
import com.haulmont.chile.core.model.MetaProperty
import com.haulmont.cuba.security.entity.User
import groovy.sql.GroovyRowResult
import spock.lang.Specification

class SqlSelectResultFactorySpec extends Specification{

    SqlSelectResultFactoryBean delegate
    String predefinedPropName

    void setup() {
        predefinedPropName = "name"

        delegate = new SqlSelectResultFactoryBean()
    }

    def "createFromRows with GroovyRowResult argument returns SqlSelectResult"(){
        given:
        GroovyRowResult groovyRowResult = [(predefinedPropName): "Name"] as GroovyRowResult

        when:
        def result = delegate.createFromRows([groovyRowResult])

        then:
        result != null
        result.columns == [(predefinedPropName)]
        result.entities.size() > 0
    }

    def "createFromRows with Entity argument returns SqlSelectResult"() {
        given:
        MetaProperty metaProperty = Mock(MetaProperty){
            getName() >> predefinedPropName
        }

        User user = Mock(User){
            getMetaClass() >> Mock(MetaClass){
                getProperties() >> [metaProperty]
            }
        }

        when:
        def result = delegate.createFromRows([user])

        then:
        result != null
        result.columns == [(predefinedPropName)]
        result.entities.size() > 0
    }
}
