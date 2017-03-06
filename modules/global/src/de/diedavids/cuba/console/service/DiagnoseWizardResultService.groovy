package de.diedavids.cuba.console.service

import de.diedavids.cuba.console.DiagnoseExecution
import de.diedavids.cuba.console.wizard.DiagnoseWizardResult


public interface DiagnoseWizardResultService {
    String NAME = "console_DiagnoseWizardResultService";

    Collection<DiagnoseWizardResult> createResultsForDiagnose(DiagnoseExecution diagnose);
}