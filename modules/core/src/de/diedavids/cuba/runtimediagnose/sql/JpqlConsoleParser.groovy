package de.diedavids.cuba.runtimediagnose.sql

import com.haulmont.cuba.core.global.AppBeans
import com.haulmont.cuba.core.global.QueryParserAstBased
import com.haulmont.cuba.core.sys.jpql.DomainModel
import com.haulmont.cuba.core.sys.jpql.DomainModelBuilder
import org.springframework.stereotype.Component

@Component
class JpqlConsoleParser {

    void analyseJpql(String queryString) {
        QueryParserAstBased parser = new QueryParserAstBased(ScriptManagerUtilsHolder.domainModelInstance, queryString)
        parser.queryPaths
    }

    private static class ScriptManagerUtilsHolder {
        private static class ScriptManagerUtilLazyHolder {
            public static final DomainModel DOMAIN_MODEL_INSTANCE = AppBeans.get(DomainModelBuilder).produce()
        }
        static DomainModel getDomainModelInstance() {
            ScriptManagerUtilLazyHolder.DOMAIN_MODEL_INSTANCE
        }
    }
}
