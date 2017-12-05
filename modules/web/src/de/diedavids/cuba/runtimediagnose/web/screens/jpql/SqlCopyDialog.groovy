package de.diedavids.cuba.runtimediagnose.web.screens.jpql

import com.haulmont.cuba.gui.components.AbstractWindow
import com.haulmont.cuba.gui.components.Button
import com.haulmont.cuba.gui.components.TextArea
import com.vaadin.ui.JavaScript

import javax.inject.Named

class SqlCopyDialog extends AbstractWindow {

    @Named('sqlTextArea')
    TextArea textArea
    @Named('copyBtn')
    Button copyBtn
    @Named('cancelBtn')
    Button cancelBtn

    String copyButtonClass = 'copy-sql-button'
    String sqlTextContentClass = "sql-text-content-${UUID.randomUUID()}"

    @Override
    void init(Map<String, Object> params) {
        super.init(params)
        copyBtn.setStyleName(copyButtonClass)
        textArea.setStyleName(sqlTextContentClass)
        textArea.value = params.get('sqlQuery')

        JavaScript.current.execute(addCopyButtonListener())
    }

    void onCancelBtnClick() {
        close('closeSqlDialog')
    }

    String addCopyButtonListener() {
        """
            try {
                var copyButton = document.querySelector('.${copyButtonClass}')
                copyButton.addEventListener('click', function(){
                    console.log('clicked!');
                    var textarea = document.querySelector('.${sqlTextContentClass}').querySelector('textarea');
                    textarea.select();
                    document.execCommand('copy');
                });
            }
            catch(err) {
                concole.log(err);
            }
        """
    }
}
