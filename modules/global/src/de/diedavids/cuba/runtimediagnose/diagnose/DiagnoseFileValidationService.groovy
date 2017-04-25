package de.diedavids.cuba.runtimediagnose.diagnose

import de.diedavids.cuba.runtimediagnose.wizard.DiagnoseWizardResult

interface DiagnoseFileValidationService {
    String NAME = 'ddcrd_DiagnoseFileValidationService'

    Collection<DiagnoseWizardResult> validateDiagnose(DiagnoseExecution diagnose)
}