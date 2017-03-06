package de.diedavids.cuba.console.web.screens.wizard

import com.haulmont.cuba.core.global.*
import com.haulmont.cuba.gui.components.*
import com.haulmont.cuba.gui.export.ByteArrayDataProvider
import com.haulmont.cuba.gui.export.ExportDisplay
import com.haulmont.cuba.gui.export.ExportFormat
import com.haulmont.cuba.gui.upload.FileUploadingAPI
import de.diedavids.cuba.console.DiagnoseExecution
import de.diedavids.cuba.console.DiagnoseExecutionFactory
import de.diedavids.cuba.console.service.GroovyDiagnoseService
import de.diedavids.cuba.console.wizard.DiagnoseWizardResult
import de.diedavids.cuba.console.wizard.DiagnoseWizardResultType

import javax.inject.Inject

class DiagnoseWizard extends AbstractWindow {

    @Inject
    private FileUploadField consoleFileUploadBtn

    @Inject
    private FileUploadingAPI fileUploadingAPI

    @Inject
    GlobalConfig globalConfig

    @Inject
    protected ExportDisplay exportDisplay;


    @Inject
    Accordion wizardAccordion

    @Inject
    Table diagnoseFileValidationTable
    @Inject
    Table diagnoseWizardResultsTable

    @Inject
    DiagnoseFileValidationDatasource diagnoseFileValidationDs

    @Inject
    DiagnoseExecutionResultDatasource diagnoseWizardResultsDs

    @Inject
    Button executeDiagnosisBtn

    @Inject
    GroovyDiagnoseService groovyDiagnoseService


    @Inject
    DatatypeFormatter datatypeFormatter

    @Inject
    TimeSource timeSource

    @Inject
    DiagnoseExecutionFactory diagnoseExecutionFactory


    @Inject
    Button closeWizard


    DiagnoseExecution diagnoseExecution


    @Override
    void init(Map<String, Object> params) {

        def iconProvider = new DiagnoseWizardResultTypeIconProvider()
        diagnoseFileValidationTable.iconProvider = iconProvider
        diagnoseWizardResultsTable.iconProvider = iconProvider


        consoleFileUploadBtn.addFileUploadSucceedListener(new FileUploadField.FileUploadSucceedListener() {
            @Override
            void fileUploadSucceed(FileUploadField.FileUploadSucceedEvent e) {
                File file = fileUploadingAPI.getFile(consoleFileUploadBtn.fileId)

                diagnoseExecution = diagnoseExecutionFactory.createDiagnoseExecutionFromFile(file)

                diagnoseFileValidationDs.refresh([diagnose: diagnoseExecution])

                wizardAccordion.getTab("step2").setEnabled(true)
                wizardAccordion.setTab("step2")

                if (diagnoseFileValidationDs.getItems().any { it.type == DiagnoseWizardResultType.ERROR }) {
                    executeDiagnosisBtn.setEnabled(false)
                }
                Accordion.Tab step1 = wizardAccordion.getTab("step1")
                step1.setCaption(step1.getCaption() + " " + formatMessage("check"))
                step1.setEnabled(false)

            }
        })

        consoleFileUploadBtn.addFileUploadErrorListener(new UploadField.FileUploadErrorListener() {
            @Override
            void fileUploadError(UploadField.FileUploadErrorEvent e) {
                showNotification("File upload error", Frame.NotificationType.ERROR)
            }
        })
    }

    void runGroovyDiagnose() {
        diagnoseExecution = groovyDiagnoseService.runGroovyDiagnose(diagnoseExecution)

        diagnoseWizardResultsDs.refresh([diagnose: diagnoseExecution])
    }


    protected String createZipFileName() {
        def dateString = datatypeFormatter.formatDateTime(timeSource.currentTimestamp()).replace(" ", "-")
        def appName = globalConfig.webContextName
        "${appName}-console-execution-${dateString}.zip"
    }


    void executeDiagnosis() {

        if (diagnoseExecution.groovy) {
            runGroovyDiagnose()

            wizardAccordion.getTab("step3").setEnabled(true)
            wizardAccordion.setTab("step3")
            closeWizard.setEnabled(true)

            Accordion.Tab step2 = wizardAccordion.getTab("step2")
            step2.setCaption(step2.getCaption() + " " + formatMessage("check"))
            step2.setEnabled(false)
        }

    }

    void cancelWizard() {
        close(CLOSE_ACTION_ID)
    }

    void closeWizard() {
        downloadDiagnoseResult()
        close(CLOSE_ACTION_ID)
    }

    void downloadDiagnoseResult() {

        def zipBytes = diagnoseExecutionFactory.createExecutionResultFormDiagnoseExecution(diagnoseExecution)

        try {
            exportDisplay.show(new ByteArrayDataProvider(zipBytes),
                    createZipFileName(), ExportFormat.ZIP);
            showNotification(formatMessage("diagnoseResultsDownloadedMessage"))
        } catch (Exception e) {
            showNotification(getMessage("exportFailed"), e.getMessage(), Frame.NotificationType.ERROR);
        }
    }

    @Override
    protected boolean preClose(String actionId) {

        if (diagnoseExecution?.pending) {
            showOptionDialog("Cancel Diagnose Execution", "The Diagnose has not been executed yet. Are you sure?", Frame.MessageType.CONFIRMATION, [new DialogAction(DialogAction.Type.OK) {
                @Override
                void actionPerform(Component component) {
                    close(actionId, true)
                }
            }, new DialogAction(DialogAction.Type.CANCEL)])
        } else {
            close(actionId, true)
        }
        return false
    }

}

class DiagnoseWizardResultTypeIconProvider implements ListComponent.IconProvider<DiagnoseWizardResult> {
    @Override
    String getItemIcon(DiagnoseWizardResult entity) {
        entity.type.icon
    }
}