package de.diedavids.cuba.console.diagnose

import spock.lang.Specification

class DiagnoseExecutionSpec extends Specification {


    DiagnoseExecution sut

    def setup() {

        sut = new DiagnoseExecution(
                manifest: new DiagnoseManifest(diagnoseType: DiagnoseType.GROOVY)
        )
    }

    def "isGroovy returns true if the type is Groovy"() {
        given:
        sut.manifest = new DiagnoseManifest(diagnoseType: DiagnoseType.GROOVY)
        expect:
        sut.isGroovy()
    }

    def "isSQL returns true if the type is SQL"() {
        given:
        sut.manifest = new DiagnoseManifest(diagnoseType: DiagnoseType.SQL)
        expect:
        sut.isSQL()
    }

    def "getExecutedScriptFileExtension returns sql for SQL"() {
        given:
        sut.manifest = new DiagnoseManifest(diagnoseType: DiagnoseType.SQL)
        expect:
        sut.executedScriptFileExtension == "sql"
    }

    def "getExecutedScriptFileExtension returns groovy for GROOVY"() {
        given:
        sut.manifest = new DiagnoseManifest(diagnoseType: DiagnoseType.GROOVY)
        expect:
        sut.executedScriptFileExtension == "groovy"
    }

    def "isExecuted is true when there is an execution timestamp"() {
        given:
        sut.executionTimestamp = new Date()
        expect:
        sut.isExecuted()
    }

    def "isExecuted is false when there is no execution timestamp"() {
        given:
        sut.executionTimestamp = null
        expect:
        !sut.isExecuted()
    }

    def "isPending is false when there is an execution timestamp"() {
        given:
        sut.executionTimestamp = new Date()
        expect:
        !sut.isPending()
    }

    def "isPending is true when there is no execution timestamp"() {
        given:
        sut.executionTimestamp = null
        expect:
        sut.isPending()
    }

    def "accessor methods for results work as expected"() {
        given:
        sut.addResult("result", "fileContent")
        expect:
        sut.getResult("result") == "fileContent"
    }

    def "getResult for a non existing result will return null"() {
        given:
        sut.addResult("result", "fileContent")
        expect:
        !sut.getResult("otherResult")
    }

    def "getExecutionResultFileMap will create a file for diagnose script"() {
        given:
        sut.manifest = new DiagnoseManifest(diagnoseType: DiagnoseType.GROOVY)
        sut.diagnoseScript = "5 + 10"

        expect:
        sut.executionResultFileMap == ["diagnose.groovy":"5 + 10"]
    }

    def "getExecutionResultFileMap will create a file for the result"() {
        given:
        sut.addResult("result", "fileContent")

        expect:
        sut.executionResultFileMap == ["result.log":"fileContent"]
    }

    def "getExecutionResultFileMap will create a file for the log"() {
        given:
        sut.addResult("log", "fileContent")

        expect:
        sut.executionResultFileMap == ["log.log":"fileContent"]
    }

    def "getExecutionResultFileMap will create a file for the stacktrace"() {
        given:
        sut.addResult("stacktrace", "fileContent")

        expect:
        sut.executionResultFileMap == ["stacktrace.log":"fileContent"]
    }

    def "getExecutionResultFileMap will leave out files with empty content"() {
        given:
        sut.addResult("result", "fileContent")

        and: "log result is not present"
        sut.diagnoseResults.remove("log")
        expect:
        !sut.executionResultFileMap.containsKey("log.log")
        sut.executionResultFileMap.containsKey("result.log")
    }

    def "handleErrorExecution sets the successful execution to false"() {

        given:
        sut.executionSuccessful = true
        when:
        sut.handleErrorExecution(new RuntimeException("my exception message"))
        then:
        !sut.executionSuccessful
    }

    def "handleErrorExecution adds the stacktrace to the corresponding result"() {

        when:
        sut.handleErrorExecution(new RuntimeException("my exception message"))
        then:
        sut.getResult('stacktrace').contains "java.lang.RuntimeException: my exception message"
    }

    def "handleErrorExecution sets the exception message as the result"() {

        when:
        sut.handleErrorExecution(new RuntimeException("my exception message"))
        then:
        sut.getResult('result') == "my exception message"
    }



    def "handleSuccessfulExecution sets the successful execution to true"() {

        given:
        sut.executionSuccessful = false
        when:
        sut.handleSuccessfulExecution("result")
        then:
        sut.executionSuccessful
    }

    def "handleSuccessfulExecution sets the result string"() {

        when:
        sut.handleSuccessfulExecution("my Result")
        then:
        sut.getResult('result') == "my Result"
    }
}
