package de.diedavids.cuba.runtimediagnose.diagnose

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import spock.lang.Specification

import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream

class ZipFileHelperSpec extends Specification {

    ZipFileHelper sut

    def setup() {
        sut = new ZipFileHelper()
    }

    def "readFileContentFromArchive returns the text from the file within the zip archive"() {

        given:
        def file = loadFileFromTestDirectory("sql-diagnose-wizard.zip")
        def diagnoseZipFile = new ZipFile(file)

        when:
        def fileContent = sut.readFileContentFromArchive('manifest-sql.json', diagnoseZipFile)

        then:
        new JsonSlurper().parseText(fileContent).diagnoseType == "SQL"
    }

    def "readFileContentFromArchive returns null if the file is not found within the zip file"() {

        given:
        def file = loadFileFromTestDirectory("sql-diagnose-wizard.zip")
        def diagnoseZipFile = new ZipFile(file)

        expect:
        !sut.readFileContentFromArchive('wrong-filename.json', diagnoseZipFile)
    }


    def "createZipFileForEntries creates a file for every entry in the map"() {

        given:
        def diagnoseExecution = new DiagnoseExecution(
                manifest: new DiagnoseManifest(
                        appName: "my-app"
                )
        )
        def files = [
                'diagnose.groovy': "4+5",
                'manifest.json'  : JsonOutput.prettyPrint(JsonOutput.toJson(diagnoseExecution.manifest)),
        ]

        when:
        def zipBytes = sut.createZipFileForEntries(files)
        def zipEntries = getZipEntriesFromZipBytes(zipBytes)

        then:
        zipEntries[0].name == "diagnose.groovy"
        zipEntries[1].name == "manifest.json"
    }


    def "createZipFileForEntries creates an empty file if there is no content"() {

        given:
        def files = ['diagnose.groovy': null]

        when:
        def zipBytes = sut.createZipFileForEntries(files)
        def zipEntries = getZipEntriesFromZipBytes(zipBytes)

        then:
        zipEntries[0].name == "diagnose.groovy"
        zipEntries[0].compressedSize == 0l
    }


    def "createZipFileForEntries creates an empty zip file if no entries are given"() {

        when:
        def zipBytes = sut.createZipFileForEntries([:])
        def zipEntries = getZipEntriesFromZipBytes(zipBytes)

        then:
        !zipEntries
    }

    private Collection<ZipEntry> getZipEntriesFromZipBytes(byte[] result) {
        InputStream is = new ByteArrayInputStream(result);
        InputStream zis = new ZipInputStream(is);
        ZipEntry ze;

        Collection<ZipEntry> entries = []

        while ((ze = zis.getNextEntry()) != null) {
            entries << ze
        }
        entries
    }

    protected File loadFileFromTestDirectory(String filename) {
        new File(getClass().classLoader.getResource("de/diedavids/cuba/runtimediagnose/diagnose/testfile/$filename").file)
    }
}
