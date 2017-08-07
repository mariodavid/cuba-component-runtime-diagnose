package de.diedavids.cuba.runtimediagnose.diagnose

import de.diedavids.cuba.runtimediagnose.entity.DiagnoseExecutionLog

interface DiagnoseExecutionLogFactory {

    public static final String NAME = 'ddcrd_DiagnoseExecutionLogFactory'

    DiagnoseExecutionLog create(DiagnoseExecution diagnoseExecution)

}
