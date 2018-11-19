package de.diedavids.cuba.runtimediagnose.web.screens.sql


import de.diedavids.cuba.runtimediagnose.diagnose.DiagnoseType
import de.diedavids.cuba.runtimediagnose.web.screens.console.ConsoleWindow

class SqlConsole extends ConsoleWindow {

    @Override
    DiagnoseType getDiagnoseType() {
        return DiagnoseType.SQL
    }
}