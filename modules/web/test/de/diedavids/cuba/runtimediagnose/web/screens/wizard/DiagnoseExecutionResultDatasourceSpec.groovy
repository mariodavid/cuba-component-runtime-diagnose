package de.diedavids.cuba.runtimediagnose.web.screens.wizard

import com.haulmont.cuba.client.ClientConfig
import com.haulmont.cuba.core.global.Configuration
import com.haulmont.cuba.core.global.UserSessionSource
import de.diedavids.cuba.runtimediagnose.diagnose.DiagnoseExecution
import de.diedavids.cuba.runtimediagnose.diagnose.DiagnoseWizardResultService
import de.diedavids.cuba.runtimediagnose.web.screens.SpecificationWithApplicationContext
import de.diedavids.cuba.runtimediagnose.wizard.DiagnoseWizardResult

class DiagnoseExecutionResultDatasourceSpec extends SpecificationWithApplicationContext {

  DiagnoseExecutionResultDatasource sut
  DiagnoseWizardResultService diagnoseWizardResultService
  Configuration configuration

  @Override
  Map<Class, Object> getBeans() {
    diagnoseWizardResultService = Mock(DiagnoseWizardResultService)
    configuration = Mock(Configuration)
    def clientConfig = Mock(ClientConfig)
    def userSessionSource = Mock(UserSessionSource)

    configuration.getConfig(ClientConfig) >> clientConfig
    [
        (DiagnoseWizardResultService): diagnoseWizardResultService,
        (Configuration)              : configuration,
        (UserSessionSource)          : userSessionSource
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
