package de.diedavids.cuba.console.diagnose

import de.diedavids.cuba.console.wizard.DiagnoseWizardResult

interface DiagnoseWizardResultService {
    String NAME = 'ddrd_DiagnoseWizardResultService'

    Collection<DiagnoseWizardResult> createResultsForDiagnose(DiagnoseExecution diagnose)
}