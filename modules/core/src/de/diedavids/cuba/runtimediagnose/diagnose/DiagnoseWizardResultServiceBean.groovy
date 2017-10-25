package de.diedavids.cuba.runtimediagnose.diagnose

import com.haulmont.cuba.core.global.Messages
import com.haulmont.cuba.core.global.Metadata
import de.diedavids.cuba.runtimediagnose.wizard.DiagnoseWizardResult
import de.diedavids.cuba.runtimediagnose.wizard.DiagnoseWizardResultType
import groovy.transform.CompileStatic
import org.springframework.stereotype.Service

import javax.inject.Inject

@Service(DiagnoseWizardResultService.NAME)
@CompileStatic
class DiagnoseWizardResultServiceBean implements DiagnoseWizardResultService {

    @Inject
    Metadata metadata

    @Inject
    Messages messages

    @Override
    Collection<DiagnoseWizardResult> createResultsForDiagnose(DiagnoseExecution diagnose) {
        [
            createBasicSuccessErrorResult(diagnose)
        ]
    }

    private DiagnoseWizardResult createBasicSuccessErrorResult(DiagnoseExecution diagnose) {
        DiagnoseWizardResult wizardResult = createDiagnoseFileValidation()

        if (diagnose.executionSuccessful) {
            wizardResult.type = DiagnoseWizardResultType.SUCCESS

            wizardResult.message = messages.getMessage(getClass(), 'diagnoseExecutedSuccessful')
        }
        else {
            wizardResult.type = DiagnoseWizardResultType.ERROR
            wizardResult.message = messages.getMessage(getClass(), 'diagnoseExecutedError')
        }
        wizardResult
    }

    private DiagnoseWizardResult createDiagnoseFileValidation() {
        metadata.create(DiagnoseWizardResult)
    }

}