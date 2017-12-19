package de.diedavids.cuba.runtimediagnose.web.screens.jpql

import com.haulmont.cuba.gui.components.Button
import com.haulmont.cuba.gui.components.TextArea
import spock.lang.Specification

class SqlCopyDialogSpec extends Specification {

    MockableSqlCopyDialog sut
    Button copyBtn
    TextArea textArea

    def setup() {
        copyBtn = Mock(Button)
        textArea = Mock(TextArea)
        sut = new MockableSqlCopyDialog(
                copyBtn: copyBtn,
                textArea: textArea
        )
    }


    def "init defines style names on the button and the textarea so that the javascript can access it afterwards"() {

        when:
        sut.init([sqlQuery: 'mySqlQuery'])

        then:
        1 * copyBtn.setStyleName('copy-sql-button')
        textArea.setStyleName("sql-text-content-")

    }

    def "init registers the javascript button listener"() {

        when:
        sut.init([:])

        then:
        sut.javascriptHasBeenExecuted
    }

    def "setCopyBtnAction returns javascript code which defines copy to clipboard functionality"() {

        given:
        String expectedJavascriptCode = """
            try {
                var copyButton = document.querySelector('.${sut.copyButtonClass}')
                copyButton.addEventListener('click', function(){
                    var textarea = document.querySelector('.${sut.sqlTextContentClass}').querySelector('textarea');
                    textarea.select();
                    document.execCommand('copy');
                });
            }
            catch(err) {
                concole.log(err);
            }
        """

        expect:
        sut.addCopyButtonListener() == expectedJavascriptCode
    }

}


class MockableSqlCopyDialog extends SqlCopyDialog {


    def javascriptHasBeenExecuted = false

    @Override
    void initJavascriptButtonListener() {
        javascriptHasBeenExecuted = true
    }
}