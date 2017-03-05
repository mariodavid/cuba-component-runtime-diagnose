package de.diedavids.cuba.console.wizard;

import com.haulmont.chile.core.datatypes.impl.EnumClass;

import javax.annotation.Nullable;


public enum DiagnoseFileValidationType implements EnumClass<String> {

    ERROR("ERROR", "font-icon:EXCLAMATION"),
    WARNING("WARNING", "font-icon:INFO"),
    SUCCESS("SUCCESS", "font-icon:CHECK");

    private String id;

    private String icon;

    DiagnoseFileValidationType(String value, String icon) {
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
    public static DiagnoseFileValidationType fromId(String id) {
        for (DiagnoseFileValidationType at : DiagnoseFileValidationType.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}