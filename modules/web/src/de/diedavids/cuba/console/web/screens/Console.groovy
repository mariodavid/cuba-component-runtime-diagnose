package de.diedavids.cuba.console.web.screens

import com.haulmont.cuba.core.global.DataManager
import com.haulmont.cuba.core.global.Metadata
import com.haulmont.cuba.core.global.Scripting
import com.haulmont.cuba.gui.components.AbstractWindow
import com.haulmont.cuba.gui.components.SourceCodeEditor

import javax.inject.Inject

class Console extends AbstractWindow {


    @Inject
    SourceCodeEditor console

    @Inject
    SourceCodeEditor consoleResult
    @Inject
    SourceCodeEditor consoleResultLog


    @Inject
    Scripting scripting

    @Inject
    DataManager dataManager

    @Inject
    Metadata metadata

    void runConsole() {
        if (console.value) {

            def binding = new Binding()
            def log = new ConsoleLogger()
            binding.setVariable("log", log)
            binding.setVariable("dataManager", dataManager)
            binding.setVariable("metadata", metadata)
            def result = ""
            try {
                def consoleScript = console.value
                result = scripting.evaluateGroovy(consoleScript, binding)

            }
            catch (Throwable throwable) {
                StringWriter stacktrace = new StringWriter();
                throwable.printStackTrace(new PrintWriter(stacktrace));
                result = stacktrace.toString()
            }

            consoleResult.value = result.toString()
            consoleResultLog.value = log.toString()

        }

    }

    void clearConsole() {
        console.value = ""
    }

    void clearConsoleResult() {
        consoleResult.value = ""
        consoleResultLog.value = ""
    }
}