package de.diedavids.cuba.runtimediagnose.web.screens.wizard;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.gui.data.impl.CustomCollectionDatasource;
import de.diedavids.cuba.runtimediagnose.diagnose.DiagnoseExecution;
import de.diedavids.cuba.runtimediagnose.diagnose.DiagnoseWizardResultService;
import de.diedavids.cuba.runtimediagnose.wizard.DiagnoseWizardResult;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

public class DiagnoseExecutionResultDatasource extends CustomCollectionDatasource<DiagnoseWizardResult, UUID> {

    private DiagnoseWizardResultService diagnoseWizardResultService = getDiagnoseWizardResultService();

    private DiagnoseWizardResultService getDiagnoseWizardResultService() {
        return AppBeans.get(DiagnoseWizardResultService.NAME);
    }

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