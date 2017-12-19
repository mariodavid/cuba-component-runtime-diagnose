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

    String copyButtonClass = 'copy-sql-button'
    String sqlTextContentClass = "sql-text-content-${UUID.randomUUID()}"

    @Override
    void init(Map<String, Object> params) {
        super.init(params)
        copyBtn.styleName = copyButtonClass
        textArea.styleName = sqlTextContentClass
        textArea.value = params.get('sqlQuery')

        initJavascriptButtonListener()
    }

    void initJavascriptButtonListener() {
        JavaScript.current.execute(addCopyButtonListener())
    }

    String addCopyButtonListener() {
        """
            try {
                var copyButton = document.querySelector('.${copyButtonClass}')
                copyButton.addEventListener('click', function(){
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
