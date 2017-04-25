package de.diedavids.cuba.runtimediagnose.wizard;

import com.haulmont.chile.core.datatypes.impl.EnumClass;

import javax.annotation.Nullable;


public enum DiagnoseWizardResultType implements EnumClass<String> {

    ERROR("ERROR", "font-icon:EXCLAMATION"),
    WARNING("WARNING", "font-icon:INFO"),
    SUCCESS("SUCCESS", "font-icon:CHECK");

    private String id;

    private String icon;

    DiagnoseWizardResultType(String value, String icon) {
        this.id = value;
        this.icon = icon;
    }

    public String getId() {
        return id;
    }

    public String getIcon() {
        return icon;
    }

    @Nullable
    public static DiagnoseWizardResultType fromId(String id) {
        for (DiagnoseWizardResultType at : DiagnoseWizardResultType.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}