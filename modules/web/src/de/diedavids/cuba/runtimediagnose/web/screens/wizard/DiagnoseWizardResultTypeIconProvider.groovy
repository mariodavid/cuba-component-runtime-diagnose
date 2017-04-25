package de.diedavids.cuba.runtimediagnose.web.screens.wizard

import com.haulmont.cuba.gui.components.ListComponent
import de.diedavids.cuba.runtimediagnose.wizard.DiagnoseWizardResult

class DiagnoseWizardResultTypeIconProvider implements ListComponent.IconProvider<DiagnoseWizardResult> {
    @Override
    String getItemIcon(DiagnoseWizardResult entity) {
        entity.type.icon
    }
}