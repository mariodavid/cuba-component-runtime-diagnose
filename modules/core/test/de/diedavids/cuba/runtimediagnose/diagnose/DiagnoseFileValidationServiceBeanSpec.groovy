package de.diedavids.cuba.runtimediagnose.diagnose

import com.haulmont.cuba.core.global.Messages
import com.haulmont.cuba.core.global.Metadata
import com.haulmont.cuba.core.global.UuidSource
import de.diedavids.cuba.runtimediagnose.SpecificationWithApplicationContext
import de.diedavids.cuba.runtimediagnose.wizard.DiagnoseWizardResult
import de.diedavids.cuba.runtimediagnose.wizard.DiagnoseWizardResultType

class DiagnoseFileValidationServiceBeanSpec extends SpecificationWithApplicationContext {

    DiagnoseFileValidationService sut
    Metadata metadata

    @Override
    Map<Class, Object> getBeans() {
        [
                (Messages)  : Mock(Messages),
                (UuidSource): Mock(UuidSource),
        ]
    }

    def setup() {
        metadata = Mock(Metadata)
        sut = new DiagnoseFileValidationServiceBean(
                metadata: metadata
        )
    }

    def "validateDiagnose returns an error when the app name does not comply to the predefined value"() {

        given:
        def diagnoseExecution = new DiagnoseExecution(
                manifest: new DiagnoseManifest(
                        appName: "myApp"
                )
        )

        and:
        def diagnoseWizardResult = new DiagnoseWizardResult()
        metadata.create(DiagnoseWizardResult) >> diagnoseWizardResult

        when:
        def result = sut.validateDiagnose(diagnoseExecution)
        def appNameValidation = result[0]

        then:
        appNameValidation.type == DiagnoseWizardResultType.ERROR
    }

    def "validateDiagnose returns a success when the app name does comply to the predefined value"() {

        given:
        def diagnoseExecution = new DiagnoseExecution(
                manifest: new DiagnoseManifest(
                        appName: "runtime-diagnose-app"
                )
        )

        and:
        def expectedAppNameResult = new DiagnoseWizardResult()
        metadata.create(DiagnoseWizardResult) >>> [
                expectedAppNameResult,
                new DiagnoseWizardResult(),
                new DiagnoseWizardResult()
        ]

        when:
        def result = sut.validateDiagnose(diagnoseExecution)
        def actualAppNameResult = result[0]

        then:
        expectedAppNameResult == actualAppNameResult
        actualAppNameResult.type == DiagnoseWizardResultType.SUCCESS
    }

    def "validateDiagnose returns a error when the app version does comply to the predefined value"() {

        given:
        def diagnoseExecution = new DiagnoseExecution(
                manifest: new DiagnoseManifest(
                        appVersion: "1.1"
                )
        )

        and:
        def expectedAppVersionResult = new DiagnoseWizardResult()
        metadata.create(DiagnoseWizardResult) >>> [
                new DiagnoseWizardResult(),
                expectedAppVersionResult,
                new DiagnoseWizardResult()
        ]

        when:
        def result = sut.validateDiagnose(diagnoseExecution)
        def actualAppVersionResult = result[1]

        then:
        expectedAppVersionResult == actualAppVersionResult
        actualAppVersionResult.type == DiagnoseWizardResultType.ERROR
    }

    def "validateDiagnose returns a success when the app version does comply to the predefined value"() {

        given:
        def diagnoseExecution = new DiagnoseExecution(
                manifest: new DiagnoseManifest(
                        appVersion: "1.0"
                )
        )

        and:
        def expectedAppVersionResult = new DiagnoseWizardResult()
        metadata.create(DiagnoseWizardResult) >>> [
                new DiagnoseWizardResult(),
                expectedAppVersionResult,
                new DiagnoseWizardResult()
        ]

        when:
        def result = sut.validateDiagnose(diagnoseExecution)
        def actualAppVersionResult = result[1]

        then:
        expectedAppVersionResult == actualAppVersionResult
        actualAppVersionResult.type == DiagnoseWizardResultType.SUCCESS
    }

    def "validateDiagnose returns a error when the app producer does comply to the predefined value"() {

        given:
        def diagnoseExecution = new DiagnoseExecution(
                manifest: new DiagnoseManifest(
                        producer: "Wrong Company Ltd."
                )
        )

        and:
        def expectedAppProducerResult = new DiagnoseWizardResult()
        metadata.create(DiagnoseWizardResult) >>> [
                new DiagnoseWizardResult(),
                new DiagnoseWizardResult(),
                expectedAppProducerResult
        ]

        when:
        def result = sut.validateDiagnose(diagnoseExecution)
        def actualAppProducerResult = result[2]

        then:
        expectedAppProducerResult == actualAppProducerResult
        actualAppProducerResult.type == DiagnoseWizardResultType.ERROR
    }


    def "validateDiagnose returns a success when the app producer does comply to the predefined value"() {

        given:
        def diagnoseExecution = new DiagnoseExecution(
                manifest: new DiagnoseManifest(
                        producer: "Company Inc."
                )
        )

        and:
        def expectedAppProducerResult = new DiagnoseWizardResult()
        metadata.create(DiagnoseWizardResult) >>> [
                new DiagnoseWizardResult(),
                new DiagnoseWizardResult(),
                expectedAppProducerResult
        ]

        when:
        def result = sut.validateDiagnose(diagnoseExecution)
        def actualAppProducerResult = result[2]

        then:
        expectedAppProducerResult == actualAppProducerResult
        actualAppProducerResult.type == DiagnoseWizardResultType.SUCCESS
    }
}
