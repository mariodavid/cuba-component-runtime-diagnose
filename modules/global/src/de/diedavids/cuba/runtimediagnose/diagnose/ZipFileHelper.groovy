package de.diedavids.cuba.runtimediagnose.diagnose

import groovy.transform.CompileStatic
import org.apache.commons.compress.archivers.ArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream
import org.apache.commons.io.IOUtils
import org.springframework.stereotype.Component

import java.nio.charset.StandardCharsets
import java.util.zip.CRC32
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

@Component
@CompileStatic
class ZipFileHelper {

    String readFileContentFromArchive(String filename, ZipFile diagnoseZipFile) {
        readFileFromArchive(filename, diagnoseZipFile).text
    }

    InputStream readFileFromArchive(String filename, ZipFile diagnoseZipFile) {
        ZipEntry foundFile = diagnoseZipFile.entries().find { ZipEntry zipEntry -> zipEntry.name == filename } as ZipEntry
        foundFile ? diagnoseZipFile.getInputStream(foundFile) : createEmptyInputStream()

    }

    byte[] createZipFileForEntries(Map<String, String> fileEntries) {
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

    protected InputStream createEmptyInputStream() {
        new ByteArrayInputStream(''.getBytes('UTF-8'))
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
        crc32.update(data,0, data.length)
        zipEntry.crc = crc32.value

        zipEntry
    }
}
