package de.diedavids.cuba.console.service;

import de.diedavids.cuba.console.DiagnoseExecution;
import de.diedavids.cuba.console.wizard.DiagnoseFileValidation;

import java.util.Collection;


public interface DiagnoseFileValidationService {
    String NAME = "console_DiagnoseFileValidationService";

    Collection<DiagnoseFileValidation> validateDiagnose(DiagnoseExecution diagnose);
}