package de.diedavids.cuba.console.service;

import de.diedavids.cuba.console.DiagnoseExecution
import de.diedavids.cuba.console.wizard.DiagnoseWizardResult

public interface DiagnoseFileValidationService {
    String NAME = "console_DiagnoseFileValidationService";

    Collection<DiagnoseWizardResult> validateDiagnose(DiagnoseExecution diagnose);
}