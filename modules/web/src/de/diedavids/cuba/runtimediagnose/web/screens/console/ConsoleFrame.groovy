package de.diedavids.cuba.runtimediagnose.web.screens.console

import com.haulmont.chile.core.model.MetaClass
import com.haulmont.chile.core.model.MetaProperty
import com.haulmont.cuba.gui.WindowParam
import com.haulmont.cuba.gui.components.*
import com.haulmont.cuba.gui.components.actions.ExcelAction
import com.haulmont.cuba.gui.data.DsBuilder
import com.haulmont.cuba.gui.data.impl.ValueCollectionDatasourceImpl
import com.haulmont.cuba.gui.xml.layout.ComponentsFactory
import de.diedavids.cuba.runtimediagnose.SqlConsoleSecurityException
import de.diedavids.cuba.runtimediagnose.diagnose.DiagnoseExecutionFactory
import de.diedavids.cuba.runtimediagnose.diagnose.DiagnoseType
import de.diedavids.cuba.runtimediagnose.sql.DbDiagnoseService
import de.diedavids.cuba.runtimediagnose.sql.DbQueryResult
import de.diedavids.cuba.runtimediagnose.web.screens.diagnose.DiagnoseFileDownloader

import javax.inject.Inject

class ConsoleFrame extends ConsoleWindow {

    @Inject
    DbDiagnoseService dbDiagnoseService

    @Inject
    ComponentsFactory componentsFactory

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

    @Override
    DiagnoseType getDiagnoseType() {
        this.diagnoseType
    }

    @Inject
    SourceCodeEditor console

    @WindowParam(name='diagnoseType')
    protected DiagnoseType diagnoseType

    @Override
    void init(Map<String, Object> params) {
        super.init(params)

        this.setHeightFull()
        this.setWidthFull()
    }

    @Override
    void doRunConsole() {
        try {
            DbQueryResult result = dbDiagnoseService.runSqlDiagnose(console.value, diagnoseType)
            ValueCollectionDatasourceImpl sqlResultDs = createDatasource(result)
            createResultTable(sqlResultDs)
        }
        catch (SqlConsoleSecurityException e) {
            showNotification(e.message, Frame.NotificationType.ERROR)
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
        resultTable = componentsFactory.createComponent(Table)
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
