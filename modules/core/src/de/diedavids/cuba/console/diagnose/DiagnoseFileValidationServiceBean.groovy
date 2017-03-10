package de.diedavids.cuba.console.diagnose

import com.haulmont.cuba.core.global.Metadata
import de.diedavids.cuba.console.wizard.DiagnoseWizardResult
import de.diedavids.cuba.console.wizard.DiagnoseWizardResultType
import org.springframework.stereotype.Service

import javax.inject.Inject

@Service(DiagnoseFileValidationService.NAME)
class DiagnoseFileValidationServiceBean implements DiagnoseFileValidationService {

    @Inject
    Metadata metadata


    @Override
    Collection<DiagnoseWizardResult> validateDiagnose(DiagnoseExecution diagnose) {
        [
                checkApplication(diagnose),
                checkVersion(diagnose),
                checkFileProducer(diagnose)
        ]
    }

    private DiagnoseWizardResult checkApplication(DiagnoseExecution diagnose) {
        DiagnoseWizardResult diagnoseFileValidation = createDiagnoseFileValidation()

        def manifestAppName = diagnose.manifest.appName
        def expectedAppName = 'console-app'
        if (manifestAppName == expectedAppName) {
            diagnoseFileValidation.type = DiagnoseWizardResultType.SUCCESS
            diagnoseFileValidation.message = "Application correct ($expectedAppName)"
        }
        else {
            diagnoseFileValidation.type = DiagnoseWizardResultType.ERROR
            diagnoseFileValidation.message = "Application wrong ($manifestAppName)"
        }
        diagnoseFileValidation
    }

    private DiagnoseWizardResult createDiagnoseFileValidation() {
        metadata.create(DiagnoseWizardResult)
    }

    private DiagnoseWizardResult checkVersion(DiagnoseExecution diagnose) {
        DiagnoseWizardResult diagnoseFileValidation = createDiagnoseFileValidation()


        def manifestVersion = diagnose.manifest.appVersion
        def expectedVersion = '1.0'
        if (manifestVersion == expectedVersion) {
            diagnoseFileValidation.type = DiagnoseWizardResultType.SUCCESS
            diagnoseFileValidation.message = "Version correct ($expectedVersion)"
        }
        else {
            diagnoseFileValidation.type = DiagnoseWizardResultType.ERROR
            diagnoseFileValidation.message = "Version wrong ($manifestVersion)"
        }
        diagnoseFileValidation
    }

    private DiagnoseWizardResult checkFileProducer(DiagnoseExecution diagnose) {
        DiagnoseWizardResult diagnoseFileValidation = createDiagnoseFileValidation()


        def manifestProducer = diagnose.manifest.producer
        def expectedProducer = 'Company Inc.'
        if (manifestProducer == expectedProducer) {
            diagnoseFileValidation.type = DiagnoseWizardResultType.SUCCESS
            diagnoseFileValidation.message = "Producer correct ($expectedProducer)"
        }
        else {
            diagnoseFileValidation.type = DiagnoseWizardResultType.ERROR
            diagnoseFileValidation.message = "Producer wrong ($manifestProducer)"
        }
        diagnoseFileValidation
    }

}