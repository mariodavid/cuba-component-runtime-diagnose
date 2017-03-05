package de.diedavids.cuba.console.service

import com.haulmont.cuba.core.Persistence
import com.haulmont.cuba.core.global.DataManager
import com.haulmont.cuba.core.global.DatatypeFormatter
import com.haulmont.cuba.core.global.Metadata
import com.haulmont.cuba.core.global.Scripting
import com.haulmont.cuba.core.global.TimeSource
import com.haulmont.cuba.core.global.UserSessionSource
import com.haulmont.cuba.security.global.UserSession
import de.diedavids.cuba.console.DiagnoseExecution
import de.diedavids.cuba.console.GroovyConsoleLogger
import org.springframework.stereotype.Service

import javax.inject.Inject

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

            def log = new GroovyConsoleLogger(
                    timeSource: timeSource,
                    datatypeFormatter: datatypeFormatter
            )
            Binding binding = createBinding(log)
            try {
                def result = scripting.evaluateGroovy(diagnoseExecution.diagnoseScript, binding)
                diagnoseExecution.addResult('result',result)
                diagnoseExecution.executionSuccessful = true
            }
            catch (Throwable throwable) {
                StringWriter stacktrace = new StringWriter();
                throwable.printStackTrace(new PrintWriter(stacktrace));
                diagnoseExecution.addResult('stacktrace', stacktrace.toString())
                diagnoseExecution.addResult('result', throwable.message)
                diagnoseExecution.executionSuccessful = false
            }

            diagnoseExecution.addResult('log', log.toString())

            diagnoseExecution

        }

    }

    private Binding createBinding(GroovyConsoleLogger log) {
        def binding = new Binding()
        binding.setVariable("log", log)
        binding.setVariable("dataManager", dataManager)
        binding.setVariable("metadata", metadata)
        binding.setVariable("persistence", persistence)
        binding
    }
}