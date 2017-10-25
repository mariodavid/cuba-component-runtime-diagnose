package de.diedavids.cuba.runtimediagnose.diagnose

import com.haulmont.cuba.core.global.Messages
import com.haulmont.cuba.core.global.Metadata
import com.haulmont.cuba.core.global.UuidSource
import de.diedavids.cuba.runtimediagnose.SpecificationWithApplicationContext
import de.diedavids.cuba.runtimediagnose.wizard.DiagnoseWizardResult
import de.diedavids.cuba.runtimediagnose.wizard.DiagnoseWizardResultType

class DiagnoseWizardResultServiceBeanSpec extends SpecificationWithApplicationContext {


    DiagnoseWizardResultServiceBean sut
    Messages messages
    Metadata metadata

    @Override
    Map<Class, Object> getBeans() {

        [
                (UuidSource.class): Mock(UuidSource)
        ]
    }

    def setup() {

        messages = Mock(Messages)
        metadata = Mock(Metadata)

        sut = new DiagnoseWizardResultServiceBean(
            messages: messages,
            metadata: metadata
        )

        metadata.create(DiagnoseWizardResult) >> new DiagnoseWizardResult()
    }

    def "createResultsForDiagnose returns a success wizardResult in case the execution was sucessful"() {

        given:
        def diagnose = new DiagnoseExecution(
            executionSuccessful: true
        )

        and:
        messages.getMessage(DiagnoseWizardResultServiceBean, 'diagnoseExecutedSuccessful') >> "worked out"
        when:
        def results = sut.createResultsForDiagnose(diagnose)
        then:
        results[0].type == DiagnoseWizardResultType.SUCCESS
        results[0].message == 'worked out'
    }

    def "createResultsForDiagnose returns a error wizardResult in case the execution was not sucessful"() {

        given:
        def diagnose = new DiagnoseExecution(
            executionSuccessful: false
        )

        and:
        messages.getMessage(DiagnoseWizardResultServiceBean, 'diagnoseExecutedError') >> "didn't work"
        when:
        def results = sut.createResultsForDiagnose(diagnose)
        then:
        results[0].type == DiagnoseWizardResultType.ERROR
        results[0].message == "didn't work"
    }

    def "createResultsForDiagnose returns a error wizardResult in case the execution is not set"() {

        given:
        def diagnose = new DiagnoseExecution(
            executionSuccessful: null
        )
        and:
        messages.getMessage(DiagnoseWizardResultServiceBean, 'diagnoseExecutedError') >> "didn't work"
        when:
        def results = sut.createResultsForDiagnose(diagnose)
        then:
        results[0].type == DiagnoseWizardResultType.ERROR
        results[0].message == "didn't work"
    }
}