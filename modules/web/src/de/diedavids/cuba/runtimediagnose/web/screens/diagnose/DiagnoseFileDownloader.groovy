package de.diedavids.cuba.runtimediagnose.web.screens.diagnose

import com.haulmont.cuba.core.global.DatatypeFormatter
import com.haulmont.cuba.core.global.GlobalConfig
import com.haulmont.cuba.core.global.Messages
import com.haulmont.cuba.core.global.TimeSource
import com.haulmont.cuba.gui.components.Frame
import com.haulmont.cuba.gui.export.ByteArrayDataProvider
import com.haulmont.cuba.gui.export.ExportDisplay
import com.haulmont.cuba.gui.export.ExportFormat
import groovy.transform.CompileStatic
import org.springframework.stereotype.Component

import javax.inject.Inject

@Component
@CompileStatic
class DiagnoseFileDownloader {

    @Inject
    GlobalConfig globalConfig

    @Inject
    DatatypeFormatter datatypeFormatter

    @Inject
    TimeSource timeSource

    @Inject
    ExportDisplay exportDisplay

    @Inject
    Messages messages

    String createResultFilename() {
        "${appName}-diagnose-execution-${currentDateFilenameString}.zip"
    }

    private String getAppName() {
        globalConfig.webContextName
    }

    private String getCurrentDateFilenameString() {
        def now = timeSource.currentTimestamp()
        datatypeFormatter.formatDateTime(now).replace(' ', '-')
    }

    void downloadFile(Frame frame, byte[] zipBytes, String filename = createResultFilename()) {

        try {
            exportDisplay.show(new ByteArrayDataProvider(zipBytes), filename, ExportFormat.ZIP)
            frame.showNotification(messages.formatMessage(getClass(),'diagnoseResultsDownloadedMessage'))
        } catch (Exception e) {
            frame.showNotification(messages.formatMessage(getClass(),'exportFailed'), e.message, Frame.NotificationType.ERROR)
        }
    }
}