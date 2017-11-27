package de.diedavids.cuba.runtimediagnose.db

import com.haulmont.chile.core.model.MetaClass
import com.haulmont.cuba.core.entity.Entity
import com.haulmont.cuba.core.entity.KeyValueEntity
import com.haulmont.cuba.core.global.DatatypeFormatter
import org.springframework.stereotype.Component

import javax.inject.Inject
import java.sql.Timestamp

@Component(SqlSelectResultFactory.NAME)
class SqlSelectResultFactoryBean implements SqlSelectResultFactory {

    @Inject
    DatatypeFormatter datatypeFormatter

    @Override
    DbQueryResult createFromRows(List<Object> rows) {
        def result = new DbQueryResult()
        def queryValue = rows[0]

        if (queryValue instanceof Entity) {
            MetaClass queryValueMetaClass = queryValue.metaClass
            for (def prop : queryValueMetaClass.properties) {
                if (!Collection.isAssignableFrom(prop.javaType)) {
                    result.addColumn(prop.name)
                }
            }

            rows.each { result.addEntity(createKeyValueEntity(it.properties)) }
        } else if (queryValue instanceof Map) {
            ((Map) queryValue).keySet().each { result.addColumn(it.toString()) }
            rows.each { result.addEntity(createKeyValueEntity((Map) it)) }
        }

        result
    }

    private KeyValueEntity createKeyValueEntity(Map<String, Object> content) {
        def kv = new KeyValueEntity()
        content.each { k, v ->
            def displayedValue = v.toString()
            if (v instanceof Timestamp) {
                displayedValue = datatypeFormatter.formatDateTime(new Date(v.time))
            } else if (v instanceof Entity) {
                displayedValue = v.id.toString()
            }
            kv.setValue(k, displayedValue)
        }
        kv
    }
}
