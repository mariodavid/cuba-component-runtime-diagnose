package de.diedavids.cuba.runtimediagnose.diagnose

import groovy.transform.CompileStatic

@CompileStatic
class DiagnoseExecution implements Serializable {


    private static final long serialVersionUID = -8288852447591914153L

    static final String RESULT_STACKTRACE_NAME = 'stacktrace'
    static final String RESULT_LOG_NAME = 'log'
    static final String RESULT_NAME = 'result'

    Boolean executionSuccessful

    DiagnoseExecutionType executionType

    Date executionTimestamp

    String executionUser

    String diagnoseScript

    String executedDiagnoseScript

    Map<String, String> diagnoseResults = [:]

    DiagnoseManifest manifest

    String finalDiagnoseScript() {

        if (groovy) {
            executedDiagnoseScript ?: diagnoseScript
        }
        else {
            diagnoseScript
        }

    }
    boolean isGroovy() {
        manifest.diagnoseType == DiagnoseType.GROOVY
    }

    boolean isSQL() {
        manifest.diagnoseType == DiagnoseType.SQL
    }

    boolean isJPQL() {
        manifest.diagnoseType == DiagnoseType.JPQL
    }

    String getExecutedScriptFileExtension() {
        manifest ? manifest.diagnoseType.name().toLowerCase() : ''
    }

    Map<String, String> getExecutionResultFileMap() {

        def executionResultFileMap = [:] as Map<String, String>

        addResultFileIfPossible(executionResultFileMap, "diagnose.${executedScriptFileExtension}".toString(), finalDiagnoseScript())
        addResultFileIfPossible(executionResultFileMap, 'result.log', getResult(RESULT_NAME))
        addResultFileIfPossible(executionResultFileMap, 'log.log', getResult(RESULT_LOG_NAME))
        addResultFileIfPossible(executionResultFileMap, 'stacktrace.log', getResult(RESULT_STACKTRACE_NAME))

        executionResultFileMap
    }

    private void addResultFileIfPossible(Map<String, String> executionResultFileMap, String filename, String fileContent) {
        if (fileContent) {
            executionResultFileMap[filename] = fileContent
        }
    }

    void addResult(String fileName, Object fileContent) {
        diagnoseResults[fileName] = fileContent.toString()
    }

    String getResult(String fileName) {
        diagnoseResults[fileName]
    }

    boolean isExecuted() {
        executionTimestamp != null
    }
    boolean isPending() {
        !executed
    }


    void handleErrorExecution(Exception e) {
        StringWriter stacktrace = new StringWriter()
        e.printStackTrace(new PrintWriter(stacktrace))
        addResult(RESULT_STACKTRACE_NAME, stacktrace.toString())
        addResult(RESULT_NAME, e.message)
        executionSuccessful = false
    }

    void handleSuccessfulExecution(String result) {
        addResult(RESULT_NAME, result)
        executionSuccessful = true
    }

}
