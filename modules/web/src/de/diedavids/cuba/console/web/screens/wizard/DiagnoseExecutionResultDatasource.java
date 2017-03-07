package de.diedavids.cuba.console.web.screens.wizard;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.gui.data.impl.CustomCollectionDatasource;
import de.diedavids.cuba.console.diagnose.DiagnoseExecution;
import de.diedavids.cuba.console.diagnose.DiagnoseWizardResultService;
import de.diedavids.cuba.console.wizard.DiagnoseWizardResult;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

public class DiagnoseExecutionResultDatasource extends CustomCollectionDatasource<DiagnoseWizardResult, UUID> {

    private DiagnoseWizardResultService diagnoseWizardResultService = AppBeans.get(DiagnoseWizardResultService.NAME);

    @Override
    protected Collection<DiagnoseWizardResult> getEntities(Map params) {

        DiagnoseExecution diagnose = (DiagnoseExecution) params.get("diagnose");

        if (diagnose != null) {
            return diagnoseWizardResultService.createResultsForDiagnose(diagnose);
        }
        else {
            return new HashSet<>();
        }

    }
}