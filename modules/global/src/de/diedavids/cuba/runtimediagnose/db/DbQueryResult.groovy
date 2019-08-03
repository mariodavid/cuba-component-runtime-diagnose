package de.diedavids.cuba.runtimediagnose.db

import com.haulmont.cuba.core.entity.KeyValueEntity

class DbQueryResult implements Serializable {

    private static final long serialVersionUID = -8288812447591918153L

    Collection<String> columns = []

    Collection<KeyValueEntity> entities = []
    private final String NEW_LINE = '\n'
    private final String CSV_SEPERATOR = ','

    boolean isEmpty() {
        columns.empty && entities.empty
    }

    void addColumn(String column) {
        columns << column
    }

    void addEntity(KeyValueEntity entity) {
        entities << entity
    }

    String resultMessage() {
        empty ? 'Execution successful' : entities[0].toString()
    }

    String toCSV() {
        [
                headerString(),
                entities.collect { entity ->
                    entityToCSV(entity)
                }
        ].flatten().join(NEW_LINE)
    }

    private String headerString() {
        columns.collect { "\"$it\"" }.join(CSV_SEPERATOR)
    }

    String entityToCSV(KeyValueEntity entity) {

        def valueColumn = columns.collect { column ->
            entityColumnString(entity, column)
        }

        valueColumn.join(CSV_SEPERATOR)
    }

    private static String entityColumnString(KeyValueEntity entity, String column) {
        def value = entity.getValue(column)

        (value && (value != 'null')) ? "\"$value\"" : '""'
    }
}
