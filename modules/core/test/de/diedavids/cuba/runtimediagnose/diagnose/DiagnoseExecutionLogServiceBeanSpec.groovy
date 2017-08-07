package de.diedavids.cuba.runtimediagnose.diagnose

import com.haulmont.cuba.core.app.FileStorageAPI
import com.haulmont.cuba.core.entity.FileDescriptor
import com.haulmont.cuba.core.global.CommitContext
import com.haulmont.cuba.core.global.DataManager
import com.haulmont.cuba.core.global.Metadata
import de.diedavids.cuba.runtimediagnose.RuntimeDiagnoseConfiguration
import de.diedavids.cuba.runtimediagnose.entity.DiagnoseExecutionLog
import spock.lang.Specification

class DiagnoseExecutionLogServiceBeanSpec extends Specification {


    DiagnoseExecutionLogServiceBean sut
    DataManager dataManager
    DiagnoseExecutionLogFactory diagnoseExecutionLogFactory
    RuntimeDiagnoseConfiguration config
    DiagnoseExecutionFactory diagnoseExecutionFactory
    private Metadata metadata
    private FileStorageAPI fileStorageAPI

    def setup() {

        dataManager = Mock(DataManager)
        diagnoseExecutionLogFactory = Mock(DiagnoseExecutionLogFactory)

        config = Mock(RuntimeDiagnoseConfiguration)
        diagnoseExecutionFactory = Mock(DiagnoseExecutionFactory)
        metadata = Mock(Metadata)
        fileStorageAPI = Mock(FileStorageAPI)
        sut = new DiagnoseExecutionLogServiceBean(
                dataManager: dataManager,
                diagnoseExecutionLogFactory: diagnoseExecutionLogFactory,
                runtimeDiagnoseConfiguration: config,
                diagnoseExecutionFactory: diagnoseExecutionFactory,
                metadata: metadata,
                fileStorageAPI: fileStorageAPI
        )
    }


    def "logDiagnoseExecution uses the LogFactory to create an Log entity"() {

        given:
        def diagnoseExecution = new DiagnoseExecution()

        and:
        config.logEnabled >> true

        when:
        sut.logDiagnoseExecution(diagnoseExecution)

        then:
        1 * diagnoseExecutionLogFactory.create(diagnoseExecution)
    }

    def "logDiagnoseExecution creates and persists a record of the RuntimeDiagnoseExecutionLog"() {

        given:
        def diagnoseExecution = new DiagnoseExecution()

        and:
        config.logEnabled >> true

        and:
        def logEntity = new DiagnoseExecutionLog()
        diagnoseExecutionLogFactory.create(diagnoseExecution) >> logEntity

        when:
        sut.logDiagnoseExecution(diagnoseExecution)

        then:
        1 * dataManager.commit({
            it instanceof CommitContext &&
                    it.commitInstances.contains(logEntity)
        })
    }

    def "logDiagnoseExecution does not persist a log entity if disabled via configuration"() {

        given:
        def diagnoseExecution = new DiagnoseExecution()

        and:
        config.logEnabled >> false


        when:
        sut.logDiagnoseExecution(diagnoseExecution)

        then:
        0 * diagnoseExecutionLogFactory.create(_)
        0 * dataManager.commit(_)
    }

    def "logDiagnoseExecution creates a zip file through the diagnoseExecutionFactory"() {

        given:
        def diagnoseExecution = new DiagnoseExecution(
                executionTimestamp: new Date()
        )

        and:
        config.logEnabled >> true

        and:
        config.logDiagnoseDetails >> true

        and:
        def fileDescriptor = new FileDescriptor()
        metadata.create(FileDescriptor) >> fileDescriptor

        and:
        def logEntity = new DiagnoseExecutionLog()
        diagnoseExecutionLogFactory.create(diagnoseExecution) >> logEntity

        and:
        def zipBytes = "hello".bytes

        when:
        sut.logDiagnoseExecution(diagnoseExecution)

        then:
        1 * diagnoseExecutionFactory.createExecutionResultFromDiagnoseExecution(diagnoseExecution) >> zipBytes
    }

    def "logDiagnoseExecution adds the file descriptor to the commit context"() {

        given:
        def diagnoseExecution = new DiagnoseExecution(
                executionTimestamp: new Date()
        )

        and:
        config.logEnabled >> true
        config.logDiagnoseDetails >> true

        and:
        def zipBytes = "hello".bytes
        diagnoseExecutionFactory.createExecutionResultFromDiagnoseExecution(diagnoseExecution) >> zipBytes

        and:
        def fileDescriptor = new FileDescriptor()
        metadata.create(FileDescriptor) >> fileDescriptor

        and:
        def logEntity = new DiagnoseExecutionLog()
        diagnoseExecutionLogFactory.create(diagnoseExecution) >> logEntity

        when:
        sut.logDiagnoseExecution(diagnoseExecution)

        then:
        1 * dataManager.commit({
            it instanceof CommitContext &&
                    it.commitInstances.contains(fileDescriptor)
        })

        1 * fileStorageAPI.saveFile(fileDescriptor, zipBytes)
    }

    def "logDiagnoseExecution does not create a FileDescriptor when the configuration for creating a file is disabled"() {

        given:
        def diagnoseExecution = new DiagnoseExecution()

        and:
        config.logEnabled >> true

        and:
        config.logDiagnoseDetails >> false

        when:
        sut.logDiagnoseExecution(diagnoseExecution)

        then:
        0 * diagnoseExecutionFactory.createExecutionResultFromDiagnoseExecution(diagnoseExecution)
    }
}
