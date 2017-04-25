package de.diedavids.cuba.runtimediagnose.sql

import com.haulmont.cuba.core.entity.KeyValueEntity

class SqlSelectResult implements Serializable {

    private static final long serialVersionUID = -8288812447591918153L

    Collection<String> columns = []

    Collection<KeyValueEntity> entities = []

    void addColumn(String column) {
        columns << column
    }

    void addEntity(KeyValueEntity entity) {
        entities << entity
    }
}
