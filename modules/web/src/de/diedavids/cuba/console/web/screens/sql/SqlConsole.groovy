package de.diedavids.cuba.console.web.screens.sql

import com.haulmont.chile.core.model.MetaClass
import com.haulmont.chile.core.model.MetaProperty
import com.haulmont.cuba.core.global.GlobalConfig
import com.haulmont.cuba.core.global.Metadata
import com.haulmont.cuba.gui.components.*
import com.haulmont.cuba.gui.data.DsBuilder
import com.haulmont.cuba.gui.data.impl.ValueCollectionDatasourceImpl
import com.haulmont.cuba.gui.export.ExportDisplay
import com.haulmont.cuba.gui.upload.FileUploadingAPI
import com.haulmont.cuba.gui.xml.layout.ComponentsFactory
import de.diedavids.cuba.console.service.SqlConsoleService

import javax.inject.Inject

class SqlConsole extends AbstractWindow {


    @Inject
    SourceCodeEditor sqlConsole

    @Inject
    SqlConsoleService sqlConsoleService

    @Inject
    TabSheet.Tab consoleResultTabResult

    @Inject
    private FileUploadField consoleFileUploadBtn

    @Inject
    private FileUploadingAPI fileUploadingAPI

    @Inject
    SplitPanel consoleResultSplitter

    @Inject
    Metadata metadata

    @Inject
    GlobalConfig globalConfig

    @Inject
    protected ExportDisplay exportDisplay;

    @Inject
    ComponentsFactory componentsFactory

    @Inject
    protected BoxLayout resultTableBox;

    protected Table resultTable;

    @Override
    void init(Map<String, Object> params) {
        consoleFileUploadBtn.addFileUploadSucceedListener(new FileUploadField.FileUploadSucceedListener() {
            @Override
            void fileUploadSucceed(FileUploadField.FileUploadSucceedEvent e) {
                File file = fileUploadingAPI.getFile(consoleFileUploadBtn.fileId)


                showOptionDialog("File upload successful", "File upload successful. Execute the content of the file?", Frame.MessageType.CONFIRMATION, [new DialogAction(DialogAction.Type.OK) {

                    @Override
                    void actionPerform(Component component) {
                        runGroovyScript(file.text)
                    }
                }, new DialogAction(DialogAction.Type.CANCEL)])
            }
        })
        consoleFileUploadBtn.addFileUploadErrorListener(new UploadField.FileUploadErrorListener() {
            @Override
            void fileUploadError(UploadField.FileUploadErrorEvent e) {
                showNotification("File upload error", Frame.NotificationType.ERROR)
            }
        })
    }


    void runSqlConsole() {
        def result = sqlConsoleService.executeSqlKvE(sqlConsole.value)
        ValueCollectionDatasourceImpl sqlResultDs = createDatasource(result)
        createResultTable(sqlResultDs)
    }

    private ValueCollectionDatasourceImpl createDatasource(Map<String, Object> result) {
        ValueCollectionDatasourceImpl sqlResultDs = creteValueCollectionDs()
        result.entities.each { sqlResultDs.includeItem(it) }
        result.columns.each { sqlResultDs.addProperty(it.name) }
        sqlResultDs
    }

    private ValueCollectionDatasourceImpl creteValueCollectionDs() {
        DsBuilder.create(getDsContext()).reset().setAllowCommit(false)
                .buildValuesCollectionDatasource()
    }

    private void createResultTable(ValueCollectionDatasourceImpl sqlResultDs) {
        if (resultTable) {
            resultTableBox.remove(resultTable);
        }
        resultTable = componentsFactory.createComponent(Table)
        resultTable.setFrame(frame)
        MetaClass meta = sqlResultDs.getMetaClass()
        for (MetaProperty metaProperty : meta.getProperties()) {
            Table.Column column = new Table.Column(meta.getPropertyPath(metaProperty.getName()));
            column.setCaption(metaProperty.getName());
            resultTable.addColumn(column);
        }
        resultTable.setDatasource(sqlResultDs)
        resultTable.setSizeFull()
        resultTableBox.add(resultTable)
    }

    void clearSqlConsole() {

    }
}