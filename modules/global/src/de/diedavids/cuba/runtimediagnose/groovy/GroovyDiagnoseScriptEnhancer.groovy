package de.diedavids.cuba.runtimediagnose.groovy

import com.haulmont.cuba.core.global.Metadata
import de.diedavids.cuba.runtimediagnose.RuntimeDiagnoseConfiguration
import de.diedavids.cuba.runtimediagnose.diagnose.DiagnoseExecution
import org.springframework.stereotype.Component

import javax.inject.Inject

@Component('ddcrd_GroovyDiagnoseScriptEnhancer')
class GroovyDiagnoseScriptEnhancer {


    @Inject
    RuntimeDiagnoseConfiguration configuration

    @Inject
    Metadata metadata

    private static final String NEW_LINE_CHAR = '\n'

    String enhance(DiagnoseExecution diagnoseExecution) {

        def importStatements = [] as List<String>

        if (configuration.consoleAutoImportEntities) {
            importStatements.addAll(entitiesImportStatements())
        }

        if (configuration.consoleAutoImportAdditionalClasses) {
            importStatements.addAll(additionalImportStatements())
        }

        def result = createImportStatementsString(importStatements) + diagnoseExecution.diagnoseScript

        diagnoseExecution.executedDiagnoseScript = result

        result

    }

    private String createImportStatementsString(List<String> importStatements) {
        importStatements ? importStatements.join(NEW_LINE_CHAR) + NEW_LINE_CHAR : ''
    }

    private List<String> entitiesImportStatements() {
        def metaClasses = metadata.tools.allPersistentMetaClasses
        metaClasses.collect {
            "import ${it.javaClass.name};".toString()
        }
    }

    private List<String> additionalImportStatements() {
        configuration.consoleAutoImportAdditionalClasses.split(';').collect {
            "import ${it.trim()};".toString()
        }
    }
}
