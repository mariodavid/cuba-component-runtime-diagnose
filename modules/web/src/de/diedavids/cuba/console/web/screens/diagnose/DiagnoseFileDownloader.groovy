package de.diedavids.cuba.console.web.screens.diagnose

import com.haulmont.cuba.core.global.DatatypeFormatter
import com.haulmont.cuba.core.global.GlobalConfig
import com.haulmont.cuba.core.global.Messages
import com.haulmont.cuba.core.global.TimeSource
import com.haulmont.cuba.gui.components.Frame
import com.haulmont.cuba.gui.export.ByteArrayDataProvider
import com.haulmont.cuba.gui.export.ExportDisplay
import com.haulmont.cuba.gui.export.ExportFormat
import org.springframework.stereotype.Component

import javax.inject.Inject

@Component
class DiagnoseFileDownloader {

    @Inject
    GlobalConfig globalConfig
    @Inject
    DatatypeFormatter datatypeFormatter
    @Inject
    TimeSource timeSource

    @Inject ExportDisplay exportDisplay

    @Inject
    Messages messages
    String createResultFilename() {
        def dateString = datatypeFormatter.formatDateTime(timeSource.currentTimestamp()).replace(' ', '-')
        def appName = globalConfig.webContextName
        "${appName}-console-execution-${dateString}.zip"
    }

    void downloadFile(Frame frame, byte[] zipBytes) {
        try {
            exportDisplay.show(new ByteArrayDataProvider(zipBytes),
                    createResultFilename(), ExportFormat.ZIP)
            frame.showNotification(messages.formatMessage(getClass(),'diagnoseResultsDownloadedMessage'))
        } catch (Exception e) {
            frame.showNotification(messages.formatMessage(getClass(),'exportFailed'), e.message, Frame.NotificationType.ERROR)
        }
    }
}