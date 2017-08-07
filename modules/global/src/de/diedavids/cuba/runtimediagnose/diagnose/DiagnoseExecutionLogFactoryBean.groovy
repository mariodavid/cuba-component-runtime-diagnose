package de.diedavids.cuba.runtimediagnose.diagnose

import com.haulmont.cuba.core.global.Metadata
import de.diedavids.cuba.runtimediagnose.entity.DiagnoseExecutionLog
import groovy.transform.CompileStatic
import org.springframework.stereotype.Component

import javax.inject.Inject

@CompileStatic
@Component(DiagnoseExecutionLogFactory.NAME)
class DiagnoseExecutionLogFactoryBean implements DiagnoseExecutionLogFactory {

    @Inject
    Metadata metadata

    @Override
    DiagnoseExecutionLog create(DiagnoseExecution diagnoseExecution) {
        def entityLog = metadata.create(DiagnoseExecutionLog)

        setEntityLogAttributes(diagnoseExecution, entityLog)

        entityLog
    }

    private void setEntityLogAttributes(DiagnoseExecution diagnoseExecution, DiagnoseExecutionLog entityLog) {
        entityLog.executionSuccessful = diagnoseExecution.executionSuccessful
        entityLog.executionTimestamp = diagnoseExecution.executionTimestamp
        entityLog.executionUser = diagnoseExecution.executionUser
        entityLog.diagnoseType = diagnoseExecution.manifest.diagnoseType.name()
        entityLog.executionType = diagnoseExecution.executionType.name()
    }
}
