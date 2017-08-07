package de.diedavids.cuba.runtimediagnose.groovy

import com.haulmont.cuba.core.Persistence
import com.haulmont.cuba.core.global.*
import de.diedavids.cuba.runtimediagnose.diagnose.DiagnoseExecution
import de.diedavids.cuba.runtimediagnose.diagnose.DiagnoseExecutionLogService
import org.springframework.stereotype.Service

import javax.inject.Inject

@SuppressWarnings('DuplicateStringLiteral')
@Service(GroovyDiagnoseService.NAME)
class GroovyDiagnoseServiceBean implements GroovyDiagnoseService {

    @Inject
    Scripting scripting

    @Inject
    DataManager dataManager

    @Inject
    Metadata metadata

    @Inject
    DatatypeFormatter datatypeFormatter

    @Inject
    TimeSource timeSource

    @Inject
    Persistence persistence

    @Inject
    UserSessionSource userSessionSource

    @Inject
    DiagnoseExecutionLogService diagnoseExecutionLogService


    DiagnoseExecution runGroovyDiagnose(DiagnoseExecution diagnoseExecution) {
        if (diagnoseExecution) {
            def log = new GroovyConsoleLogger(timeSource: timeSource, datatypeFormatter: datatypeFormatter)
            Binding binding = createBinding(log)
            diagnoseExecution.executionTimestamp = timeSource.currentTimestamp()
            diagnoseExecution.executionUser = userSessionSource.userSession.currentOrSubstitutedUser.login

            try {
                def result = scripting.evaluateGroovy(diagnoseExecution.diagnoseScript, binding)
                diagnoseExecution.handleSuccessfulExecution(result.toString())
            }

            catch (Exception e) {
                diagnoseExecution.handleErrorExecution(e)
            }
            diagnoseExecution.addResult('log', log.toString())

            diagnoseExecutionLogService.logDiagnoseExecution(diagnoseExecution)

            diagnoseExecution
        }
    }

    protected Binding createBinding(GroovyConsoleLogger log) {
        def binding = new Binding()
        setDefaultBindingVariables(binding, log)

        additionalBindingVariableMap.each { k, v ->
            binding.setVariable(k, v)
        }
        binding
    }

    protected void setDefaultBindingVariables(Binding binding, GroovyConsoleLogger log) {
        binding.setVariable('log', log)
        binding.setVariable('dataManager', dataManager)
        binding.setVariable('metadata', metadata)
        binding.setVariable('persistence', persistence)
    }

    protected Map<String, Object> getAdditionalBindingVariableMap() {
        [:]
    }
}