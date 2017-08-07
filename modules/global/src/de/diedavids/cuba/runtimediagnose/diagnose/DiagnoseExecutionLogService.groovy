package de.diedavids.cuba.runtimediagnose.diagnose


interface DiagnoseExecutionLogService {
    String NAME = 'ddcrd_DiagnoseExecutionLogService'

    void logDiagnoseExecution(DiagnoseExecution diagnoseExecution)
}