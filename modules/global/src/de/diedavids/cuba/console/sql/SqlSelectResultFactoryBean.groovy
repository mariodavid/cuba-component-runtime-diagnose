package de.diedavids.cuba.console.sql

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
    SqlSelectResult createFromRows(List<Map> rows) {

        def result = new SqlSelectResult()

        rows[0].keySet().each {result.addColumn(it.toString())}
        rows.each {result.addEntity(createKeyValueEntity(it))}

        result
    }

    private KeyValueEntity createKeyValueEntity(Map<String, Object> content) {

        def kv = new KeyValueEntity()
        content.each {k,v ->
            def displayedValue = v.toString()
            if (v instanceof Timestamp) {
                displayedValue = datatypeFormatter.formatDateTime(new Date(v.getTime()))
            }
            kv.setValue(k, displayedValue)
        }
        kv
    }
}
