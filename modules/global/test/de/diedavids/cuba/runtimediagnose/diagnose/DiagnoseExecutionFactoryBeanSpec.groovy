package de.diedavids.cuba.runtimediagnose.diagnose

import spock.lang.Specification

class DiagnoseExecutionFactoryBeanSpec extends Specification {


    DiagnoseExecutionFactory sut
    private ZipFileHelper zipFileHelper

    def setup() {
        zipFileHelper = Mock(ZipFileHelper)
        sut = new DiagnoseExecutionFactoryBean(
                zipFileHelper: zipFileHelper
        )
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

    protected File loadFileFromTestDirectory(String filename) {
        new File(getClass().classLoader.getResource("de/diedavids/cuba/runtimediagnose/diagnose/testfile/$filename").file)
    }
}
