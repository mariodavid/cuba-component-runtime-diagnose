package de.diedavids.cuba.console.diagnose

import de.diedavids.cuba.console.wizard.DiagnoseWizardResult

interface DiagnoseFileValidationService {
    String NAME = 'console_DiagnoseFileValidationService'

    Collection<DiagnoseWizardResult> validateDiagnose(DiagnoseExecution diagnose)
}