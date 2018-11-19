package de.diedavids.cuba.runtimediagnose.web.screens.groovy

import com.haulmont.cuba.gui.components.Button
import com.haulmont.cuba.gui.components.Frame
import com.haulmont.cuba.gui.components.SourceCodeEditor
import de.diedavids.cuba.runtimediagnose.diagnose.DiagnoseExecution
import de.diedavids.cuba.runtimediagnose.diagnose.DiagnoseExecutionFactory
import de.diedavids.cuba.runtimediagnose.diagnose.DiagnoseType
import de.diedavids.cuba.runtimediagnose.groovy.GroovyDiagnoseService
import de.diedavids.cuba.runtimediagnose.web.screens.console.AbstractConsoleWindow
import de.diedavids.cuba.runtimediagnose.web.screens.diagnose.DiagnoseFileDownloader

import javax.inject.Inject

class GroovyConsole extends AbstractConsoleWindow {

    @Inject
    SourceCodeEditor consoleResult
    @Inject
    SourceCodeEditor consoleResultLog
    @Inject
    SourceCodeEditor consoleStacktraceLog
    @Inject
    SourceCodeEditor consoleExecutedScriptLog
    @Inject
    Button downloadResultBtn

    @Inject
    GroovyDiagnoseService groovyDiagnoseService
    @Inject
    DiagnoseExecutionFactory diagnoseExecutionFactory
    @Inject
    DiagnoseFileDownloader diagnoseFileDownloader

    DiagnoseExecution diagnoseExecution

    @Override
    void doRunConsole() {
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
    }

    protected void updateResultTabs(String result, String log, String stacktraceLog, String groovyScript) {
        consoleResult.value = result
        consoleResultLog.value = log
        consoleStacktraceLog.value = stacktraceLog
        consoleExecutedScriptLog.value = groovyScript
    }

    @Override
    DiagnoseType getDiagnoseType() {
        DiagnoseType.GROOVY
    }

    @Override
    void clearConsoleResult() {
        updateResultTabs('', '', '', '')
    }

}