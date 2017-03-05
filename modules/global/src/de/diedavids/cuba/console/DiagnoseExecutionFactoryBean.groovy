package de.diedavids.cuba.console

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

    private String readDiagnoseScriptFromDiagnoseFile(DiagnoseExecution diagnoseExecution,ZipFile diagnoseZipFile) {
        if (diagnoseExecution.isGroovy()) {
            readFileContentFromArchive('diagnose.groovy', diagnoseZipFile)
        }
        else if (diagnoseExecution.isSQL()) {
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

        def entries = [
                "environmentInformation.log": createEnvironmentInformation(),
                "executedScript.groovy"     : diagnoseExecution.diagnoseScript,
                "result.log"                : diagnoseExecution.getResult('result'),
                "log.log"                   : diagnoseExecution.getResult('log'),
                "stacktrace.log"            : diagnoseExecution.getResult('stacktrace')
        ]

        createZipFileForEntries(entries)

    }

    protected String createEnvironmentInformation() {

        def environmentContent = [
                "App Name"          : "globalConfig.webContextName",
                "Execution User"    : "userSession.user.instanceName",
                "Client Information": "userSession.clientInfo"
        ]

        environmentContent.collect { k, v ->
            "$k: $v"
        }.join("\n")

    }


    protected byte[] createZipFileForEntries(Map<String, String> fileEntries) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ZipArchiveOutputStream zipOutputStream = new ZipArchiveOutputStream(byteArrayOutputStream);
        zipOutputStream.method = ZipArchiveOutputStream.STORED
        zipOutputStream.encoding = StandardCharsets.UTF_8.name()
        try {
            fileEntries.each { String fileName, String fileContent ->
                addArchiveEntryToZipFile(zipOutputStream, fileName, fileContent?.bytes)
            }

        } catch (Exception e) {
            throw new RuntimeException("Error on creating zip archive during entities export", e)
        } finally {
            IOUtils.closeQuietly(zipOutputStream)
        }
        byteArrayOutputStream.toByteArray()

    }

    protected void addArchiveEntryToZipFile(ZipArchiveOutputStream zipOutputStream, String fileName, byte[] fileContent) {

        if (!fileContent) {
            fileContent = []
        }
        ArchiveEntry resultArchiveEntry = createArchiveEntry(fileName, fileContent);

        zipOutputStream.putArchiveEntry(resultArchiveEntry);
        zipOutputStream.write(fileContent);
        zipOutputStream.closeArchiveEntry();
    }

    protected ArchiveEntry createArchiveEntry(String name, byte[] data) {
        ZipArchiveEntry zipEntry = new ZipArchiveEntry(name);
        zipEntry.size = data.length
        zipEntry.compressedSize = zipEntry.size
        CRC32 crc32 = new CRC32()
        crc32.update(data)
        zipEntry.crc = crc32.value

        zipEntry
    }
}
