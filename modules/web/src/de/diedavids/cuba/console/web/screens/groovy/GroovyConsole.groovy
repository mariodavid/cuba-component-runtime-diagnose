package de.diedavids.cuba.console.web.screens.groovy

import com.haulmont.cuba.core.global.*
import com.haulmont.cuba.gui.components.*
import com.haulmont.cuba.gui.export.ByteArrayDataProvider
import com.haulmont.cuba.gui.export.ExportDisplay
import com.haulmont.cuba.gui.export.ExportFormat
import com.haulmont.cuba.security.global.UserSession
import de.diedavids.cuba.console.diagnose.DiagnoseExecution
import de.diedavids.cuba.console.diagnose.DiagnoseExecutionFactory
import de.diedavids.cuba.console.groovy.GroovyDiagnoseService

import javax.inject.Inject

class GroovyConsole extends AbstractWindow {


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
    GlobalConfig globalConfig

    @Inject
    Button downloadResultBtn

    @Inject
    protected ExportDisplay exportDisplay;

    @Inject
    Scripting scripting

    @Inject
    DataManager dataManager

    // cuba.groovyEvaluatorImport (https://doc.cuba-platform.com/manual-6.4/app_properties_reference.html)

    @Inject
    Metadata metadata

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

    DiagnoseExecution diagnoseExecution

    void runGroovyDiagnose() {
        if (console.value) {
            diagnoseExecution = diagnoseExecutionFactory.createAdHocDiagnoseExecution(console.value)
            diagnoseExecution = groovyDiagnoseService.runGroovyDiagnose(diagnoseExecution)
            if (diagnoseExecution.executionSuccessful) {
                showNotification("Execution successful", Frame.NotificationType.TRAY)
            } else {
                showNotification("Execution failed. See Result-Tab for more information", Frame.NotificationType.ERROR)
            }
            updateResultTabs(
                    diagnoseExecution.getResult('result'),
                    diagnoseExecution.getResult('log'),
                    diagnoseExecution.getResult('stacktrace'),
                    diagnoseExecution.diagnoseScript,
            )
            downloadResultBtn.enabled = diagnoseExecution.executed
        } else {
            showNotification("Script has to be defined", Frame.NotificationType.WARNING)
        }
    }

    protected void updateResultTabs(String result, String log, String stacktraceLog, String groovyScript) {
        consoleResult.value = result
        consoleResultLog.value = log
        consoleStacktraceLog.value = stacktraceLog
        consoleExecutedScriptLog.value = groovyScript
    }

    void clearConsole() {
        console.value = ""
    }

    void clearConsoleResult() {
        consoleResult.value = ""
        consoleResultLog.value = ""
        consoleStacktraceLog.value = ""
        consoleExecutedScriptLog.value = ""

    }


    void downloadConsoleResult() {

        def zipBytes = diagnoseExecutionFactory.createExecutionResultFormDiagnoseExecution(diagnoseExecution)

        try {
            exportDisplay.show(new ByteArrayDataProvider(zipBytes),
                    createZipFileName(), ExportFormat.ZIP);
            showNotification(formatMessage("diagnoseResultsDownloadedMessage"))
        } catch (Exception e) {
            showNotification(getMessage("exportFailed"), e.getMessage(), Frame.NotificationType.ERROR);
        }
    }


    protected String createZipFileName() {
        def dateString = datatypeFormatter.formatDateTime(timeSource.currentTimestamp()).replace(" ", "-")
        def appName = globalConfig.webContextName
        "${appName}-console-execution-${dateString}.zip"
    }


    void maximizeConsole() {
        consoleResultSplitter.splitPosition = 100
    }

    void maximizeConsoleResult(Integer position = 0) {
        consoleResultSplitter.splitPosition = position
    }


    void minimizeConsole() {
        consoleResultSplitter.splitPosition = 50
    }

    void minimizeConsoleResult() {
        consoleResultSplitter.splitPosition = 50
    }

}