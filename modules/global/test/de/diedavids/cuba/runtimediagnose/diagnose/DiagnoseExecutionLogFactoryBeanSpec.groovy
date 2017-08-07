package de.diedavids.cuba.runtimediagnose.diagnose

import com.haulmont.cuba.core.global.Metadata
import de.diedavids.cuba.runtimediagnose.entity.DiagnoseExecutionLog
import spock.lang.Specification

class DiagnoseExecutionLogFactoryBeanSpec extends Specification {

    def "create takes the attributes from the diagnose execution and creates a log entity"() {
        given:
        def metadata = Mock(Metadata)
        metadata.create(DiagnoseExecutionLog) >> new DiagnoseExecutionLog()

        def sut = new DiagnoseExecutionLogFactoryBean(
                metadata: metadata
        )

        and:
        def diagnoseExecution = new DiagnoseExecution(
                executionSuccessful: true,
                executionTimestamp: new Date(),
                executionUser: "some user",
                manifest: new DiagnoseManifest(
                        diagnoseType: DiagnoseType.GROOVY
                ),
                executionType: DiagnoseExecutionType.CONSOLE
        )

        when:
        def entityLog = sut.create(diagnoseExecution)

        then:
        entityLog.executionSuccessful == diagnoseExecution.executionSuccessful
        entityLog.executionTimestamp == diagnoseExecution.executionTimestamp
        entityLog.executionUser == diagnoseExecution.executionUser
        entityLog.diagnoseType == "GROOVY"
        entityLog.executionType == "CONSOLE"
    }
}
