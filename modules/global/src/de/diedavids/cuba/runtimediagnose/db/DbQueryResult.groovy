package de.diedavids.cuba.runtimediagnose.db

import com.haulmont.cuba.core.entity.KeyValueEntity

class DbQueryResult implements Serializable {

    private static final long serialVersionUID = -8288812447591918153L

    Collection<String> columns = []

    Collection<KeyValueEntity> entities = []

    boolean isEmpty() {
        columns.empty && entities.empty
    }

    void addColumn(String column) {
        columns << column
    }

    void addEntity(KeyValueEntity entity) {
        entities << entity
    }
}
