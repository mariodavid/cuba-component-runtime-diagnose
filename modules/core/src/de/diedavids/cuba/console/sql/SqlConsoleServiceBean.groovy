package de.diedavids.cuba.console.sql

import com.haulmont.bali.db.QueryRunner
import com.haulmont.bali.db.ResultSetHandler
import com.haulmont.cuba.core.Persistence
import org.springframework.stereotype.Service

import javax.inject.Inject
import java.sql.ResultSet
import java.sql.SQLException

@Service(SqlConsoleService.NAME)
public class SqlConsoleServiceBean implements SqlConsoleService {

    @Inject
    Persistence persistence

    @Inject
    SqlSelectResultFactory selectResultFactory

    @Override
    SqlSelectResult executeSql(String sqlString) {
        QueryRunner runner = new QueryRunner(persistence.getDataSource());
        try {
            def result = null
            runner.query(sqlString,
                    new ResultSetHandler<Set<String>>() {
                        public Set<String> handle(ResultSet rs) throws SQLException {
                            result = selectResultFactory.createFromResultSet(rs)
                            null
                        }
                    });
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}