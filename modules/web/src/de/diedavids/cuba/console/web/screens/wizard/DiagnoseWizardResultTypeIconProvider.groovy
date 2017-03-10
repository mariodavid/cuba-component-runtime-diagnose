package de.diedavids.cuba.console.web.screens.wizard

import com.haulmont.cuba.gui.components.ListComponent
import de.diedavids.cuba.console.wizard.DiagnoseWizardResult

class DiagnoseWizardResultTypeIconProvider implements ListComponent.IconProvider<DiagnoseWizardResult> {
    @Override
    String getItemIcon(DiagnoseWizardResult entity) {
        entity.type.icon
    }
}