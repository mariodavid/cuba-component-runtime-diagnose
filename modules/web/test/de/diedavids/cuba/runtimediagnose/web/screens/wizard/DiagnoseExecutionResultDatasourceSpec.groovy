package de.diedavids.cuba.runtimediagnose.web.screens.wizard

import com.haulmont.cuba.client.ClientConfig
import com.haulmont.cuba.core.global.Configuration
import de.diedavids.cuba.runtimediagnose.diagnose.DiagnoseExecution
import de.diedavids.cuba.runtimediagnose.diagnose.DiagnoseFileValidationService
import de.diedavids.cuba.runtimediagnose.diagnose.DiagnoseWizardResultService
import de.diedavids.cuba.runtimediagnose.web.screens.SpecificationWithApplicationContext
import de.diedavids.cuba.runtimediagnose.wizard.DiagnoseWizardResult
import spock.lang.Specification

class DiagnoseExecutionResultDatasourceSpec extends SpecificationWithApplicationContext {

    DiagnoseExecutionResultDatasource sut
    private DiagnoseWizardResultService diagnoseWizardResultService
    private Configuration configuration

    @Override
    Map<Class, Object> getBeans() {
        diagnoseWizardResultService = Mock(DiagnoseWizardResultService)
        configuration = Mock(Configuration)
        def clientConfig = Mock(ClientConfig)
        configuration.getConfig(ClientConfig) >> clientConfig
        [
                (DiagnoseWizardResultService): diagnoseWizardResultService,
                (Configuration)                : configuration
        ]
    }

    def setup() {
        sut = new DiagnoseExecutionResultDatasource()
    }

    def "getEntities returns an empty list if the diagnose param is not set"() {
        given:
        diagnoseWizardResultService.createResultsForDiagnose(_) >> []
        expect:
        !sut.getEntities([:])
    }

    def "getEntities delegates to the diagnoseWizardResultService"() {
        given:
        def diagnoseExecution = new DiagnoseExecution()
        def expectedResult = [new DiagnoseWizardResult()]

        and:
        diagnoseWizardResultService.createResultsForDiagnose(diagnoseExecution) >> expectedResult

        expect:
        sut.getEntities([diagnose: diagnoseExecution]) == expectedResult
    }
}
