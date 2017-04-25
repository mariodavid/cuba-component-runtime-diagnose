package de.diedavids.cuba.runtimediagnose.web.screens.wizard;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.gui.data.impl.CustomCollectionDatasource;
import de.diedavids.cuba.runtimediagnose.diagnose.DiagnoseFileValidationService;
import de.diedavids.cuba.runtimediagnose.diagnose.DiagnoseExecution;
import de.diedavids.cuba.runtimediagnose.wizard.DiagnoseWizardResult;

import java.util.*;

public class DiagnoseFileValidationDatasource extends CustomCollectionDatasource<DiagnoseWizardResult, UUID> {

    private DiagnoseFileValidationService diagnoseFileValidationService = AppBeans.get(DiagnoseFileValidationService.NAME);

    @Override
    protected Collection<DiagnoseWizardResult> getEntities(Map params) {

        DiagnoseExecution diagnose = (DiagnoseExecution) params.get("diagnose");

        if (diagnose != null) {
            return diagnoseFileValidationService.validateDiagnose(diagnose);
        }
        else {
            return new HashSet<>();
        }

    }
}