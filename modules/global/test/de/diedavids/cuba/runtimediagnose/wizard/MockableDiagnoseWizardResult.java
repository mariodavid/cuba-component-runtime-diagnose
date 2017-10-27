package de.diedavids.cuba.runtimediagnose.wizard;

import com.haulmont.cuba.core.global.Messages;

public class MockableDiagnoseWizardResult extends DiagnoseWizardResult {

    private Messages messages;

    public void setMessages(Messages messages) {
        this.messages = messages;
    }

    @Override
    protected Messages getMessages() {
        return this.messages;
    }
}
