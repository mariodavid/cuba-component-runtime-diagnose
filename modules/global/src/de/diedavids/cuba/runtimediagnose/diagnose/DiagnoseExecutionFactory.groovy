package de.diedavids.cuba.runtimediagnose.diagnose

interface DiagnoseExecutionFactory {

    public static final String NAME = 'ddcrd_DiagnoseExecutionFactory'


    DiagnoseExecution createDiagnoseExecutionFromFile(File file)
    DiagnoseExecution createAdHocDiagnoseExecution(String diagnoseScript, DiagnoseType diagnoseType)

    byte[] createExecutionResultFormDiagnoseExecution(DiagnoseExecution diagnoseExecution)

    byte[] createDiagnoseRequestFileFormDiagnoseExecution(DiagnoseExecution diagnoseExecution)


}
