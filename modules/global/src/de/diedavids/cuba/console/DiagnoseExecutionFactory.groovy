package de.diedavids.cuba.console

interface DiagnoseExecutionFactory {

    public static final String NAME = 'console_DiagnoseExecutionFactory'


    DiagnoseExecution createDiagnoseExecutionFromFile(File file)
    DiagnoseExecution createAdHocDiagnoseExecution(String diagnoseScript)

    byte[] createExecutionResultFormDiagnoseExecution(DiagnoseExecution diagnoseExecution)


}
