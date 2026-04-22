package backend.academy.linktracker.scrapper.repository.impl.jdbc;

import backend.academy.linktracker.scrapper.dto.UpdateLinkDto;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Component;

/**
 * ДИСКЛЕЙМЕР
 * Этот класс существует только по тому, что по тз нужно разделять
 * jdbc и orm, но каждый знает, что оптимальнее всего - использовать
 * orm + jdbc в солжных кейсах и так как я не могу некоторые методы
 * занести в jdbcRepo(который создаётся в jdbc конфигурации), я сделаю
 * это здесь
 */
@Component
@RequiredArgsConstructor
public class JdbcHelper {

    private final DataSource dataSource;

    // language=sql
    private static final String UPDATE_LINK_LAST_UPD_BATCH = """
        UPDATE links SET latest_update_time = ? WHERE link_id = ?
        """;

    public void updateLastUpdateBatch(List<UpdateLinkDto> batch, int batchSize) {

        Connection connection = DataSourceUtils.getConnection(dataSource);

        try (PreparedStatement ps = connection.prepareStatement(UPDATE_LINK_LAST_UPD_BATCH)) {
            int count = 0;

            for (UpdateLinkDto dto : batch) {
                ps.setObject(1, dto.updatedAt());
                ps.setLong(2, dto.linkId());
                ps.addBatch();

                if (++count % batchSize == 0) {
                    ps.executeBatch();
                }
            }

            ps.executeBatch();
        } catch (SQLException e) {
            throw new DataAccessException("Unexpected SQL exception while updating batch of links", e) {};
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }
}
