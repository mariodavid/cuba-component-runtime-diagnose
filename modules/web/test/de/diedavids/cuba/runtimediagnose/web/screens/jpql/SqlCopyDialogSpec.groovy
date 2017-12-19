package de.diedavids.cuba.runtimediagnose.web.screens.jpql

import com.haulmont.bali.util.ParamsMap
import com.haulmont.cuba.gui.components.TextArea
import com.haulmont.cuba.web.gui.components.WebButton
import spock.lang.Specification

class SqlCopyDialogSpec extends Specification {

    SqlCopyDialog sqlCopyDialog

    def setup() {
        sqlCopyDialog = new SqlCopyDialog()
    }

    def "setCopyBtnAction return rigth js"() {
        given:
        String js = """
            try {
                var copyButton = document.querySelector('.${sqlCopyDialog.copyButtonClass}')
                copyButton.addEventListener('click', function(){
                    console.log('clicked!');
                    var textarea = document.querySelector('.${sqlCopyDialog.sqlTextContentClass}').querySelector('textarea');
                    textarea.select();
                    document.execCommand('copy');
                });
            }
            catch(err) {
                concole.log(err);
            }
        """

        expect:
        sqlCopyDialog.addCopyButtonListener() == js
    }

    def "init check params"() {

        given:
        String copyButtonClass = 'copy-sql-button'
        String sqlTextContentClass = "sql-text-content-"
        String paramName = 'sqlQuery'
        String paramValue = 'sql query'
        Map<String, Object> params = ParamsMap.of(paramName, paramValue)

        TextArea mockTextArea = Mock()
        mockTextArea.styleName >> sqlTextContentClass

        int countExecJs = 0
        SqlCopyDialog dialog = new SqlCopyDialog() {
            @Override
            void executeJs() {
                countExecJs++
            }
        }

        dialog.with {
            copyBtn = new WebButton()
            textArea = mockTextArea
        }

        when:
        dialog.init(params)

        then:
        dialog.copyBtn.getStyleName().contains(copyButtonClass)
        dialog.textArea.getStyleName().contains(sqlTextContentClass)
        countExecJs == 1
    }
}

