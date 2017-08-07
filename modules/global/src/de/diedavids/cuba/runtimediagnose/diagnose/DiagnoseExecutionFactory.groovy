package de.diedavids.cuba.runtimediagnose.diagnose

interface DiagnoseExecutionFactory {

    public static final String NAME = 'ddcrd_DiagnoseExecutionFactory'

    /**
     * creates a diagnose execution from a File
     * @param file the file to read from (zip file)
     * @return the parsed diagnose execution information
     */
    DiagnoseExecution createDiagnoseExecutionFromFile(File file)

    /**
     * creates an ad-hoc diagnose execution
     * @param diagnoseScript the diagnose script to use
     * @param diagnoseType the diagnose type to use
     * @return the diagnose execution instance
     */
    DiagnoseExecution createAdHocDiagnoseExecution(String diagnoseScript, DiagnoseType diagnoseType)


    /**
     * creates an execution result (as zip bytes) form a given diagnose exection
     * @param diagnoseExecution the diagnose execution to create a zip file from
     * @return the execution result (as zip bytes)
     */
    byte[] createExecutionResultFromDiagnoseExecution(DiagnoseExecution diagnoseExecution)

    /**
     * creates a diagnose request file (as zip bytes) form a given diagnose exection
     * @param diagnoseExecution the diagnose execution to create a zip file from
     * @return the diagnose request (as zip bytes)
     */
    byte[] createDiagnoseRequestFileFromDiagnoseExecution(DiagnoseExecution diagnoseExecution)


}
