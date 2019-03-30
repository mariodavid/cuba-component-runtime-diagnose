package de.diedavids.cuba.runtimediagnose.web.screens.console

import com.haulmont.cuba.gui.Notifications
import com.haulmont.cuba.gui.components.AbstractFrame
import com.haulmont.cuba.gui.components.Frame
import com.haulmont.cuba.gui.components.SourceCodeEditor
import com.haulmont.cuba.gui.components.SplitPanel
import com.haulmont.cuba.gui.screen.MessageBundle
import de.diedavids.cuba.runtimediagnose.diagnose.DiagnoseExecution
import de.diedavids.cuba.runtimediagnose.diagnose.DiagnoseExecutionFactory
import de.diedavids.cuba.runtimediagnose.diagnose.DiagnoseType
import de.diedavids.cuba.runtimediagnose.web.screens.diagnose.DiagnoseFileDownloader
import spock.lang.Specification

class ConsoleWindowSpec extends Specification {

    TestGroovyConsoleWindow sut

    SourceCodeEditor console
    SplitPanel consoleResultSplitter
    DiagnoseExecutionFactory diagnoseExecutionFactory
    DiagnoseFileDownloader diagnoseFileDownloader
    DiagnoseExecution diagnoseExecution
    AbstractFrame frame
    Notifications notifications


    def setup() {
        console = Mock(SourceCodeEditor)
        consoleResultSplitter = Mock(SplitPanel)
        diagnoseExecutionFactory = Mock(DiagnoseExecutionFactory)
        diagnoseFileDownloader = Mock(DiagnoseFileDownloader)
        diagnoseExecution = Mock(DiagnoseExecution)
        notifications = Mock(Notifications)

        sut = new TestGroovyConsoleWindow(
                console: console,
                consoleResultSplitter: consoleResultSplitter,
                diagnoseExecutionFactory: diagnoseExecutionFactory,
                diagnoseExecution: diagnoseExecution,
                diagnoseFileDownloader: diagnoseFileDownloader,
                notifications: notifications
        )

        frame = Mock(AbstractFrame)
        frame.getMessagesPack() >> 'de.diedavids.cuba.ccrd'

        sut.frame = frame
    }


    def "clearConsole removes the content of the console"() {
        when:
        sut.clearConsole()

        then:
        1 * console.setValue('')
    }

    def "maximizeConsole sets the split position to 100"() {
        when:
        sut.maximizeConsole()

        then:
        1 * consoleResultSplitter.setSplitPosition(100)
    }


    def "maximizeConsoleResult sets the split position to 0"() {
        when:
        sut.maximizeConsoleResult()

        then:
        1 * consoleResultSplitter.setSplitPosition(0)
    }

    def "minimizeConsole sets the split position to 50"() {
        when:
        sut.minimizeConsole()

        then:
        1 * consoleResultSplitter.setSplitPosition(AbstractConsoleWindow.SPLIT_POSITION_CENTER)
    }

    def "minimizeConsoleResult sets the split position to 50"() {
        when:
        sut.minimizeConsoleResult()

        then:
        1 * consoleResultSplitter.setSplitPosition(AbstractConsoleWindow.SPLIT_POSITION_CENTER)
    }

    def "runConsole runs the console content if it contains content"() {

        given:
        console.getValue() >> '4+5'

        when:
        sut.runConsole()

        then:
        sut.consoleRunExecuted
    }

    def "runConsole throws an error message if the content of the console is empty"() {

        given:
        console.getValue() >> ''


        def notificationsBuilder = Mock(Notifications.NotificationBuilder)
        notificationsBuilder./with.*/(*_) >> notificationsBuilder

        when:
        sut.runConsole()

        then:
        1 * notifications.create(Notifications.NotificationType.WARNING) >> notificationsBuilder

        1 * notificationsBuilder.show()

    }

    def "downloadConsoleResult requests the zip bytes from diagnoseExecutionFactory and forwards it to diagnoseFileDownloader"() {

        given:
        def zipBytes = [] as byte[]
        diagnoseExecutionFactory.createExecutionResultFromDiagnoseExecution(_) >> zipBytes

        when:
        sut.downloadConsoleResult()

        then:
        1 * diagnoseFileDownloader.downloadFile(sut, zipBytes)

    }


    def "downloadDiagnoseRequestFile creates an ad hoc diagnose request, retrieves the zipBytes and forwards it to the file downloader"() {

        given:
        console.getValue() >> '5+6'

        and:
        def diagnoseExecution = Mock(DiagnoseExecution)
        diagnoseExecutionFactory.createAdHocDiagnoseExecution('5+6', DiagnoseType.GROOVY) >> diagnoseExecution


        and:
        def zipBytes = [] as byte[]
        diagnoseExecutionFactory.createDiagnoseRequestFileFromDiagnoseExecution(diagnoseExecution) >> zipBytes


        when:
        sut.downloadDiagnoseRequestFile()

        then:
        1 * diagnoseFileDownloader.downloadFile(sut, zipBytes, 'diagnose.zip')

    }

}

class TestGroovyConsoleWindow extends AbstractConsoleWindow {

    boolean consoleRunExecuted = false

    @Override
    DiagnoseType getDiagnoseType() {
        DiagnoseType.GROOVY
    }

    @Override
    void doRunConsole() {
        consoleRunExecuted = true
    }

    @Override
    void clearConsoleResult() {

    }

    @Override
    protected String formatMessage(String key, Object... params) {
        key
    }

    @Override
    protected String getMessage(String key) {
        key
    }
}
