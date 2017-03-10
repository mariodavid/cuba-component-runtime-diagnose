package de.diedavids.cuba.console.groovy

import com.haulmont.cuba.core.Persistence
import com.haulmont.cuba.core.global.DataManager
import com.haulmont.cuba.core.global.DatatypeFormatter
import com.haulmont.cuba.core.global.Metadata
import com.haulmont.cuba.core.global.Scripting
import com.haulmont.cuba.core.global.TimeSource
import com.haulmont.cuba.core.global.UserSessionSource
import de.diedavids.cuba.console.diagnose.DiagnoseExecution
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


    DiagnoseExecution runGroovyDiagnose(DiagnoseExecution diagnoseExecution) {
        if (diagnoseExecution) {
            def log = new GroovyConsoleLogger(timeSource: timeSource, datatypeFormatter: datatypeFormatter)
            Binding binding = createBinding(log)
            diagnoseExecution.executionTimestamp = timeSource.currentTimestamp()

            try {
                def result = scripting.evaluateGroovy(diagnoseExecution.diagnoseScript, binding)
                diagnoseExecution.handleSuccessfulExecution(result.toString())
            }

            catch (Exception e) {
                diagnoseExecution.handleErrorExecution(e)
            }
            diagnoseExecution.addResult('log', log.toString())

            diagnoseExecution
        }
    }

    private Binding createBinding(GroovyConsoleLogger log) {
        def binding = new Binding()
        binding.setVariable('log', log)
        binding.setVariable('dataManager', dataManager)
        binding.setVariable('metadata', metadata)
        binding.setVariable('persistence', persistence)
        binding
    }
}