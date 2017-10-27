package de.diedavids.cuba.runtimediagnose.wizard

import com.haulmont.cuba.core.global.Messages
import spock.lang.Specification

class DiagnoseWizardResultSpec extends Specification {


    DiagnoseWizardResult sut
    private Messages messages


    def setup() {
        messages = Mock(Messages)
        sut = new MockableDiagnoseWizardResult(
                messages: messages
        )

    }

    def "getMessage uses the message pack if available"() {
        given:
        sut.messagePack = "de.diedavids"
        sut.messageCode = "helloWorld"

        and:
        messages.getMessage("de.diedavids", "helloWorld") >> "hello world"
        expect:
        sut.message == "hello world"
    }

    def "getMessage uses main message if the message pack is not given"() {
        given:
        sut.messagePack = null
        sut.messageCode = "helloWorld"

        and:
        messages.getMainMessage("helloWorld") >> "hello world"
        expect:
        sut.message == "hello world"
    }

    def "getMessage uses the explicitly given message text if no message code available"() {
        given:
        sut.message = "hello world"
        sut.messageCode = null

        expect:
        sut.message == "hello world"
    }

    def "getMessage returns nothing if everything is not set"() {
        given:
        sut.message = null
        sut.messageCode = null

        expect:
        !sut.message
    }
}
