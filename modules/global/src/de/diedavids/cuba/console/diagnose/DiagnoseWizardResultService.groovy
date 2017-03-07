package de.diedavids.cuba.console.diagnose

import de.diedavids.cuba.console.diagnose.DiagnoseExecution
import de.diedavids.cuba.console.wizard.DiagnoseWizardResult


public interface DiagnoseWizardResultService {
    String NAME = "console_DiagnoseWizardResultService";

    Collection<DiagnoseWizardResult> createResultsForDiagnose(DiagnoseExecution diagnose);
}