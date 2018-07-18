package de.diedavids.cuba.runtimediagnose.groovy

import com.haulmont.cuba.core.Persistence
import com.haulmont.cuba.core.global.DataManager
import com.haulmont.cuba.core.global.DatatypeFormatter
import com.haulmont.cuba.core.global.Metadata
import com.haulmont.cuba.core.global.Scripting
import com.haulmont.cuba.core.global.TimeSource
import com.haulmont.cuba.core.global.UserSessionSource
import com.haulmont.cuba.security.entity.User
import com.haulmont.cuba.security.global.UserSession
import de.diedavids.cuba.runtimediagnose.RuntimeDiagnoseConfiguration
import de.diedavids.cuba.runtimediagnose.diagnose.DiagnoseExecution
import de.diedavids.cuba.runtimediagnose.diagnose.DiagnoseExecutionLogService
import spock.lang.Specification

class GroovyDiagnoseServiceBeanSpec extends Specification {

    GroovyDiagnoseService service
    private Scripting scripting
    private DataManager dataManager
    private Metadata metadata
    private DatatypeFormatter datatypeFormatter
    private TimeSource timeSource
    private Persistence persistence
    private UserSessionSource userSessionSource
    private DiagnoseExecutionLogService diagnoseExecutionLogService

    def setup() {
        scripting = Mock(Scripting)
        dataManager = Mock(DataManager)
        metadata = Mock(Metadata)
        datatypeFormatter = Mock(DatatypeFormatter)
        timeSource = Mock(TimeSource)
        persistence = Mock(Persistence)
        userSessionSource = Mock(UserSessionSource)
        def userSession = Mock(UserSession)
        userSession.getCurrentOrSubstitutedUser() >> new User(login: "admin")
        userSessionSource.getUserSession() >> userSession
        diagnoseExecutionLogService = Mock(DiagnoseExecutionLogService)
        service = new GroovyDiagnoseServiceBean(
                scripting: scripting,
                dataManager: dataManager,
                metadata: metadata,
                datatypeFormatter: datatypeFormatter,
                timeSource: timeSource,
                persistence: persistence,
                userSessionSource: userSessionSource,
                diagnoseExecutionLogService: diagnoseExecutionLogService
        )
    }

    def "RunGroovyDiagnose will not run anything if there is no diagnose execution information"() {

        when:
        service.runGroovyDiagnose(null)

        then:
        0 * scripting.evaluateGroovy(_,_)
    }

    def "RunGroovyDiagnose uses the diagnoseScript from diaglogExecution for script execution"() {

        given:
        def diagnoseExecution = new DiagnoseExecution(diagnoseScript: "println 'hello world'")

        when:
        service.runGroovyDiagnose(diagnoseExecution)

        then:
        1 * scripting.evaluateGroovy("println 'hello world'",_)
    }


    def "RunGroovyDiagnose creates a binding with an instance of GroovyConsoleLogger"() {

        given:
        def diagnoseExecution = new DiagnoseExecution()

        when:
        service.runGroovyDiagnose(diagnoseExecution)

        then:
        1 * scripting.evaluateGroovy(_,{
                    def log = it.getVariable('log')
                    log instanceof GroovyConsoleLogger &&
                            log.datatypeFormatter == datatypeFormatter &&
                            log.timeSource == timeSource
        })
    }

    def "RunGroovyDiagnose marks the diagnoseExecution as successful if there is no exception during script execution"() {

        given:
        def diagnoseExecution = new DiagnoseExecution()

        and:
        scripting.evaluateGroovy(_,_) >> true

        when:
        service.runGroovyDiagnose(diagnoseExecution)

        then:
        diagnoseExecution.executionSuccessful
    }

    def "RunGroovyDiagnose sets the result of the execution in the result key of the diagnoseExecution"() {

        given:
        def diagnoseExecution = new DiagnoseExecution()

        and:
        scripting.evaluateGroovy(_,_) >> "this is the result"

        when:
        service.runGroovyDiagnose(diagnoseExecution)

        then:
        diagnoseExecution.getResult('result') == "this is the result"
    }



    def "RunGroovyDiagnose marks the diagnoseExecution as failure if an exception occurs during script execution"() {

        given:
        def diagnoseExecution = new DiagnoseExecution()

        and:
        scripting.evaluateGroovy(_,_) >> { throw new RuntimeException("didn't work out that well") }

        when:
        service.runGroovyDiagnose(diagnoseExecution)

        then:
        !diagnoseExecution.executionSuccessful
    }


    def "RunGroovyDiagnose puts the message of the exception to the diagnose result"() {

        given:
        def diagnoseExecution = new DiagnoseExecution()

        and:
        scripting.evaluateGroovy(_,_) >> { throw new RuntimeException("exceptionMessage")}

        when:
        service.runGroovyDiagnose(diagnoseExecution)

        then:
        diagnoseExecution.getResult('result') == "exceptionMessage"
    }

    def "RunGroovyDiagnose puts the stacktrace of the exception to the diagnose result"() {

        given:
        def diagnoseExecution = new DiagnoseExecution()

        and:
        scripting.evaluateGroovy(_,_) >> { throw new RuntimeException("exceptionMessage")}

        when:
        service.runGroovyDiagnose(diagnoseExecution)
        def stacktraceResult = diagnoseExecution.getResult('stacktrace')
        then:
        stacktraceResult.contains "java.lang.RuntimeException: exceptionMessage"
        stacktraceResult.contains "at de.diedavids.cuba.runtimediagnose.groovy.GroovyDiagnoseServiceBeanSpec"
    }

    def "RunGroovyDiagnose puts the log information in the execution result"() {

        given:
        def diagnoseExecution = new DiagnoseExecution()

        and:
        scripting.evaluateGroovy(_,_) >> { String script, Binding binding ->
            binding.getVariable('log').debug("log entry in debug")
        }

        when:
        service.runGroovyDiagnose(diagnoseExecution)
        def logResult = diagnoseExecution.getResult('log')

        then:
        logResult.contains "log entry in debug"
    }

    def "RunGroovyDiagnose logs the execution"() {

        given:
        def diagnoseExecution = new DiagnoseExecution()

        when:
        service.runGroovyDiagnose(diagnoseExecution)

        then:
        1 * diagnoseExecutionLogService.logDiagnoseExecution(diagnoseExecution)
    }

    def "RunGroovyDiagnose returns the updated diagnoseExecution"() {

        given:
        def diagnoseExecution = new DiagnoseExecution()

        expect:
        service.runGroovyDiagnose(diagnoseExecution) == diagnoseExecution

    }


}
