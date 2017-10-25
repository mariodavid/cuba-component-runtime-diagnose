package de.diedavids.cuba.runtimediagnose.web.screens

import com.haulmont.cuba.core.global.Messages
import com.haulmont.cuba.core.global.UserSessionSource
import com.haulmont.cuba.core.global.UuidSource
import com.haulmont.cuba.core.sys.AppContext
import org.springframework.context.ApplicationContext
import spock.lang.Shared
import spock.lang.Specification

class SpecificationWithApplicationContext extends Specification {

    @Shared
    ApplicationContext applicationContext

    @Shared
    Messages messages
    @Shared
    UserSessionSource sessionSource
    @Shared
    UuidSource uuidSource

    def setup() {
        applicationContext = Mock()
        initBeans()

        AppContext.Internals.setApplicationContext(applicationContext)
    }

    def cleanup() {
        AppContext.Internals.setApplicationContext(null)
    }

    private void initBeans() {
        Map<Class, Object> beansFromSpec = getBeans()
        addDefaultBeans(beansFromSpec)
        mockGetBean(beansFromSpec)
    }

    private void mockGetBean(Map<Class, Object> beans) {
        for (Map.Entry<Class, Object> bean : beans) {
            Class clazz = bean.key
            Object instance = bean.value

            applicationContext.getBean(clazz) >> instance

            try {
                String name = clazz.NAME
                if (name) {
                    applicationContext.getBean(name) >> instance
                    applicationContext.getBean(name, clazz) >> instance
                }
            }
            catch(MissingPropertyException e) {
                e.printStackTrace()
            }
        }
    }

    protected void addDefaultBeans(Map<Class, Object> beansFromSpec) {
        messages = Mock()
        sessionSource = Mock()
        uuidSource = Mock()
        uuidSource.createUuid() >> UUID.randomUUID()

        beansFromSpec.putAll([(Messages): messages, (UserSessionSource): sessionSource, (UuidSource): uuidSource])
    }

    Map<Class, Object> getBeans() {
        [:]
    }

}
