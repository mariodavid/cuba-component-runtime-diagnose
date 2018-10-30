package de.diedavids.cuba.runtimediagnose.diagnose

import com.haulmont.cuba.core.global.BuildInfo
import com.haulmont.cuba.core.global.BuildInfo.Content
import com.haulmont.cuba.core.global.Messages
import com.haulmont.cuba.core.global.Metadata
import com.haulmont.cuba.core.global.UuidSource
import de.diedavids.cuba.runtimediagnose.SpecificationWithApplicationContext
import de.diedavids.cuba.runtimediagnose.wizard.DiagnoseWizardResult
import de.diedavids.cuba.runtimediagnose.wizard.DiagnoseWizardResultType

class DiagnoseFileValidationServiceBeanSpec extends SpecificationWithApplicationContext {

    DiagnoseFileValidationService sut
    Metadata metadata
    BuildInfo buildInfo
    Content buildInfoContent

    @Override
    Map<Class, Object> getBeans() {
        [
                (Messages)  : Mock(Messages),
                (UuidSource): Mock(UuidSource),
        ]
    }

    def setup() {
        metadata = Mock(Metadata)
        buildInfo = Mock(BuildInfo)
        sut = new DiagnoseFileValidationServiceBean(
                metadata: metadata,
                buildInfo: buildInfo
        )

        buildInfoContent = Mock(Content)
        buildInfo.getContent() >> buildInfoContent
    }

    def "validateDiagnose returns a warning when the app name does not comply to the predefined value"() {

        given:
        def diagnoseExecution = new DiagnoseExecution(
                manifest: new DiagnoseManifest(
                        appName: "my-wrong-name-app"
                )
        )

        and:
        buildInfoContent.getAppName() >> "my-app"

        and:
        def expectedAppNameResult = new DiagnoseWizardResult()
        metadata.create(DiagnoseWizardResult) >>> [
                expectedAppNameResult,
                new DiagnoseWizardResult()
        ]

        when:
        def result = sut.validateDiagnose(diagnoseExecution)
        def appNameValidation = result[0]

        then:
        appNameValidation.type == DiagnoseWizardResultType.WARNING
    }

    def "validateDiagnose returns a success when the app name does comply to the predefined value"() {

        given:
        def diagnoseExecution = new DiagnoseExecution(
                manifest: new DiagnoseManifest(
                        appName: "my-app"
                )
        )

        and:
        def expectedAppNameResult = new DiagnoseWizardResult()
        metadata.create(DiagnoseWizardResult) >>> [
                expectedAppNameResult,
                new DiagnoseWizardResult()
        ]

        and:
        buildInfoContent.getAppName() >> "my-app"

        when:
        def result = sut.validateDiagnose(diagnoseExecution)
        def actualAppNameResult = result[0]

        then:
        expectedAppNameResult == actualAppNameResult
        actualAppNameResult.type == DiagnoseWizardResultType.SUCCESS
    }

    def "validateDiagnose returns a warning when the app version does comply to the predefined value"() {

        given:
        def diagnoseExecution = new DiagnoseExecution(
                manifest: new DiagnoseManifest(
                        appVersion: "1.1"
                )
        )


        and:
        buildInfoContent.getVersion() >> "1.2"


        and:
        def expectedAppVersionResult = new DiagnoseWizardResult()
        metadata.create(DiagnoseWizardResult) >>> [
                new DiagnoseWizardResult(),
                expectedAppVersionResult
        ]

        when:
        def result = sut.validateDiagnose(diagnoseExecution)
        def actualAppVersionResult = result[1]

        then:
        expectedAppVersionResult == actualAppVersionResult
        actualAppVersionResult.type == DiagnoseWizardResultType.WARNING
    }

    def "validateDiagnose returns a success when the app version does comply to the predefined value"() {

        given:
        def diagnoseExecution = new DiagnoseExecution(
                manifest: new DiagnoseManifest(
                        appVersion: "1.0"
                )
        )


        and:
        buildInfoContent.getVersion() >> "1.0"

        and:
        def expectedAppVersionResult = new DiagnoseWizardResult()
        metadata.create(DiagnoseWizardResult) >>> [
                new DiagnoseWizardResult(),
                expectedAppVersionResult
        ]

        when:
        def result = sut.validateDiagnose(diagnoseExecution)
        def actualAppVersionResult = result[1]

        then:
        expectedAppVersionResult == actualAppVersionResult
        actualAppVersionResult.type == DiagnoseWizardResultType.SUCCESS
    }

}
