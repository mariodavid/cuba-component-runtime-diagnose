package de.diedavids.cuba.console.service

import com.haulmont.bali.db.QueryRunner
import com.haulmont.bali.db.ResultSetHandler
import com.haulmont.cuba.core.Persistence
import com.haulmont.cuba.core.entity.KeyValueEntity
import org.springframework.stereotype.Service

import javax.inject.Inject
import java.sql.ResultSet
import java.sql.ResultSetMetaData
import java.sql.SQLException

@Service(SqlConsoleService.NAME)
public class SqlConsoleServiceBean implements SqlConsoleService {

    @Inject
    Persistence persistence

    @Override
    Map<String, Object> executeSqlKvE(String sqlString) {
        QueryRunner runner = new QueryRunner(persistence.getDataSource());
        try {

            def columns = []
            def entities = []
            runner.query(sqlString,
                    new ResultSetHandler<Set<String>>() {
                        public Set<String> handle(ResultSet rs) throws SQLException {

                            ResultSetMetaData rsmd = rs.getMetaData();
                            for (int i = 1; i <=
                                    rsmd.columnCount; i++) {
                                columns << [name: rsmd.getColumnLabel(i)]
                            }

                            Set<String> rows = new HashSet<String>();
                            while (rs.next()) {

                                def keyValueEntity = new KeyValueEntity()

                                for (int i = 1; i <=
                                        rsmd.columnCount; i++) {
                                    def column = rsmd.getColumnLabel(i)
                                    def value = rs.getString(i)
                                    keyValueEntity.setValue(column, value)
                                }

                                entities << keyValueEntity
                            }
                            return rows;
                        }
                    });
            return [entities: entities, columns: columns];
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}