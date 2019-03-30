package de.diedavids.cuba.runtimediagnose.web.screens.jpql

import com.haulmont.cuba.core.global.Messages
import com.haulmont.cuba.core.global.RemoteException
import com.haulmont.cuba.gui.components.AbstractFrame
import com.haulmont.cuba.gui.components.AbstractWindow
import com.haulmont.cuba.gui.components.Component
import com.haulmont.cuba.gui.components.SourceCodeEditor
import de.diedavids.cuba.runtimediagnose.db.DbDiagnoseService
import spock.lang.Specification

import static com.haulmont.cuba.gui.WindowManager.OpenType
import static com.haulmont.cuba.gui.components.Frame.NotificationType.ERROR
import static com.haulmont.cuba.gui.components.Frame.NotificationType.WARNING

class JpqlConsoleSpec extends Specification {

    TestJpqlConsole sut
    SourceCodeEditor codeEditor
    DbDiagnoseService dbDiagnoseService
    AbstractFrame frame
    Messages messages


    def setup() {
        codeEditor = Mock(SourceCodeEditor)
        dbDiagnoseService = Mock(DbDiagnoseService)
        messages = Mock(Messages)
        sut = new TestJpqlConsole(sourceCodeEditor: codeEditor, dbDiagnoseService: dbDiagnoseService, messages: messages)

        frame = Mock(AbstractFrame)
        sut.frame = frame

        frame.getMessagesPack() >> "de.diedavids"


    }

    def "showSqlDialog show notification if text area is empty"() {

        given:
        codeEditor.getValue() >> ""

        when:
        sut.showSqlDialog()

        then:
        sut.actualNotificationCaption
        sut.actualNotificationType == WARNING

    }

    def "showSqlDialog a show exception dialog if the script in text area is wrong"() {

        given:
        def codeEditorValue = 'this string is not JPQL'
        codeEditor.getValue() >> codeEditorValue
        dbDiagnoseService.getSqlQuery(codeEditorValue) >> { s -> throw new RemoteException(new IllegalArgumentException()) }

        when:
        sut.showSqlDialog()

        then:
        sut.actualNotificationCaption
        sut.actualNotificationType == ERROR

    }

    def "showSqlDialog a show sql dialog if the script is correct"() {

        given:
        String jpql = 'select u from sec$User u'
        String sql = 'SELECT * FROM SEC_USER'
        codeEditor.getValue() >> jpql
        dbDiagnoseService.getSqlQuery(jpql) >> "[${sql}]"

        when:
        sut.showSqlDialog()

        then:
        sut.actualOpenedWindow == 'sqlCopyDialog'
    }

}

class TestJpqlConsole extends JpqlConsole {

    SourceCodeEditor sourceCodeEditor
    String actualNotificationCaption
    NotificationType actualNotificationType
    String actualOpenedWindow

    @Override
    Component getComponent(String id) {
        sourceCodeEditor
    }

    @Override
    protected String getMessage(String key) {
        key
    }

    @Override
    void showNotification(String caption, NotificationType type) {
        actualNotificationCaption = caption
        actualNotificationType = type
    }

    @Override
    AbstractWindow openWindow(String windowAlias, OpenType openType, Map<String, Object> params) {
        actualOpenedWindow = windowAlias

        null
    }
}