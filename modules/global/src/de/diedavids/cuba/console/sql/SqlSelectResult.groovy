package de.diedavids.cuba.console.sql

import com.haulmont.cuba.core.entity.KeyValueEntity

class SqlSelectResult implements Serializable {

    Collection<String> columns = []

    Collection<KeyValueEntity> entities = []

    void addColumn(String column) {
        columns << column
    }

    void addEntity(KeyValueEntity entity) {
        entities << entity
    }
}
