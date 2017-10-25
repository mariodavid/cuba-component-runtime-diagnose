package de.diedavids.cuba.runtimediagnose.web.screens.diagnose

import com.haulmont.cuba.core.global.DatatypeFormatter
import com.haulmont.cuba.core.global.GlobalConfig
import com.haulmont.cuba.core.global.Messages
import com.haulmont.cuba.core.global.TimeSource
import com.haulmont.cuba.gui.components.Frame
import com.haulmont.cuba.gui.export.ByteArrayDataProvider
import com.haulmont.cuba.gui.export.ExportDisplay
import com.haulmont.cuba.gui.export.ExportFormat
import spock.lang.Specification

class DiagnoseFileDownloaderSpec extends Specification {

    DiagnoseFileDownloader sut

    GlobalConfig globalConfig
    DatatypeFormatter datatypeFormatter
    TimeSource timeSource
    ExportDisplay exportDisplay
    Messages messages

    def setup() {
        messages = Mock(Messages)
        globalConfig = Mock(GlobalConfig)
        timeSource = Mock(TimeSource)

        exportDisplay = Mock(ExportDisplay)
        datatypeFormatter = Mock(DatatypeFormatter)

        sut = new DiagnoseFileDownloader(
                messages: messages,
                globalConfig: globalConfig,
                timeSource: timeSource,
                datatypeFormatter: datatypeFormatter,
                exportDisplay: exportDisplay
        )
    }

    def "createResultFilename creates the filename from the app context as well as the current date"() {

        given:
        def now = new Date()
        timeSource.currentTimestamp() >> now

        datatypeFormatter.formatDateTime(now) >> "01/01/2020 08:00"

        globalConfig.webContextName >> "console-app"
        when:
        def filename = sut.createResultFilename()
        then:
        filename == "console-app-diagnose-execution-01/01/2020-08:00.zip"
    }

    def "downloadFile shows a success messsage if everything works ok"() {

        given:
        def frame = Mock(Frame)
        messages.formatMessage(DiagnoseFileDownloader, 'diagnoseResultsDownloadedMessage') >> "did work"

        when:
        sut.downloadFile(frame, [] as byte[], 'myfile.zip')

        then:
        frame.showNotification("did work")
    }

    def "downloadFile delegates to export display to download the file"() {

        given:
        def frame = Mock(Frame)
        messages.formatMessage(DiagnoseFileDownloader, 'diagnoseResultsDownloadedMessage') >> "did work"

        def filename = 'myfile.zip'
        when:
        sut.downloadFile(frame, [] as byte[], filename)

        then:
        1 * exportDisplay.show(_ as ByteArrayDataProvider, filename, ExportFormat.ZIP)
    }

    def "downloadFile shows an error if an exception occurs during download"() {
        def frame = Mock(Frame)
        given:
        exportDisplay.show(_,_,_) >> {throw new Exception("did not work exception")}

        and:
        messages.formatMessage(DiagnoseFileDownloader, 'exportFailed') >> "did not work"

        when:
        sut.downloadFile(frame, [] as byte[], 'myfile.zip')

        then:
        frame.showNotification("did not work", Frame.NotificationType.ERROR)
    }
}
