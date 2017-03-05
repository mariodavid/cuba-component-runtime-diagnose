package de.diedavids.cuba.console.web.screens.wizard;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.gui.data.impl.CustomCollectionDatasource;
import de.diedavids.cuba.console.service.DiagnoseFileValidationService;
import de.diedavids.cuba.console.DiagnoseExecution;
import de.diedavids.cuba.console.wizard.DiagnoseFileValidation;

import java.util.*;

public class DiagnoseFileValidationDatasource extends CustomCollectionDatasource<DiagnoseFileValidation, UUID> {

    private DiagnoseFileValidationService diagnoseFileValidationService = AppBeans.get(DiagnoseFileValidationService.NAME);

    @Override
    protected Collection<DiagnoseFileValidation> getEntities(Map params) {

        DiagnoseExecution diagnose = (DiagnoseExecution) params.get("diagnose");

        if (diagnose != null) {
            return diagnoseFileValidationService.validateDiagnose(diagnose);
        }
        else {
            return new HashSet<>();
        }

    }
}