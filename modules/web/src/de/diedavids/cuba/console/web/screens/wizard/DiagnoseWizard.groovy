package de.diedavids.cuba.console.web.screens.wizard

import com.haulmont.cuba.core.global.*
import com.haulmont.cuba.gui.components.*
import com.haulmont.cuba.gui.export.ByteArrayDataProvider
import com.haulmont.cuba.gui.export.ExportDisplay
import com.haulmont.cuba.gui.export.ExportFormat
import com.haulmont.cuba.gui.upload.FileUploadingAPI
import de.diedavids.cuba.console.DiagnoseExecution
import de.diedavids.cuba.console.DiagnoseExecutionFactory
import de.diedavids.cuba.console.DiagnoseExecutionFactoryBean
import de.diedavids.cuba.console.service.GroovyDiagnoseService
import de.diedavids.cuba.console.wizard.DiagnoseFileValidation
import de.diedavids.cuba.console.wizard.DiagnoseFileValidationType

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
    DiagnoseFileValidationDatasource diagnoseFileValidationDs

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


    DiagnoseExecution diagnoseExecution



    @Override
    void init(Map<String, Object> params) {

        diagnoseFileValidationTable.setIconProvider(new ListComponent.IconProvider<DiagnoseFileValidation>() {
            @Override
            String getItemIcon(DiagnoseFileValidation entity) {
                entity.type.icon
            }
        })

        consoleFileUploadBtn.addFileUploadSucceedListener(new FileUploadField.FileUploadSucceedListener() {
            @Override
            void fileUploadSucceed(FileUploadField.FileUploadSucceedEvent e) {
                File file = fileUploadingAPI.getFile(consoleFileUploadBtn.fileId)

                diagnoseExecution = diagnoseExecutionFactory.createDiagnoseExecutionFromFile(file)

                diagnoseFileValidationDs.refresh([diagnose: diagnoseExecution])

                wizardAccordion.getTab("step2").setEnabled(true)
                wizardAccordion.setTab("step2")

                if (diagnoseFileValidationDs.getItems().any {it.type == DiagnoseFileValidationType.ERROR}) {
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

        if (diagnoseExecution.executionSuccessful) {
            showNotification("yeah", Frame.NotificationType.HUMANIZED)
        }
        else {
            showNotification("non", Frame.NotificationType.ERROR)
        }
    }


    protected String createZipFileName() {
        def dateString = datatypeFormatter.formatDateTime(timeSource.currentTimestamp()).replace(" ", "-")
        def appName = globalConfig.webContextName
        "${appName}-console-execution-${dateString}.zip"
    }


    void executeDiagnosis() {

        if (diagnoseExecution.isGroovy()) {
            runGroovyDiagnose()

            wizardAccordion.getTab("step3").setEnabled(true)
            wizardAccordion.setTab("step3")

            Accordion.Tab step2 = wizardAccordion.getTab("step2")
            step2.setCaption(step2.getCaption() + " " + formatMessage("check"))
            step2.setEnabled(false)
        }

    }

    void cancelWizard() {
        close('cancel')
    }

    void downloadDiagnoseResult() {

        def zipBytes = diagnoseExecutionFactory.createExecutionResultFormDiagnoseExecution(diagnoseExecution)

        try {
            exportDisplay.show(new ByteArrayDataProvider(zipBytes),
                    createZipFileName(), ExportFormat.ZIP);
        } catch (Exception e) {
            showNotification(getMessage("exportFailed"), e.getMessage(), Frame.NotificationType.ERROR);
        }
    }
}
