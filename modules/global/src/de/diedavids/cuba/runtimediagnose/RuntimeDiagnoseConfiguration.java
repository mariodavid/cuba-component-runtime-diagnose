package de.diedavids.cuba.runtimediagnose;

import com.haulmont.cuba.core.config.Config;
import com.haulmont.cuba.core.config.Property;
import com.haulmont.cuba.core.config.Source;
import com.haulmont.cuba.core.config.SourceType;
import com.haulmont.cuba.core.config.defaults.Default;

@Source(type = SourceType.DATABASE)
public interface RuntimeDiagnoseConfiguration extends Config {

    @Property("runtime-diagnose.sql.allowDataManipulation")
    @Default("false")
    Boolean getSqlAllowDataManipulation();


    @Property("runtime-diagnose.sql.allowSchemaManipulation")
    @Default("false")
    Boolean getSqlAllowSchemaManipulation();


    @Property("runtime-diagnose.sql.allowExecuteOperations")
    @Default("false")
    Boolean getSqlAllowExecuteOperations();

    @Property("runtime-diagnose.log.enabled")
    @Default("false")
    Boolean getLogEnabled();

    @Property("runtime-diagnose.log.logDiagnoseDetails")
    @Default("false")
    Boolean getLogDiagnoseDetails();

}
