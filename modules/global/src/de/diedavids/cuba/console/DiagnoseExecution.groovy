package de.diedavids.cuba.console

class DiagnoseExecution implements Serializable{

    Boolean executionSuccessful

    Date executionTimestamp

    String executionUser

    String diagnoseScript

    Map<String, String> diagnoseResults = [:]

    DiagnoseManifest manifest


    boolean isGroovy() {
        manifest.diagnoseType == DiagnoseType.GROOVY
    }

    boolean isSQL() {
        manifest.diagnoseType == DiagnoseType.SQL
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


}
