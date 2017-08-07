package de.diedavids.cuba.runtimediagnose.diagnose

import com.haulmont.cuba.core.global.BuildInfo
import spock.lang.Specification

class DiagnoseExecutionFactoryBeanSpec extends Specification {


    DiagnoseExecutionFactory sut
    ZipFileHelper zipFileHelper
    BuildInfo buildInfo
    BuildInfo.Content buildInfoContent

    def setup() {
        zipFileHelper = Mock(ZipFileHelper)
        buildInfo = Mock(BuildInfo)
        sut = new DiagnoseExecutionFactoryBean(
                zipFileHelper: zipFileHelper,
                buildInfo: buildInfo
        )

        buildInfoContent = Mock(BuildInfo.Content)
        buildInfo.getContent() >> buildInfoContent

    }

    def "createAdHocDiagnoseExecution sets the diagnose type and the execution script"() {
        given:
        def diagnoseType = DiagnoseType.GROOVY
        def executionScript = "1 + 2"
        when:
        DiagnoseExecution result = sut.createAdHocDiagnoseExecution(executionScript, diagnoseType)
        then:
        result.manifest.diagnoseType == diagnoseType
        result.diagnoseScript == executionScript
        result.executionType == DiagnoseExecutionType.CONSOLE
    }

    def "createAdHocDiagnoseExecution sets the metadata from the application"() {
        given:
        buildInfoContent.getAppName() >> 'my-app'
        buildInfoContent.getVersion() >> '1.0'
        when:
        DiagnoseExecution result = sut.createAdHocDiagnoseExecution(null, null)
        then:
        result.manifest.appName == 'my-app'
        result.manifest.appVersion == '1.0'
    }

    def "createDiagnoseExecutionFromFile reads the execution script from the diagnose.sql file in the zip archive"() {
        given:
        File diagnoseFile = loadFileFromTestDirectory('sql-diagnose-wizard.zip')

        and: "the manifest file is returned directly"
        File manifestFile = loadFileFromTestDirectory('manifest-sql.json')
        zipFileHelper.readFileFromArchive('manifest.json', _) >> new FileInputStream(manifestFile)

        when:
        def result = sut.createDiagnoseExecutionFromFile(diagnoseFile)

        then:
        result.manifest.diagnoseType == DiagnoseType.SQL
    }

    def "createDiagnoseExecutionFromFile reads the manifest information from the manifest.json file in the zip archive"() {
        given:
        File diagnoseFile = loadFileFromTestDirectory('sql-diagnose-wizard.zip')

        and: "the manifest file is returned directly"
        File manifestFile = loadFileFromTestDirectory('manifest-sql.json')
        zipFileHelper.readFileFromArchive('manifest.json', _) >> new FileInputStream(manifestFile)

        and:  "the diagnose script file is returned directly"
        File diagnoseScriptFile = loadFileFromTestDirectory('diagnose.sql')
        zipFileHelper.readFileContentFromArchive('diagnose.sql', _) >> diagnoseScriptFile.text

        when:
        def result = sut.createDiagnoseExecutionFromFile(diagnoseFile)

        then:
        result.diagnoseScript == "SELECT * FROM SEC_USER;"
    }

    def "createDiagnoseExecutionFromFile sets the executionType to 'wizard'"() {

        given:
        File diagnoseFile = loadFileFromTestDirectory('sql-diagnose-wizard.zip')

        when:
        def result = sut.createDiagnoseExecutionFromFile(diagnoseFile)

        then:
        result.executionType == DiagnoseExecutionType.WIZARD
    }

    def "createDiagnoseExecutionFromFile creates an empty manifest if the manifest information could not be found"() {
        given:
        File diagnoseFile = loadFileFromTestDirectory('sql-diagnose-wizard.zip')

        and: "the manifest file is not returned"
        zipFileHelper.readFileFromArchive('manifest.json', _) >> null

        when:
        def result = sut.createDiagnoseExecutionFromFile(diagnoseFile)

        then:
        !result.manifest
    }


    def "createExecutionResultFormDiagnoseExecution uses the zipFileHelper and uses the executionResultFileMap as the ZipFile content"() {
        given:
        def diagnoseExecution = new DiagnoseExecution(
                manifest: new DiagnoseManifest(
                        diagnoseType: DiagnoseType.GROOVY
                ),
                diagnoseScript: "4+5"
        )

        and: 'result.log gets appended'
        diagnoseExecution.addResult(DiagnoseExecution.RESULT_NAME, '9')

        and: 'log.log gets appended'
        diagnoseExecution.addResult(DiagnoseExecution.RESULT_LOG_NAME, 'log entries')

        when:
        sut.createExecutionResultFromDiagnoseExecution(diagnoseExecution)

        then:
        1 * zipFileHelper.createZipFileForEntries([
                'diagnose.groovy': '4+5',
                'result.log': '9',
                'log.log': 'log entries'
        ])
    }

    def "createDiagnoseRequestFileFormDiagnoseExecution creates a zip file containing the diagnose script and the manifest"() {
        given:
        def diagnoseExecution = new DiagnoseExecution(
                manifest: new DiagnoseManifest(
                        diagnoseType: DiagnoseType.GROOVY
                ),
                diagnoseScript: "4+5"
        )

        when:
        sut.createDiagnoseRequestFileFromDiagnoseExecution(diagnoseExecution)

        then:
        1 * zipFileHelper.createZipFileForEntries({
            it['diagnose.groovy'] == '4+5' &&
                    it['manifest.json'].contains("GROOVY")

        })
    }


    protected File loadFileFromTestDirectory(String filename) {
        new File(getClass().classLoader.getResource("de/diedavids/cuba/runtimediagnose/diagnose/testfile/$filename").file)
    }
}
