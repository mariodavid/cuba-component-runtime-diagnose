package de.diedavids.cuba.console.groovy

import com.haulmont.cuba.core.global.DatatypeFormatter
import com.haulmont.cuba.core.global.TimeSource
import de.diedavids.cuba.console.groovy.GroovyConsoleLogger
import spock.lang.Specification
import spock.lang.Unroll

class GroovyConsoleLoggerSpec extends Specification {

    GroovyConsoleLogger logger
    private TimeSource timeSource
    private DatatypeFormatter datatypeFormatter

    def setup() {
        timeSource = Mock(TimeSource)
        datatypeFormatter = Mock(DatatypeFormatter)

        logger = new GroovyConsoleLogger(
                timeSource: timeSource,
                datatypeFormatter: datatypeFormatter
        )
    }

    @Unroll
    def "#level adds [#tag] before the message"() {

        when:
        logger."$level"("")

        then:
        logger.toString().contains "[$tag]"

        where:
        level   || tag
        "debug" || "DEBUG"
        "warn"  || "WARN"
        "error" || "ERROR"
    }

    def "append puts the content to the logger"() {

        when:
        logger.append("level", "myMessage")

        then:
        logger.toString().contains "myMessage"

    }

    def "append puts the current timestamp to the logger"() {

        given:
        def now = new Date()
        timeSource.currentTimestamp() >> now

        def timestampString = "2000-01-01 00:00"
        datatypeFormatter.formatDateTime(now) >> timestampString

        when:
        logger.append("level", "myMessage")

        then:
        logger.toString().contains "[$timestampString]"

    }
}
