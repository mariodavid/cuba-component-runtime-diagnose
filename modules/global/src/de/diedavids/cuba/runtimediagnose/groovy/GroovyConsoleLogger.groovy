package de.diedavids.cuba.runtimediagnose.groovy

import com.haulmont.cuba.core.global.DatatypeFormatter
import com.haulmont.cuba.core.global.TimeSource

class GroovyConsoleLogger {


    StringBuilder result = new StringBuilder()

    TimeSource timeSource

    DatatypeFormatter datatypeFormatter

    void debug(Object content) {
        append('DEBUG', content)
    }

    void append(String level, Object content) {
        def time = datatypeFormatter.formatDateTime(timeSource.currentTimestamp())
        result << "[$time]\t[$level]\t$content\n"
    }

    void warn(Object content) {
        append('WARN', content)
    }

    void error(Object content) {
        append('ERROR', content)
    }

    @Override
    String toString() {
        result
    }
}