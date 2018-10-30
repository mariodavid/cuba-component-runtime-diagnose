package de.diedavids.cuba.runtimediagnose

import com.haulmont.cuba.core.global.BeanLocator
import com.haulmont.cuba.core.global.Messages
import com.haulmont.cuba.core.global.Metadata
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
    @Shared
    BeanLocator beanLocator
    @Shared
    Metadata metadata

    def setup() {
        applicationContext = Mock()
        beanLocator = Mock()

        applicationContext.getBean(BeanLocator.NAME,BeanLocator) >> beanLocator

        initBeans()

        AppContext.Internals.applicationContext = applicationContext
    }

    def cleanup() {
        AppContext.Internals.applicationContext = null
    }

    private void initBeans() {
        def allBeans = getBeans()
        addDefaultBeans(allBeans)
        mockGetBean(allBeans)
    }

    private void mockGetBean(Map<Class, Object> beans) {
        for (Map.Entry<Class, Object> bean : beans) {
            Class clazz = bean.key
            Object instance = bean.value

            beanLocator.get(clazz) >> instance
            String name = clazz.NAME
            if (name) {
                beanLocator.get(name) >> instance
                beanLocator.get(name, clazz) >> instance
            }
        }
    }

    protected void addDefaultBeans(Map<Class, Object> beansFromSpec) {

        messages = Mock()
        sessionSource = Mock()
        uuidSource = Mock()
        metadata = Mock()
        uuidSource.createUuid() >> UUID.randomUUID()

        beansFromSpec.putAll([
            (Messages)         : messages,
            (UserSessionSource): sessionSource,
            (UuidSource)       : uuidSource,
            (Metadata)         : metadata,
        ])
    }

    Map<Class, Object> getBeans() {
        [:]
    }

}