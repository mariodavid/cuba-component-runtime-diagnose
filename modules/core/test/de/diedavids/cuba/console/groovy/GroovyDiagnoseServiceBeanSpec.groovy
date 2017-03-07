package de.diedavids.cuba.console.groovy

import com.haulmont.cuba.core.Persistence
import com.haulmont.cuba.core.global.DataManager
import com.haulmont.cuba.core.global.DatatypeFormatter
import com.haulmont.cuba.core.global.Metadata
import com.haulmont.cuba.core.global.Scripting
import com.haulmont.cuba.core.global.TimeSource
import com.haulmont.cuba.core.global.UserSessionSource
import de.diedavids.cuba.console.diagnose.DiagnoseExecution
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

    def setup() {
        scripting = Mock(Scripting)
        dataManager = Mock(DataManager)
        metadata = Mock(Metadata)
        datatypeFormatter = Mock(DatatypeFormatter)
        timeSource = Mock(TimeSource)
        persistence = Mock(Persistence)
        userSessionSource = Mock(UserSessionSource)
        service = new GroovyDiagnoseServiceBean(
                scripting: scripting,
                dataManager: dataManager,
                metadata: metadata,
                datatypeFormatter: datatypeFormatter,
                timeSource: timeSource,
                persistence: persistence,
                userSessionSource: userSessionSource,
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

    def "RunGroovyDiagnose creates a binding with an instance of common variables"() {

        given:
        def diagnoseExecution = new DiagnoseExecution()

        when:
        service.runGroovyDiagnose(diagnoseExecution)

        then:
        1 * scripting.evaluateGroovy(_,{
            it instanceof Binding &&
                    it.getVariable('log') instanceof GroovyConsoleLogger &&
                    it.getVariable('dataManager') == dataManager &&
                    it.getVariable('metadata') == metadata &&
                    it.getVariable('persistence') == persistence
        })
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
        stacktraceResult.contains "at de.diedavids.cuba.console.groovy.GroovyDiagnoseServiceBeanSpec"
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
    def "RunGroovyDiagnose returns the updated diagnoseExecution"() {

        given:
        def diagnoseExecution = new DiagnoseExecution()

        expect:
        service.runGroovyDiagnose(diagnoseExecution) == diagnoseExecution

    }


}
