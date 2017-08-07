package de.diedavids.cuba.runtimediagnose.diagnose

import com.haulmont.cuba.core.app.FileStorageAPI
import com.haulmont.cuba.core.entity.FileDescriptor
import com.haulmont.cuba.core.global.CommitContext
import com.haulmont.cuba.core.global.DataManager
import com.haulmont.cuba.core.global.Metadata
import de.diedavids.cuba.runtimediagnose.RuntimeDiagnoseConfiguration
import de.diedavids.cuba.runtimediagnose.entity.DiagnoseExecutionLog
import org.springframework.stereotype.Service

import javax.inject.Inject
import java.text.SimpleDateFormat

@Service(DiagnoseExecutionLogService.NAME)
class DiagnoseExecutionLogServiceBean implements DiagnoseExecutionLogService {

    @Inject
    DataManager dataManager

    @Inject
    DiagnoseExecutionLogFactory diagnoseExecutionLogFactory


    @Inject
    RuntimeDiagnoseConfiguration runtimeDiagnoseConfiguration

    @Inject
    DiagnoseExecutionFactory diagnoseExecutionFactory

    @Inject
    FileStorageAPI fileStorageAPI

    @Inject
    Metadata metadata

    @Override
    void logDiagnoseExecution(DiagnoseExecution diagnoseExecution) {

        if (runtimeDiagnoseConfiguration.logEnabled) {
            DiagnoseExecutionLog diagnoseExecutionLog = diagnoseExecutionLogFactory.create(diagnoseExecution)
            def commitContext = new CommitContext()
            commitContext.addInstanceToCommit(diagnoseExecutionLog)

            if (runtimeDiagnoseConfiguration.logDiagnoseDetails) {
                byte[] executionResultZipFile = diagnoseExecutionFactory.createExecutionResultFromDiagnoseExecution(diagnoseExecution)
                FileDescriptor fileDescriptor = createFileDescriptor(diagnoseExecution, executionResultZipFile)

                diagnoseExecutionLog.executionResultFile = fileDescriptor
                commitContext.addInstanceToCommit(fileDescriptor)
                dataManager.commit(commitContext)

                fileStorageAPI.saveFile(fileDescriptor, executionResultZipFile)
            }
            else {
                dataManager.commit(commitContext)
            }
        }
    }

    @SuppressWarnings('SimpleDateFormatMissingLocale')
    private FileDescriptor createFileDescriptor(DiagnoseExecution diagnoseExecution, byte[] executionResultZipFile) {

        def fileDescriptor = metadata.create(FileDescriptor)

        def filenameTimestampString = new SimpleDateFormat('yyyyMMddHHmmss').format(diagnoseExecution.executionTimestamp)

        fileDescriptor.name = "Diagnose-result-${filenameTimestampString}.zip"
        fileDescriptor.extension = 'zip'
        fileDescriptor.size = (long) executionResultZipFile.length
        fileDescriptor.createDate = new Date()

        fileDescriptor
    }
}