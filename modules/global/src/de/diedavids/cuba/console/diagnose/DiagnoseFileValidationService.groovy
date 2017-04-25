package de.diedavids.cuba.console.diagnose

import de.diedavids.cuba.console.wizard.DiagnoseWizardResult

interface DiagnoseFileValidationService {
    String NAME = 'ddrd_DiagnoseFileValidationService'

    Collection<DiagnoseWizardResult> validateDiagnose(DiagnoseExecution diagnose)
}