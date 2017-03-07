package de.diedavids.cuba.console.sql

import com.haulmont.cuba.core.entity.KeyValueEntity
import org.springframework.stereotype.Component

import java.sql.ResultSet
import java.sql.ResultSetMetaData

@Component(SqlSelectResultFactory.NAME)
class SqlSelectResultFactoryBean implements SqlSelectResultFactory {

    @Override
    SqlSelectResult createFromResultSet(ResultSet resultSet) {

        def result = new SqlSelectResult()
        ResultSetMetaData metadata = resultSet.getMetaData();
        int numberOfColumns = metadata.columnCount

        createColumns(numberOfColumns, result, metadata)

        while (resultSet.next()) {
            createKeyValueEntities(numberOfColumns, metadata, resultSet, result)
        }

        result
    }

    private void createKeyValueEntities(int numberOfColumns, metadata, rs, SqlSelectResult result) {
        def keyValueEntity = new KeyValueEntity()

        numberOfColumns.times { i ->
            def columnIndex = i + 1
            def column = metadata.getColumnLabel(columnIndex)
            def value = rs.getString(columnIndex)
            keyValueEntity.setValue(column, value)
        }

        result.addEntity(keyValueEntity)
    }

    private createColumns(int numberOfColumns, result, metadata) {
        numberOfColumns.times { i ->
            def columnIndex = i + 1
            result.addColumn(metadata.getColumnLabel(columnIndex))
        }
    }
}
