package de.diedavids.cuba.console.diagnose

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
class ZipFileHelper {

    String readFileContentFromArchive(String filename, ZipFile diagnoseZipFile) {
        readFileFromArchive(filename, diagnoseZipFile).text
    }

    InputStream readFileFromArchive(String filename, ZipFile diagnoseZipFile) {
        ZipEntry foundFile = diagnoseZipFile.entries().find { it.name == filename } as ZipEntry
        diagnoseZipFile.getInputStream(foundFile)
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
