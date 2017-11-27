package de.diedavids.cuba.runtimediagnose.db

import com.haulmont.chile.core.model.MetaClass
import com.haulmont.chile.core.model.MetaProperty
import com.haulmont.cuba.security.entity.User
import groovy.sql.GroovyRowResult
import spock.lang.Specification

class SqlSelectResultFactorySpec extends Specification{

    SqlSelectResultFactoryBean delegate
    String predefinedPropName
    MetaProperty predefinedMetaProperty

    void setup() {
        predefinedPropName = "name"
        predefinedMetaProperty = Mock(MetaProperty){
            getName() >> predefinedPropName
            getJavaType() >> String.class
        }

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
        User user = Mock(User) {
            getMetaClass() >> Mock(MetaClass) {
                getProperties() >> [predefinedMetaProperty]
            }
        }

        when:
        def result = delegate.createFromRows([user])

        then:
        result != null
        result.columns == [(predefinedPropName)]
        result.entities.size() > 0
    }

    def "sqlSelectResult columns not contains collections"() {
        given:
        MetaProperty collectionMetaProperty = Mock(MetaProperty) {
            getName() >> predefinedPropName
            getJavaType() >> Collection.class
        }

        User user = Mock(User) {
            getMetaClass() >> Mock(MetaClass) {
                getProperties() >> [predefinedMetaProperty, collectionMetaProperty]
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
