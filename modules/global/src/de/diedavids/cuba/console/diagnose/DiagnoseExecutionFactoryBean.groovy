package de.diedavids.cuba.console.diagnose

import groovy.json.JsonSlurper
import org.apache.commons.compress.archivers.ArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream
import org.apache.commons.io.IOUtils
import org.springframework.stereotype.Component

import javax.inject.Inject
import java.nio.charset.StandardCharsets
import java.util.zip.CRC32
import java.util.zip.ZipEntry
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
    DiagnoseExecution createAdHocDiagnoseExecution(String diagnoseScript) {

        def result = new DiagnoseExecution(manifest: new DiagnoseManifest(diagnoseType: DiagnoseType.GROOVY))
        result.diagnoseScript = diagnoseScript
        result
    }

    private String readDiagnoseScriptFromDiagnoseFile(DiagnoseExecution diagnoseExecution, ZipFile diagnoseZipFile) {
        if (diagnoseExecution.isGroovy()) {
            zipFileHelper.readFileContentFromArchive('diagnose.groovy', diagnoseZipFile)
        } else if (diagnoseExecution.isSQL()) {
            zipFileHelper.readFileContentFromArchive('diagnose.sql', diagnoseZipFile)
        }
    }

    private DiagnoseManifest createManifestFromDiagnoseFile(ZipFile diagnoseZipFile) {
        def manifestJson = new JsonSlurper().parse(zipFileHelper.readFileFromArchive(MANIFEST_FILENAME, diagnoseZipFile))
        manifestJson as DiagnoseManifest
    }


    byte[] createExecutionResultFormDiagnoseExecution(DiagnoseExecution diagnoseExecution) {
        zipFileHelper.createZipFileForEntries(diagnoseExecution.executionResultFileMap)
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
