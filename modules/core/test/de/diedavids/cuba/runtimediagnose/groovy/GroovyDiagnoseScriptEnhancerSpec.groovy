package de.diedavids.cuba.runtimediagnose.groovy

import com.haulmont.chile.core.model.MetaClass
import com.haulmont.cuba.core.entity.Entity
import com.haulmont.cuba.core.global.Metadata
import com.haulmont.cuba.core.global.MetadataTools
import com.haulmont.cuba.security.entity.Role
import com.haulmont.cuba.security.entity.User
import de.diedavids.cuba.runtimediagnose.RuntimeDiagnoseConfiguration
import de.diedavids.cuba.runtimediagnose.diagnose.DiagnoseExecution
import spock.lang.Specification

class GroovyDiagnoseScriptEnhancerSpec extends Specification {

    GroovyDiagnoseScriptEnhancer sut
    Metadata metadata
    MetadataTools metadataTools
    RuntimeDiagnoseConfiguration configuration

    def setup() {
        metadata = Mock(Metadata)
        configuration = Mock(RuntimeDiagnoseConfiguration)
        sut = new GroovyDiagnoseScriptEnhancer(
                metadata: metadata,
                configuration: configuration
        )
        metadataTools = Mock(MetadataTools)
        metadata.getTools() >> metadataTools

    }

    def "enhance adds the import statements of all entities if the property is activated"() {

        given:
        def diagnoseExecution = new DiagnoseExecution(diagnoseScript: """
def user = dataManager.create(User)
""")

        and:
        entitiesAvailable([
                entityAvailable(User),
                entityAvailable(Role),
        ])

        and:
        configuration.getConsoleAutoImportEntities() >> true

        when:
        def enhancedScript = sut.enhance(diagnoseExecution)

        then:
        enhancedScript == """import com.haulmont.cuba.security.entity.User;
import com.haulmont.cuba.security.entity.Role;

def user = dataManager.create(User)
"""
    }

    def "enhance adds no the import statements of all entities if the property is deactivated"() {

        given:
        entitiesAvailable([
                entityAvailable(User),
                entityAvailable(Role),
        ])

        and:
        configuration.getConsoleAutoImportEntities() >> false

        when:
        def enhancedScript = runScript("""
def user = dataManager.create(User)
""")
        then:
        enhancedScript == """
def user = dataManager.create(User)
"""
    }

    def "enhance adds the import statements of all additional classes if the property is activated"() {

        given:
        entitiesAvailable([
                entityAvailable(User),
                entityAvailable(Role),
        ])

        and:
        configuration.getConsoleAutoImportAdditionalClasses() >> "com.haulmont.cuba.core.app.UniqueNumbersService; com.haulmont.cuba.core.app.EntityLogService"

        when:
        def enhancedScript = runScript("""
def user = dataManager.create(User)
""")
        then:
        enhancedScript == """import com.haulmont.cuba.core.app.UniqueNumbersService;
import com.haulmont.cuba.core.app.EntityLogService;

def user = dataManager.create(User)
"""
    }

    private String runScript(String script) {
        sut.enhance(new DiagnoseExecution(diagnoseScript: script))
    }

    private void entitiesAvailable(List<MetaClass> metaClasses) {
        metadataTools.getAllPersistentMetaClasses() >> metaClasses
    }

    private void noEntitiesAvailable() {
        metadataTools.getAllPersistentMetaClasses() >> []
    }

    private MetaClass entityAvailable(Class<? extends Entity> clazz) {
        def userMetaClass = Mock(MetaClass)
        userMetaClass.getJavaClass() >> clazz
        userMetaClass
    }


}
