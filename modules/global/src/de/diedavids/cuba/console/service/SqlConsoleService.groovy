package de.diedavids.cuba.console.service

import com.haulmont.cuba.core.entity.KeyValueEntity


interface SqlConsoleService {
    String NAME = "console_SqlConsoleService";

    Map<String, Object> executeSqlKvE(String sqlString)
}