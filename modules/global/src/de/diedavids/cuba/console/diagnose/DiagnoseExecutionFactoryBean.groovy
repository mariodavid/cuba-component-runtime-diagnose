package de.diedavids.cuba.console.diagnose

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.springframework.stereotype.Component

import javax.inject.Inject
import java.util.zip.ZipFile

@Component(DiagnoseExecutionFactory.NAME)
class DiagnoseExecutionFactoryBean implements DiagnoseExecutionFactory {

    public static final String MANIFEST_FILENAME = 'manifest.json'

    @Inject
    ZipFileHelper zipFileHelper


    DiagnoseExecution createDiagnoseExecutionFromFile(File file) {
        def diagnoseZipFile = new ZipFile(file)

        def result = new DiagnoseExecution()
        result.manifest = createManifestFromDiagnoseFile(diagnoseZipFile)
        result.diagnoseScript = readDiagnoseScriptFromDiagnoseFile(result, diagnoseZipFile)
        result

    }

    @Override
    DiagnoseExecution createAdHocDiagnoseExecution(String diagnoseScript, DiagnoseType diagnoseType) {

        def result = new DiagnoseExecution(manifest: new DiagnoseManifest(diagnoseType: diagnoseType))
        result.diagnoseScript = diagnoseScript
        result
    }

    private String readDiagnoseScriptFromDiagnoseFile(DiagnoseExecution diagnoseExecution, ZipFile diagnoseZipFile) {
            zipFileHelper.readFileContentFromArchive(getDiagnoseScriptFilename(diagnoseExecution), diagnoseZipFile)
    }

    private DiagnoseManifest createManifestFromDiagnoseFile(ZipFile diagnoseZipFile) {
        def manifestJson = new JsonSlurper().parse(zipFileHelper.readFileFromArchive(MANIFEST_FILENAME, diagnoseZipFile))
        manifestJson as DiagnoseManifest
    }


    byte[] createExecutionResultFormDiagnoseExecution(DiagnoseExecution diagnoseExecution) {
        zipFileHelper.createZipFileForEntries(diagnoseExecution.executionResultFileMap)
    }

    @Override
    byte[] createDiagnoseRequestFileFormDiagnoseExecution(DiagnoseExecution diagnoseExecution) {
        def files = [
                (getDiagnoseScriptFilename(diagnoseExecution)): diagnoseExecution.diagnoseScript,
                'manifest.json'                             : JsonOutput.prettyPrint(JsonOutput.toJson(diagnoseExecution.manifest)),
        ]
        zipFileHelper.createZipFileForEntries(files)
    }

    protected String getDiagnoseScriptFilename(DiagnoseExecution diagnoseExecution) {
        "diagnose.${diagnoseExecution.executedScriptFileExtension}"
    }

    protected String createEnvironmentInformation() {

        def environmentContent = [
                'App Name'          : 'globalConfig.webContextName',
                'Execution User'    : 'userSession.user.instanceName',
                'Client Information': 'userSession.clientInfo'
        ]

        environmentContent.collect { k, v ->
            "$k: $v"
        }.join('\n')

    }

}
