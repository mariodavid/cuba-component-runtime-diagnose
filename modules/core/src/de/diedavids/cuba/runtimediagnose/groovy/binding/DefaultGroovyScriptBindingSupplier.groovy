package de.diedavids.cuba.runtimediagnose.groovy.binding

import com.haulmont.cuba.core.Persistence
import com.haulmont.cuba.core.global.AppBeans
import com.haulmont.cuba.core.global.DataManager
import com.haulmont.cuba.core.global.Metadata
import com.haulmont.cuba.core.global.TimeSource
import groovy.sql.Sql
import groovy.util.logging.Slf4j
import org.springframework.stereotype.Component

import javax.inject.Inject

@Slf4j
@Component('ddcrd_DefaultGroovyScriptTeststepBindingSupplier')
class DefaultGroovyScriptBindingSupplier implements GroovyScriptBindingSupplier {

    @Inject
    Persistence persistence

    @Inject
    DataManager dataManager

    @Inject
    TimeSource timeSource

    @Inject
    Metadata metadata

    @Override
    Map<String, Object> getBinding() {
        [
                dataManager: dataManager,
                persistence: persistence,
                metadata   : metadata,
                bean       : beanClosure,
                getSql     : sqlClosure,
        ]
    }

    protected Closure getBeanClosure() {
        return { String name ->
            AppBeans.get(name)
        }

    }

    protected Closure getSqlClosure() {
        return { String name = null ->
            def dataSource = name ? persistence.getDataSource(name) : persistence.dataSource
            new Sql(dataSource)
        }
    }

}