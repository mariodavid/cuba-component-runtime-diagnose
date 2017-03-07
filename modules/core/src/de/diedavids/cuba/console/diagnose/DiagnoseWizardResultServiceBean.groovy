package de.diedavids.cuba.console.diagnose

import com.haulmont.cuba.core.global.Metadata
import de.diedavids.cuba.console.wizard.DiagnoseWizardResult
import de.diedavids.cuba.console.wizard.DiagnoseWizardResultType
import org.springframework.stereotype.Service

import javax.inject.Inject

@Service(DiagnoseWizardResultService.NAME)
public class DiagnoseWizardResultServiceBean implements DiagnoseWizardResultService {

    @Inject
    Metadata metadata

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
            wizardResult.message = "Diagnose Execution successful"
        }
        else {
            wizardResult.type = DiagnoseWizardResultType.ERROR
            wizardResult.message = "Error while executing Diagnose"
        }
        wizardResult
    }

    private DiagnoseWizardResult createDiagnoseFileValidation() {
        metadata.create(DiagnoseWizardResult)
    }

}