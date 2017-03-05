package de.diedavids.cuba.console.wizard;

import com.haulmont.chile.core.annotations.MetaClass;
import com.haulmont.chile.core.annotations.MetaProperty;
import com.haulmont.cuba.core.entity.AbstractNotPersistentEntity;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Messages;

@MetaClass(name = "console$DiagnoseFileValidation")
public class DiagnoseResult extends AbstractNotPersistentEntity {
    private static final long serialVersionUID = 8079820443159058331L;

    @MetaProperty(mandatory = true)
    protected String type;

    @MetaProperty
    protected String messageCode;

    @MetaProperty
    protected String messagePack;

    @MetaProperty
    protected String message;

    public void setType(DiagnoseFileValidationType type) {
        this.type = type == null ? null : type.getId();
    }

    public DiagnoseFileValidationType getType() {
        return type == null ? null : DiagnoseFileValidationType.fromId(type);
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
        Messages messages = AppBeans.get(Messages.class);

        String result = "";

        if (result.equals(messageCode) && messagePack != null) {
            result = messages.getMessage(messagePack, messageCode);
        }

        if (result.equals(messageCode)) {
            result = messages.getMainMessage(messageCode);
        }

        if (message != null) {
            result = message;
        }
        return result;
    }

}