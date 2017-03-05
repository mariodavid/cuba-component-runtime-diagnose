package de.diedavids.cuba.console.service

import com.haulmont.cuba.core.global.Metadata
import de.diedavids.cuba.console.DiagnoseExecution
import de.diedavids.cuba.console.wizard.DiagnoseFileValidation
import de.diedavids.cuba.console.wizard.DiagnoseFileValidationType
import org.springframework.stereotype.Service

import javax.inject.Inject

@Service(DiagnoseFileValidationService.NAME)
public class DiagnoseFileValidationServiceBean implements DiagnoseFileValidationService {

    @Inject
    Metadata metadata


    @Override
    Collection<DiagnoseFileValidation> validateDiagnose(DiagnoseExecution diagnose) {
        [
                checkApplication(diagnose),
                checkVersion(diagnose),
                checkFileProducer(diagnose)
        ]
    }

    private DiagnoseFileValidation checkApplication(DiagnoseExecution diagnose) {
        DiagnoseFileValidation diagnoseFileValidation = createDiagnoseFileValidation()

        def manifestAppName = diagnose.manifest.appName
        def expectedAppName = 'console-app'
        if (manifestAppName == expectedAppName) {
            diagnoseFileValidation.type = DiagnoseFileValidationType.SUCCESS
            diagnoseFileValidation.message = "Application correct ($expectedAppName)"
        }
        else {
            diagnoseFileValidation.type = DiagnoseFileValidationType.ERROR
            diagnoseFileValidation.message = "Application wrong ($manifestAppName)"
        }
        diagnoseFileValidation
    }

    private DiagnoseFileValidation createDiagnoseFileValidation() {
        metadata.create(DiagnoseFileValidation)
    }

    private DiagnoseFileValidation checkVersion(DiagnoseExecution diagnose) {
        DiagnoseFileValidation diagnoseFileValidation = createDiagnoseFileValidation()


        def manifestVersion = diagnose.manifest.appVersion
        def expectedVersion = '1.0'
        if (manifestVersion == expectedVersion) {
            diagnoseFileValidation.type = DiagnoseFileValidationType.SUCCESS
            diagnoseFileValidation.message = "Version correct ($expectedVersion)"
        }
        else {
            diagnoseFileValidation.type = DiagnoseFileValidationType.ERROR
            diagnoseFileValidation.message = "Version wrong ($manifestVersion)"
        }
        diagnoseFileValidation
    }

    private DiagnoseFileValidation checkFileProducer(DiagnoseExecution diagnose) {
        DiagnoseFileValidation diagnoseFileValidation = createDiagnoseFileValidation()


        def manifestProducer = diagnose.manifest.producer
        def expectedProducer = 'Company Inc.'
        if (manifestProducer == expectedProducer) {
            diagnoseFileValidation.type = DiagnoseFileValidationType.SUCCESS
            diagnoseFileValidation.message = "Producer correct ($expectedProducer)"
        }
        else {
            diagnoseFileValidation.type = DiagnoseFileValidationType.ERROR
            diagnoseFileValidation.message = "Producer wrong ($manifestProducer)"
        }
        diagnoseFileValidation
    }

}