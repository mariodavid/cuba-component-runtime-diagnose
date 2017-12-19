package de.diedavids.cuba.runtimediagnose.web.screens.jpql

import com.haulmont.cuba.core.global.RemoteException
import com.haulmont.cuba.gui.components.AbstractWindow
import com.haulmont.cuba.gui.components.Frame
import com.haulmont.cuba.gui.components.SourceCodeEditor
import com.haulmont.cuba.web.gui.components.WebSourceCodeEditor
import de.diedavids.cuba.runtimediagnose.db.DbDiagnoseService
import de.diedavids.cuba.runtimediagnose.web.screens.console.ConsoleFrame
import spock.lang.Specification
import spock.lang.Unroll

import static com.haulmont.cuba.gui.WindowManager.OpenType
import static com.haulmont.cuba.gui.WindowManager.OpenType.DIALOG

class JpqlConsoleSpec extends Specification {

    JpqlConsole sut
    ConsoleFrame predefinedConsoleFrame
    SourceCodeEditor predefinedCodeEditor
    DbDiagnoseService predefinedDbDiagnoseService


    def setup() {
        predefinedCodeEditor = Mock()
        predefinedConsoleFrame = Mock() {
            getComponent('console') >> predefinedCodeEditor
        }
        predefinedDbDiagnoseService = Mock()
        sut = new JpqlConsole()
    }

    @Unroll
    def "showSqlDialog show notification if text area is empty"() {

        given:
        predefinedCodeEditor.getValue() >> testVal

        int countShowNotificationExecution = 0

        def localSut = new JpqlConsole() {
            protected String getMessage(String key) { "" }

            void showNotification(String caption, Frame.NotificationType type) { countShowNotificationExecution++ }
        }

        localSut.with {
            dbDiagnoseService = this.predefinedDbDiagnoseService
            consoleFrame = this.predefinedConsoleFrame
        }

        when:
        localSut.showSqlDialog()

        then:
        countShowNotificationExecution == 1

        where:
        testVal | _
        ""      | _
        null    | _
    }

    def "showSqlDialog a show exception dialog if the script in text area is wrong"() {

        given:
        int countExceptionNotification = 0
        String noJpql = 'this string is not JPQL'
        predefinedCodeEditor.getValue() >> noJpql
        predefinedDbDiagnoseService.getSqlQuery(noJpql) >> { s -> throw new RemoteException(new IllegalArgumentException()) }

        def localSut = new JpqlConsole() {
            protected String getMessage(String key) { "" }

            void showNotification(String caption, Frame.NotificationType type) { countExceptionNotification++ }
        }

        localSut.with {
            dbDiagnoseService = this.predefinedDbDiagnoseService
            consoleFrame = this.predefinedConsoleFrame
        }

        when:
        localSut.showSqlDialog()

        then:
        countExceptionNotification == 1

    }

    def "showSqlDialog a show sql dialog if the script is correct"() {

        given:
        int countOpenSqlDialog = 0
        String jpql = 'select u from sec$User u'
        String sql = 'SELECT * FROM SEC_USER'
        predefinedCodeEditor.getValue() >> jpql
        predefinedDbDiagnoseService.getSqlQuery(jpql) >> "[${sql}]"

        def localSut = new JpqlConsole() {
            AbstractWindow openWindow(String alias, OpenType type, Map<String, Object> params) {
                if (alias == 'sqlCopyDialog' && type == DIALOG && params.get('sqlQuery') == sql) {
                    countOpenSqlDialog++
                }
                return null
            }
        }

        localSut.with {
            dbDiagnoseService = this.predefinedDbDiagnoseService
            consoleFrame = this.predefinedConsoleFrame
        }

        when:
        localSut.showSqlDialog()

        then:
        countOpenSqlDialog == 1

    }

    @Unroll
    def "codeEditorIsEmpty check"() {
        given:
        SourceCodeEditor editor = new WebSourceCodeEditor()
        editor.value = values

        expect:
        sut.codeEditorIsEmpty(editor) == res
        !sut.codeEditorIsEmpty(null)

        where:
        values | res
        null   | true
        ''     | true
        "str"  | false
    }
}