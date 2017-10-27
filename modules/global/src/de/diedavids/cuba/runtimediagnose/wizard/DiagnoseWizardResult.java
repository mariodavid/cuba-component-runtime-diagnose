package de.diedavids.cuba.runtimediagnose.wizard;

import com.haulmont.chile.core.annotations.MetaClass;
import com.haulmont.chile.core.annotations.MetaProperty;
import com.haulmont.cuba.core.entity.AbstractNotPersistentEntity;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Messages;

@MetaClass(name = "console$DiagnoseWizardResult")
public class DiagnoseWizardResult extends AbstractNotPersistentEntity {
    private static final long serialVersionUID = 8079820443159058331L;

    @MetaProperty(mandatory = true)
    protected String type;

    @MetaProperty
    protected String messageCode;

    @MetaProperty
    protected String messagePack;

    @MetaProperty
    protected String message;

    public void setType(DiagnoseWizardResultType type) {
        this.type = type == null ? null : type.getId();
    }

    public DiagnoseWizardResultType getType() {
        return type == null ? null : DiagnoseWizardResultType.fromId(type);
    }



    public void setMessagePack(String messagePack) {
        this.messagePack = messagePack;
    }

    public String getMessagePack() {
        return messagePack;
    }


    public void setMessageCode(String messageCode) {
        this.messageCode = messageCode;
    }

    public String getMessageCode() {
        return messageCode;
    }


    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        Messages messages = getMessages();

        String result = "";

        if (messageCode != null) {

            if (messagePack != null) {
                result = messages.getMessage(messagePack, messageCode);
            }
            else {
                result = messages.getMainMessage(messageCode);
            }
        }
        else if (message != null) {
            result = message;
        }
        return result;
    }

    protected Messages getMessages() {
        return AppBeans.get(Messages.class);
    }

}