package de.diedavids.cuba.runtimediagnose.web.screens.groovy

import com.haulmont.cuba.core.global.*
import com.haulmont.cuba.gui.components.*
import com.haulmont.cuba.gui.export.ExportDisplay
import com.haulmont.cuba.security.global.UserSession
import de.diedavids.cuba.runtimediagnose.diagnose.DiagnoseExecution
import de.diedavids.cuba.runtimediagnose.diagnose.DiagnoseExecutionFactory
import de.diedavids.cuba.runtimediagnose.diagnose.DiagnoseType
import de.diedavids.cuba.runtimediagnose.groovy.GroovyDiagnoseService
import de.diedavids.cuba.runtimediagnose.web.screens.diagnose.DiagnoseFileDownloader

import javax.inject.Inject

class GroovyConsole extends AbstractWindow {

    public static final int SPLIT_POSITION_CENTER = 50

    @Inject
    SourceCodeEditor console
    @Inject
    SourceCodeEditor consoleResult
    @Inject
    SourceCodeEditor consoleResultLog
    @Inject
    SourceCodeEditor consoleStacktraceLog
    @Inject
    SourceCodeEditor consoleExecutedScriptLog
    @Inject
    SplitPanel consoleResultSplitter
    @Inject
    Button downloadResultBtn

    @Inject
    GlobalConfig globalConfig
    @Inject
    ExportDisplay exportDisplay
    @Inject
    Scripting scripting
    @Inject
    Metadata metadata
    @Inject
    DataManager dataManager
    @Inject
    DatatypeFormatter datatypeFormatter
    @Inject
    UserSession userSession
    @Inject
    TimeSource timeSource

    @Inject
    GroovyDiagnoseService groovyDiagnoseService
    @Inject
    DiagnoseExecutionFactory diagnoseExecutionFactory
    @Inject
    DiagnoseFileDownloader diagnoseFileDownloader

    DiagnoseExecution diagnoseExecution

    void runGroovyDiagnose() {
        if (console.value) {
            diagnoseExecution = diagnoseExecutionFactory.createAdHocDiagnoseExecution(console.value, DiagnoseType.GROOVY)
            diagnoseExecution = groovyDiagnoseService.runGroovyDiagnose(diagnoseExecution)
            if (diagnoseExecution.executionSuccessful) {
                showNotification(formatMessage('executionSuccessful'), Frame.NotificationType.TRAY)
            } else {
                showNotification(formatMessage('executionError'), Frame.NotificationType.ERROR)
            }
            updateResultTabs(
                    diagnoseExecution.getResult('result'),
                    diagnoseExecution.getResult('log'),
                    diagnoseExecution.getResult('stacktrace'),
                    diagnoseExecution.diagnoseScript,
            )
            downloadResultBtn.enabled = diagnoseExecution.executed
        } else {
            showNotification(formatMessage('noScriptDefined'), Frame.NotificationType.WARNING)
        }
    }

    protected void updateResultTabs(String result, String log, String stacktraceLog, String groovyScript) {
        consoleResult.value = result
        consoleResultLog.value = log
        consoleStacktraceLog.value = stacktraceLog
        consoleExecutedScriptLog.value = groovyScript
    }

    void clearConsole() {
        console.value = ''
    }

    void clearConsoleResult() {
        updateResultTabs('', '', '', '')
    }

    void downloadConsoleResult() {
        def zipBytes = diagnoseExecutionFactory.createExecutionResultFormDiagnoseExecution(diagnoseExecution)
        diagnoseFileDownloader.downloadFile(this, zipBytes)
    }

    void maximizeConsole() {
        consoleResultSplitter.splitPosition = 100
    }

    void maximizeConsoleResult(Integer position = 0) {
        consoleResultSplitter.splitPosition = position
    }

    void minimizeConsole() {
        consoleResultSplitter.splitPosition = SPLIT_POSITION_CENTER
    }

    void minimizeConsoleResult() {
        consoleResultSplitter.splitPosition = SPLIT_POSITION_CENTER
    }

    void downloadDiagnoseRequestFile() {
        diagnoseExecution = diagnoseExecutionFactory.createAdHocDiagnoseExecution(console.value, DiagnoseType.GROOVY)
        def zipBytes = diagnoseExecutionFactory.createDiagnoseRequestFileFormDiagnoseExecution(diagnoseExecution)
        diagnoseFileDownloader.downloadFile(this, zipBytes, 'diagnose.zip')
    }
}