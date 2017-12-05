package de.diedavids.cuba.runtimediagnose.web.screens.jpql

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

    def "onCancelBtnClick test"() {
        given:
        int countCloseWindow = 0
        SqlCopyDialog dialog = new SqlCopyDialog() {
            @Override
            public boolean close(String actionId) {
                countCloseWindow++
                return true
            }
        }

        when:
        dialog.onCancelBtnClick()

        then:
        countCloseWindow == 1
    }
}

