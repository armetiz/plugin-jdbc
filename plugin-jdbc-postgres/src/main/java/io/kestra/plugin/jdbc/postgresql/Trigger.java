package io.kestra.plugin.jdbc.postgresql;

import io.kestra.core.models.annotations.Example;
import io.kestra.core.models.annotations.Plugin;
import io.kestra.core.runners.RunContext;
import io.kestra.plugin.jdbc.AbstractJdbcQuery;
import io.kestra.plugin.jdbc.AbstractJdbcTrigger;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@Schema(
    title = "Wait for query on a PostgreSQL database."
)
@Plugin(
    examples = {
        @Example(
            title = "Wait for a sql query to return results and iterate through rows",
            full = true,
            code = {
                "id: jdbc-trigger",
                "namespace: io.kestra.tests",
                "",
                "tasks:",
                "  - id: each",
                "    type: io.kestra.core.tasks.flows.EachSequential",
                "    tasks:",
                "      - id: return",
                "        type: io.kestra.core.tasks.debugs.Return",
                "        format: \"{{json(taskrun.value)}}\"",
                "    value: \"{{ trigger.rows }}\"",
                "",
                "triggers:",
                "  - id: watch",
                "    type: io.kestra.plugin.jdbc.postgresql.Trigger",
                "    interval: \"PT5M\"",
                "    sql: \"SELECT * FROM my_table\""
            }
        )
    }
)
public class Trigger extends AbstractJdbcTrigger implements PostgresConnectionInterface {
    @Builder.Default
    protected Boolean ssl = false;
    protected PostgresConnectionInterface.SslMode sslMode;
    protected String sslRootCert;
    protected String sslCert;
    protected String sslKey;
    protected String sslKeyPassword;

    @Override
    protected AbstractJdbcQuery.Output runQuery(RunContext runContext) throws Exception {
        var query = Query.builder()
            .id(this.id)
            .type(Query.class.getName())
            .url(this.getUrl())
            .username(this.getUsername())
            .password(this.getPassword())
            .timeZoneId(this.getTimeZoneId())
            .sql(this.getSql())
            .fetch(this.isFetch())
            .store(this.isStore())
            .fetchOne(this.isFetchOne())
            .additionalVars(this.additionalVars)
            .build();
        return query.run(runContext);
    }

    @Override
    public void registerDriver() throws SQLException {
        DriverManager.registerDriver(new org.postgresql.Driver());
    }
}
