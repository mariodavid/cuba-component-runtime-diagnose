package de.diedavids.cuba.runtimediagnose.web.screens.jpql

import com.haulmont.cuba.core.global.Messages
import com.haulmont.cuba.core.global.RemoteException
import com.haulmont.cuba.gui.components.AbstractFrame
import com.haulmont.cuba.gui.components.SourceCodeEditor
import com.haulmont.cuba.web.gui.components.WebSourceCodeEditor
import de.diedavids.cuba.runtimediagnose.db.DbDiagnoseService
import de.diedavids.cuba.runtimediagnose.web.screens.console.ConsoleFrame
import spock.lang.Specification
import spock.lang.Unroll

import static com.haulmont.cuba.gui.WindowManager.OpenType
import static com.haulmont.cuba.gui.components.Frame.NotificationType.ERROR
import static com.haulmont.cuba.gui.components.Frame.NotificationType.WARNING

class JpqlConsoleSpec extends Specification {

    JpqlConsole sut
    ConsoleFrame consoleFrame
    SourceCodeEditor codeEditor
    DbDiagnoseService dbDiagnoseService
    AbstractFrame frame
    Messages messages


    def setup() {
        codeEditor = Mock(SourceCodeEditor)
        consoleFrame = Mock() {
            getComponent('console') >> codeEditor
        }
        dbDiagnoseService = Mock(DbDiagnoseService)
        messages = Mock(Messages)
        sut = new JpqlConsole(consoleFrame: consoleFrame, dbDiagnoseService: dbDiagnoseService, messages: messages)

        frame = Mock(AbstractFrame)
        sut.setWrappedFrame(frame)

        frame.getMessagesPack() >> "de.diedavids"


    }

    @Unroll
    def "showSqlDialog show notification if text area is empty"() {

        given:
        codeEditor.getValue() >> ""

        when:
        sut.showSqlDialog()

        then:
        1 * frame.showNotification(_, WARNING)

    }

    def "showSqlDialog a show exception dialog if the script in text area is wrong"() {

        given:
        def codeEditorValue = 'this string is not JPQL'
        codeEditor.getValue() >> codeEditorValue
        dbDiagnoseService.getSqlQuery(codeEditorValue) >> { s -> throw new RemoteException(new IllegalArgumentException()) }

        when:
        sut.showSqlDialog()

        then:
        1 * frame.showNotification(_, ERROR)

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
        1 * frame.openWindow('sqlCopyDialog', OpenType.DIALOG,_)

    }

}