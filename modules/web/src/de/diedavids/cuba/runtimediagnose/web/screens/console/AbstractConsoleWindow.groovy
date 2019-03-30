package de.diedavids.cuba.runtimediagnose.web.screens.console

import com.haulmont.cuba.gui.Notifications
import com.haulmont.cuba.gui.components.AbstractWindow
import com.haulmont.cuba.gui.components.SourceCodeEditor
import com.haulmont.cuba.gui.components.SplitPanel
import de.diedavids.cuba.runtimediagnose.diagnose.DiagnoseExecution
import de.diedavids.cuba.runtimediagnose.diagnose.DiagnoseExecutionFactory
import de.diedavids.cuba.runtimediagnose.diagnose.DiagnoseType
import de.diedavids.cuba.runtimediagnose.web.screens.diagnose.DiagnoseFileDownloader
import groovy.transform.CompileStatic

import javax.inject.Inject

@CompileStatic
abstract class AbstractConsoleWindow extends AbstractWindow {

    public static final int SPLIT_POSITION_CENTER = 50

    @Inject
    SourceCodeEditor console

    @Inject
    SplitPanel consoleResultSplitter

    @Inject
    DiagnoseExecutionFactory diagnoseExecutionFactory

    @Inject
    DiagnoseFileDownloader diagnoseFileDownloader

    @Inject
    Notifications notifications

    DiagnoseExecution diagnoseExecution

    abstract DiagnoseType getDiagnoseType()

    abstract void doRunConsole()

    abstract void clearConsoleResult()

    void clearConsole() {
        console.setValue('')
    }

    void runConsole() {
        if (console.value) {
            doRunConsole()
        } else {
            notifications.create(Notifications.NotificationType.WARNING)
                    .withCaption(getMessage('noScriptDefined'))
                    .show()
        }
    }

    void downloadConsoleResult() {
        def zipBytes = diagnoseExecutionFactory.createExecutionResultFromDiagnoseExecution(diagnoseExecution)
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
        diagnoseExecution = diagnoseExecutionFactory.createAdHocDiagnoseExecution(console.value as String, diagnoseType)
        def zipBytes = diagnoseExecutionFactory.createDiagnoseRequestFileFromDiagnoseExecution(diagnoseExecution)
        diagnoseFileDownloader.downloadFile(this, zipBytes, 'diagnose.zip')
    }
}