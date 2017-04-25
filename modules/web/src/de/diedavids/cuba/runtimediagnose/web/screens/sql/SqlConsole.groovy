package de.diedavids.cuba.runtimediagnose.web.screens.sql

import com.haulmont.chile.core.model.MetaClass
import com.haulmont.chile.core.model.MetaProperty
import com.haulmont.cuba.core.global.GlobalConfig
import com.haulmont.cuba.core.global.Metadata
import com.haulmont.cuba.gui.components.*
import com.haulmont.cuba.gui.components.actions.ExcelAction
import com.haulmont.cuba.gui.data.DsBuilder
import com.haulmont.cuba.gui.data.impl.ValueCollectionDatasourceImpl
import com.haulmont.cuba.gui.export.ExportDisplay
import com.haulmont.cuba.gui.theme.ThemeConstants
import com.haulmont.cuba.gui.xml.layout.ComponentsFactory
import de.diedavids.cuba.runtimediagnose.SqlConsoleSecurityException
import de.diedavids.cuba.runtimediagnose.diagnose.DiagnoseExecutionFactory
import de.diedavids.cuba.runtimediagnose.diagnose.DiagnoseType
import de.diedavids.cuba.runtimediagnose.sql.SqlDiagnoseService
import de.diedavids.cuba.runtimediagnose.sql.SqlSelectResult
import de.diedavids.cuba.runtimediagnose.web.screens.diagnose.DiagnoseFileDownloader

import javax.inject.Inject

class SqlConsole extends AbstractWindow {


    @Inject
    SourceCodeEditor sqlConsole

    @Inject
    SqlDiagnoseService sqlDiagnoseService

    @Inject
    TabSheet.Tab consoleResultTabResult

    @Inject
    SplitPanel consoleResultSplitter

    @Inject
    Metadata metadata

    @Inject
    GlobalConfig globalConfig

    @Inject
    protected ExportDisplay exportDisplay

    @Inject
    ComponentsFactory componentsFactory

    @Inject
    protected BoxLayout resultTableBox

    protected Table resultTable


    @Inject
    protected ThemeConstants themeConstants

    @Inject
    ButtonsPanel resultButtonPanel


    @Inject
    protected Button excelButton


    @Inject
    DiagnoseExecutionFactory diagnoseExecutionFactory
    @Inject
    DiagnoseFileDownloader diagnoseFileDownloader


    void runSqlConsole() {
        try {
            SqlSelectResult result = sqlDiagnoseService.runSqlDiagnose(sqlConsole.value)
            ValueCollectionDatasourceImpl sqlResultDs = createDatasource(result)
            createResultTable(sqlResultDs)
        }
        catch (SqlConsoleSecurityException e) {
            showNotification(e.message, Frame.NotificationType.ERROR)
        }

    }

    private ValueCollectionDatasourceImpl createDatasource(SqlSelectResult result) {
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


    void clearSqlConsole() {
        sqlConsole.value = null
    }

    void downloadDiagnoseRequestFile() {
        def diagnoseExecution = diagnoseExecutionFactory.createAdHocDiagnoseExecution(sqlConsole.value, DiagnoseType.SQL)
        def zipBytes = diagnoseExecutionFactory.createDiagnoseRequestFileFormDiagnoseExecution(diagnoseExecution)
        diagnoseFileDownloader.downloadFile(this, zipBytes, 'diagnose.zip')

    }
}