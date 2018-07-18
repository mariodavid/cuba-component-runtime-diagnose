package de.diedavids.cuba.runtimediagnose.groovy.binding;

import java.util.Map;

interface GroovyScriptBindingSupplier {

    Map<String, Object> getBinding();
}