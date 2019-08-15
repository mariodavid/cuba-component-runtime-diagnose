package de.diedavids.cuba.runtimediagnose;

import com.haulmont.cuba.core.config.Config;
import com.haulmont.cuba.core.config.Property;
import com.haulmont.cuba.core.config.Source;
import com.haulmont.cuba.core.config.SourceType;
import com.haulmont.cuba.core.config.defaults.Default;

@Source(type = SourceType.DATABASE)
public interface RuntimeDiagnoseConfiguration extends Config {

    @Property("runtime-diagnose.db.allowDataManipulation")
    @Default("false")
    Boolean getSqlAllowDataManipulation();


    @Property("runtime-diagnose.db.allowSchemaManipulation")
    @Default("false")
    Boolean getSqlAllowSchemaManipulation();


    @Property("runtime-diagnose.db.allowExecuteOperations")
    @Default("false")
    Boolean getSqlAllowExecuteOperations();

    @Property("runtime-diagnose.log.enabled")
    @Default("false")
    Boolean getLogEnabled();

    @Property("runtime-diagnose.log.logDiagnoseDetails")
    @Default("false")
    Boolean getLogDiagnoseDetails();


    @Property("runtime-diagnose.console.autoImport.entities")
    @Default("false")
    Boolean getConsoleAutoImportEntities();

    @Property("runtime-diagnose.console.autoImport.additionalClasses")
    String getConsoleAutoImportAdditionalClasses();

}
