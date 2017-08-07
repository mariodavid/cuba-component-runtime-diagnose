package de.diedavids.cuba.runtimediagnose.web.diagnoseexecutionlog

import com.haulmont.cuba.core.app.FileStorageService
import com.haulmont.cuba.gui.components.AbstractLookup
import com.haulmont.cuba.gui.components.GroupTable
import de.diedavids.cuba.runtimediagnose.entity.DiagnoseExecutionLog
import de.diedavids.cuba.runtimediagnose.web.screens.diagnose.DiagnoseFileDownloader

import javax.inject.Inject

class DiagnoseExecutionLogBrowse extends AbstractLookup {

    @Inject
    GroupTable<DiagnoseExecutionLog> diagnoseExecutionLogsTable

    @Inject
    DiagnoseFileDownloader diagnoseFileDownloader

    @Inject
    FileStorageService fileStorageService

    void downloadResultFile() {
        DiagnoseExecutionLog executionLog = diagnoseExecutionLogsTable.singleSelected
        def zipBytes = fileStorageService.loadFile(executionLog.executionResultFile)
        diagnoseFileDownloader.downloadFile(this,zipBytes, executionLog.executionResultFile.name)
    }
}