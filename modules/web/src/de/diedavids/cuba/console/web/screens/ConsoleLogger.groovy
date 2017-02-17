package de.diedavids.cuba.console.web.screens

class ConsoleLogger {


    StringBuilder result = new StringBuilder()


    void debug(Object content) {
        append("DEBUG: $content")
    }

    protected StringBuilder append(String content) {
        result << content << "\n"
    }

    void warn(Object content) {
        append("WARN: $content")
    }

    void error(Object content) {
        append("ERROR: $content")
    }

    @Override
    String toString() {
        result
    }
}