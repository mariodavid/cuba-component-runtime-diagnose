package de.diedavids.cuba.console;

import com.haulmont.cuba.core.config.Config;
import com.haulmont.cuba.core.config.Property;
import com.haulmont.cuba.core.config.Source;
import com.haulmont.cuba.core.config.SourceType;
import com.haulmont.cuba.core.config.defaults.Default;

@Source(type = SourceType.DATABASE)
public interface ConsoleConfiguration extends Config {

    @Property("console.sql.allowDataManipulation")
    @Default("false")
    Boolean getSqlAllowDataManipulation();


    @Property("console.sql.allowSchemaManipulation")
    @Default("false")
    Boolean getSqlAllowSchemaManipulation();


    @Property("console.sql.allowExecuteOperations")
    @Default("false")
    Boolean getSqlAllowExecuteOperations();


}
