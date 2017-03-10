package de.diedavids.cuba.console.diagnose

import groovy.json.JsonSlurper
import org.apache.commons.compress.archivers.ArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream
import org.apache.commons.io.IOUtils
import org.springframework.stereotype.Component

import java.nio.charset.StandardCharsets
import java.util.zip.CRC32
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

@Component(DiagnoseExecutionFactory.NAME)
class DiagnoseExecutionFactoryBean implements DiagnoseExecutionFactory {

    public static final String MANIFEST_FILENAME = 'manifest.json'


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
            readFileContentFromArchive('diagnose.groovy', diagnoseZipFile)
        } else if (diagnoseExecution.isSQL()) {
            readFileContentFromArchive('diagnose.sql', diagnoseZipFile)
        }
    }

    private DiagnoseManifest createManifestFromDiagnoseFile(ZipFile diagnoseZipFile) {
        def manifestJson = new JsonSlurper().parse(readFileFromArchive(MANIFEST_FILENAME, diagnoseZipFile))
        manifestJson as DiagnoseManifest
    }


    private String readFileContentFromArchive(String filename, ZipFile diagnoseZipFile) {
        readFileFromArchive(filename, diagnoseZipFile).text
    }

    private InputStream readFileFromArchive(String filename, ZipFile diagnoseZipFile) {
        ZipEntry foundFile = diagnoseZipFile.entries().find { it.name == filename } as ZipEntry
        diagnoseZipFile.getInputStream(foundFile)
    }

    byte[] createExecutionResultFormDiagnoseExecution(DiagnoseExecution diagnoseExecution) {
        createZipFileForEntries(diagnoseExecution.executionResultFileMap)
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


    protected byte[] createZipFileForEntries(Map<String, String> fileEntries) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()
        ZipArchiveOutputStream zipOutputStream = new ZipArchiveOutputStream(byteArrayOutputStream)
        zipOutputStream.method = ZipArchiveOutputStream.STORED
        zipOutputStream.encoding = StandardCharsets.UTF_8.name()

        fileEntries.each { String fileName, String fileContent ->
            addArchiveEntryToZipFile(zipOutputStream, fileName, fileContent?.bytes)
        }

        IOUtils.closeQuietly(zipOutputStream)

        byteArrayOutputStream.toByteArray()

    }

    protected void addArchiveEntryToZipFile(ZipArchiveOutputStream zipOutputStream, String fileName, byte[] fileContent) {

        byte[] correctFileContent = fileContent ?: [] as byte[]
        ArchiveEntry resultArchiveEntry = createArchiveEntry(fileName, correctFileContent)

        zipOutputStream.putArchiveEntry(resultArchiveEntry)
        zipOutputStream.write(correctFileContent)
        zipOutputStream.closeArchiveEntry()
    }

    protected ArchiveEntry createArchiveEntry(String name, byte[] data) {
        ZipArchiveEntry zipEntry = new ZipArchiveEntry(name)
        zipEntry.size = data.length
        zipEntry.compressedSize = zipEntry.size
        CRC32 crc32 = new CRC32()
        crc32.update(data)
        zipEntry.crc = crc32.value

        zipEntry
    }
}
