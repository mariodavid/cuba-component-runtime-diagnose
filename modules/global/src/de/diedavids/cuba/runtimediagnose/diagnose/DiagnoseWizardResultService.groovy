package de.diedavids.cuba.runtimediagnose.diagnose

import de.diedavids.cuba.runtimediagnose.wizard.DiagnoseWizardResult

interface DiagnoseWizardResultService {
    String NAME = 'ddcrd_DiagnoseWizardResultService'

    Collection<DiagnoseWizardResult> createResultsForDiagnose(DiagnoseExecution diagnose)
}