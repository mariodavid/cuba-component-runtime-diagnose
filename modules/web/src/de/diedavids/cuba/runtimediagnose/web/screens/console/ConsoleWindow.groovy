package de.diedavids.cuba.runtimediagnose.web.screens.console

import com.haulmont.chile.core.model.MetaClass
import com.haulmont.chile.core.model.MetaProperty
import com.haulmont.cuba.core.global.Messages
import com.haulmont.cuba.core.global.Stores
import com.haulmont.cuba.gui.UiComponents
import com.haulmont.cuba.gui.WindowParam
import com.haulmont.cuba.gui.components.*
import com.haulmont.cuba.gui.components.actions.ExcelAction
import com.haulmont.cuba.gui.data.DsBuilder
import com.haulmont.cuba.gui.data.impl.ValueCollectionDatasourceImpl
import de.diedavids.cuba.runtimediagnose.SqlConsoleSecurityException
import de.diedavids.cuba.runtimediagnose.db.DbDiagnoseService
import de.diedavids.cuba.runtimediagnose.db.DbQueryResult
import de.diedavids.cuba.runtimediagnose.diagnose.DiagnoseExecutionFactory
import de.diedavids.cuba.runtimediagnose.diagnose.DiagnoseType
import de.diedavids.cuba.runtimediagnose.web.screens.diagnose.DiagnoseFileDownloader

import javax.inject.Inject

class ConsoleWindow extends AbstractConsoleWindow {

    @Inject
    DbDiagnoseService dbDiagnoseService

    @Inject
    UiComponents uiComponents

    @Inject
    protected BoxLayout resultTableBox

    protected Table resultTable

    @Inject
    ButtonsPanel resultButtonPanel

    @Inject
    protected Button excelButton

    @Inject
    DiagnoseExecutionFactory diagnoseExecutionFactory

    @Inject
    DiagnoseFileDownloader diagnoseFileDownloader

    @Inject
    Messages messages

    @Override
    DiagnoseType getDiagnoseType() {
        this.diagnoseType
    }

    @Inject
    SourceCodeEditor console

    @WindowParam(name='diagnoseType')
    protected DiagnoseType diagnoseType

    @Inject
    LookupField<String> dataStoreLookupField

    @Override
    void init(Map<String, Object> params) {
        super.init(params)

        this.setHeightFull()
        this.setWidthFull()

        initDataStoreField()
    }


    protected void initDataStoreField() {
        Map<String, String> dataStores = [:]
        dataStores.put(
                messages.getMessage('de.diedavids.cuba.runtimediagnose.web.screens.console', 'dataStoreMain'),
                Stores.MAIN
        )

        Stores.additional.each { String additional ->
            dataStores.put(additional, additional)
        }

        dataStoreLookupField.optionsMap = dataStores
    }

    @SuppressWarnings('UnnecessaryGetter')
    @Override
    void doRunConsole() {
        try {
            DbQueryResult result = dbDiagnoseService.runSqlDiagnose(
                    console.value,
                    getDiagnoseType(),
                    dataStoreLookupField.value
            )

            if (result.empty) {
                showNotification(formatMessage('executionSuccessful'), NotificationType.TRAY)
            }
            else {
                ValueCollectionDatasourceImpl sqlResultDs = createDatasource(result)
                createResultTable(sqlResultDs)
            }
        }
        catch (SqlConsoleSecurityException e) {
            showNotification(e.message, NotificationType.ERROR)
        }
    }

    @Override
    void clearConsoleResult() {
        resultTableBox.remove(resultTable)
        excelButton.enabled = false
    }

    private ValueCollectionDatasourceImpl createDatasource(DbQueryResult result) {
        ValueCollectionDatasourceImpl sqlResultDs = creteValueCollectionDs()
        result.entities.each { sqlResultDs.includeItem(it) }
        result.columns.each { sqlResultDs.addProperty(it) }
        sqlResultDs
    }

    private ValueCollectionDatasourceImpl creteValueCollectionDs() {
        DsBuilder.create(dsContext).reset().setAllowCommit(false)
                .buildValuesCollectionDatasource()
    }

    private Table createResultTable(ValueCollectionDatasourceImpl sqlResultDs) {
        if (resultTable) {
            resultTableBox.remove(resultTable)
        }
        resultTable = uiComponents.create(Table)
        resultTable.frame = frame

        addTableColumns(sqlResultDs, resultTable)

        resultTable.datasource = sqlResultDs
        resultTable.setSizeFull()
        resultTableBox.add(resultTable)

        configureExcelButton(resultTable)

        resultTable
    }

    private void configureExcelButton(Table resultTable) {
        excelButton.enabled = true
        excelButton.action = new ExcelAction(resultTable)
    }

    private void addTableColumns(ValueCollectionDatasourceImpl sqlResultDs, Table resultTable) {
        MetaClass meta = sqlResultDs.metaClass
        for (MetaProperty metaProperty : meta.properties) {
            Table.Column column = new Table.Column(meta.getPropertyPath(metaProperty.name))
            column.caption = metaProperty.name
            resultTable.addColumn(column)
        }
    }
}
