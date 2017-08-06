package de.diedavids.cuba.runtimediagnose.diagnose

import com.haulmont.cuba.core.global.BuildInfo
import com.haulmont.cuba.core.global.Metadata
import de.diedavids.cuba.runtimediagnose.wizard.DiagnoseWizardResult
import de.diedavids.cuba.runtimediagnose.wizard.DiagnoseWizardResultType
import groovy.transform.CompileStatic
import org.springframework.stereotype.Service

import javax.inject.Inject

@CompileStatic
@Service(DiagnoseFileValidationService.NAME)
class DiagnoseFileValidationServiceBean implements DiagnoseFileValidationService {

    @Inject
    Metadata metadata

    @Inject
    BuildInfo buildInfo


    @Override
    Collection<DiagnoseWizardResult> validateDiagnose(DiagnoseExecution diagnose) {
        [
                checkApplication(diagnose),
                checkVersion(diagnose)
        ]
    }

    private DiagnoseWizardResult checkApplication(DiagnoseExecution diagnose) {
        DiagnoseWizardResult diagnoseFileValidation = createDiagnoseFileValidation()

        def manifestAppName = diagnose.manifest.appName
        if (manifestAppName == expectedAppName) {
            diagnoseFileValidation.type = DiagnoseWizardResultType.SUCCESS
            diagnoseFileValidation.message = "Application correct ($expectedAppName)"
        }
        else {
            diagnoseFileValidation.type = DiagnoseWizardResultType.WARNING
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
        if (manifestVersion == expectedAppVersion) {
            diagnoseFileValidation.type = DiagnoseWizardResultType.SUCCESS
            diagnoseFileValidation.message = "Version correct ($expectedAppVersion)"
        }
        else {
            diagnoseFileValidation.type = DiagnoseWizardResultType.WARNING
            diagnoseFileValidation.message = "Version wrong ($manifestVersion)"
        }
        diagnoseFileValidation
    }


    private String getExpectedAppName() {
        buildInfo.content.appName
    }

    private String getExpectedAppVersion() {
        buildInfo.content.version
    }

}