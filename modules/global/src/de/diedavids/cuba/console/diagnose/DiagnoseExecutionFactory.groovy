package de.diedavids.cuba.console.diagnose

interface DiagnoseExecutionFactory {

    public static final String NAME = 'ddrd_DiagnoseExecutionFactory'


    DiagnoseExecution createDiagnoseExecutionFromFile(File file)
    DiagnoseExecution createAdHocDiagnoseExecution(String diagnoseScript, DiagnoseType diagnoseType)

    byte[] createExecutionResultFormDiagnoseExecution(DiagnoseExecution diagnoseExecution)

    byte[] createDiagnoseRequestFileFormDiagnoseExecution(DiagnoseExecution diagnoseExecution)


}
