package de.diedavids.cuba.runtimediagnose.web.screens.wizard

import com.haulmont.cuba.client.ClientConfig
import com.haulmont.cuba.core.global.Configuration
import de.diedavids.cuba.runtimediagnose.diagnose.DiagnoseExecution
import de.diedavids.cuba.runtimediagnose.diagnose.DiagnoseFileValidationService
import de.diedavids.cuba.runtimediagnose.web.screens.SpecificationWithApplicationContext
import de.diedavids.cuba.runtimediagnose.wizard.DiagnoseWizardResult

class DiagnoseFileValidationDatasourceSpec extends SpecificationWithApplicationContext {

    DiagnoseFileValidationDatasource sut
    private DiagnoseFileValidationService diagnoseFileValidationService
    private Configuration configuration

    @Override
    Map<Class, Object> getBeans() {
        diagnoseFileValidationService = Mock(DiagnoseFileValidationService)
        configuration = Mock(Configuration)
        def clientConfig = Mock(ClientConfig)
        configuration.getConfig(ClientConfig) >> clientConfig
        [
                (DiagnoseFileValidationService):diagnoseFileValidationService,
                (Configuration): configuration
        ]
    }

    def setup() {
        sut = new DiagnoseFileValidationDatasource()
    }

    def "getEntities returns an empty list if the diagnose param is not set"() {
        given:
        diagnoseFileValidationService.validateDiagnose(_) >> []
        expect:
        !sut.getEntities([:])
    }

    def "getEntities delegates to the diagnoseFileValidationService"() {
        given:
        def diagnoseExecution = new DiagnoseExecution()
        def expectedResult = [new DiagnoseWizardResult()]

        and:
        diagnoseFileValidationService.validateDiagnose(diagnoseExecution) >> expectedResult

        expect:
        sut.getEntities([diagnose: diagnoseExecution]) == expectedResult
    }
}
