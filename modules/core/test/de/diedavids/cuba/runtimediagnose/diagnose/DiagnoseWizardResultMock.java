package de.diedavids.cuba.runtimediagnose.diagnose;

import de.diedavids.cuba.runtimediagnose.wizard.DiagnoseWizardResult;

class DiagnoseWizardResultMock extends DiagnoseWizardResult {


    private String messageString;

    public void setMessageString(String messageString) {
        this.messageString = messageString;
    }

    @Override
    public String getMessage() {
        return messageString;
    }
}