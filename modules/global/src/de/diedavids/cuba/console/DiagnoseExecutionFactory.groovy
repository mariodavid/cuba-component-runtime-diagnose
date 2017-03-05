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

interface DiagnoseExecutionFactory {

    public static final String NAME = 'console_DiagnoseExecutionFactory'


    DiagnoseExecution createDiagnoseExecutionFromFile(File file)
    byte[] createExecutionResultFormDiagnoseExecution(DiagnoseExecution diagnoseExecution)


}
