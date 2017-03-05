package de.diedavids.cuba.console.web.screens.groovy

import com.haulmont.cuba.core.global.DataManager
import com.haulmont.cuba.core.global.DatatypeFormatter
import com.haulmont.cuba.core.global.GlobalConfig
import com.haulmont.cuba.core.global.Metadata
import com.haulmont.cuba.core.global.Scripting
import com.haulmont.cuba.core.global.TimeSource
import com.haulmont.cuba.gui.components.*
import com.haulmont.cuba.gui.export.ByteArrayDataProvider
import com.haulmont.cuba.gui.export.ExportDisplay
import com.haulmont.cuba.gui.export.ExportFormat
import com.haulmont.cuba.gui.upload.FileUploadingAPI
import com.haulmont.cuba.security.global.UserSession
import de.diedavids.cuba.console.GroovyConsoleLogger
import org.apache.commons.compress.archivers.ArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream
import org.apache.commons.io.IOUtils

import javax.inject.Inject
import java.nio.charset.StandardCharsets
import java.util.zip.CRC32

class GroovyConsole extends AbstractWindow {


    @Inject
    SourceCodeEditor console

    @Inject
    SourceCodeEditor consoleResult

    @Inject
    SourceCodeEditor consoleResultLog

    @Inject
    SourceCodeEditor consoleStacktraceLog

    @Inject
    SourceCodeEditor consoleExecutedScriptLog

    @Inject
    private FileUploadField consoleFileUploadBtn

    @Inject
    private FileUploadingAPI fileUploadingAPI

    @Inject
    SplitPanel consoleResultSplitter


    @Inject
    GlobalConfig globalConfig

    @Inject
    protected ExportDisplay exportDisplay;

    @Inject
    Scripting scripting

    @Inject
    DataManager dataManager

    // cuba.groovyEvaluatorImport (https://doc.cuba-platform.com/manual-6.4/app_properties_reference.html)

    @Inject
    Metadata metadata

    @Inject
    DatatypeFormatter datatypeFormatter

    @Inject
    UserSession userSession

    @Inject
    TimeSource timeSource

    @Override
    void init(Map<String, Object> params) {

        consoleFileUploadBtn.addFileUploadSucceedListener(new FileUploadField.FileUploadSucceedListener() {
            @Override
            void fileUploadSucceed(FileUploadField.FileUploadSucceedEvent e) {
                File file = fileUploadingAPI.getFile(consoleFileUploadBtn.fileId)


                showOptionDialog("File upload successful", "File upload successful. Execute the content of the file?", Frame.MessageType.CONFIRMATION, [new DialogAction(DialogAction.Type.OK) {

                    @Override
                    void actionPerform(Component component) {
                        runGroovyScript(file.text)
                    }
                }, new DialogAction(DialogAction.Type.CANCEL)])
            }
        })

        consoleFileUploadBtn.addFileUploadErrorListener(new UploadField.FileUploadErrorListener() {
            @Override
            void fileUploadError(UploadField.FileUploadErrorEvent e) {
                showNotification("File upload error", Frame.NotificationType.ERROR)
            }
        })
    }

    void runConsole() {
        runGroovyScript(console.value)
    }

    void runGroovyScript(String groovyScript) {
        if (groovyScript) {

            def binding = new Binding()
            def log = new GroovyConsoleLogger(
                    timeSource: timeSource,
                    datatypeFormatter: datatypeFormatter
            )
            binding.setVariable("log", log)
            binding.setVariable("dataManager", dataManager)
            binding.setVariable("metadata", metadata)
            def result = ""
            def stacktraceLog = ""
            try {
                result = scripting.evaluateGroovy(groovyScript, binding)

                showNotification("Execution sucessful", Frame.NotificationType.HUMANIZED)

            }
            catch (Throwable throwable) {
                StringWriter stacktrace = new StringWriter();
                throwable.printStackTrace(new PrintWriter(stacktrace));
                stacktraceLog = stacktrace.toString()
                result = throwable.message

                showNotification("Execution failed. See Result-Tab for more information", Frame.NotificationType.ERROR)
            }

            consoleResult.value = result.toString()
            consoleResultLog.value = log.toString()
            consoleStacktraceLog.value = stacktraceLog
            consoleExecutedScriptLog.value = groovyScript.toString()

            maximizeConsoleResult(20)
        }

    }

    void clearConsole() {
        console.value = ""
    }

    void clearConsoleResult() {
        consoleResult.value = ""
        consoleResultLog.value = ""
        consoleStacktraceLog.value = ""
        consoleExecutedScriptLog.value = ""

    }

    void downloadConsoleResult() {

        def zipBytes = createZipFile()

        try {
            exportDisplay.show(new ByteArrayDataProvider(zipBytes),
                    createZipFileName(), ExportFormat.ZIP);
        } catch (Exception e) {
            showNotification(getMessage("exportFailed"), e.getMessage(), Frame.NotificationType.ERROR);
        }
    }

    protected String createZipFileName() {
        def dateString = datatypeFormatter.formatDateTime(timeSource.currentTimestamp()).replace(" ", "-")
        def appName = globalConfig.webContextName
        "${appName}-console-execution-${dateString}.zip"
    }

    protected byte[] createZipFile() {

        def entries = [
                "environmentInformation.log": createEnvironmentInformation(),
                "executedScript.groovy"     : consoleExecutedScriptLog.value,
                "result.log"                : consoleResult.value,
                "log.log"                   : consoleResultLog.value,
                "stacktrace.log"            : consoleStacktraceLog.value
        ]

        createZipFileForEntries(entries)

    }

    protected String createEnvironmentInformation() {

        def environmentContent = [
                "App Name"          : globalConfig.webContextName,
                "Execution User"    : userSession.user.instanceName,
                "Client Information": userSession.clientInfo
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

    void maximizeConsole() {
        consoleResultSplitter.splitPosition = 100
    }

    void maximizeConsoleResult(Integer position = 0) {
        consoleResultSplitter.splitPosition = position
    }


    void minimizeConsole() {
        consoleResultSplitter.splitPosition = 50
    }

    void minimizeConsoleResult() {
        consoleResultSplitter.splitPosition = 50
    }

}