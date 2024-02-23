package aston.hw2.repository;

import aston.hw2.entity.Curator;
import aston.hw2.entity.Group;

import java.sql.*;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Репозиторий кураторов для работы с базой данных (БД) посредством JDBC.
 *
 * Все операции с кураторами, которые связаны с какой-либо группой, а также логику управления связями делегирует {@link JdbcGroupRepository}.
 *
 * @author Максим Яськов
 * @see JdbcGroupRepository
 */
public class JdbcCuratorRepository extends JdbcAbstractRepository<Curator, Integer> implements CuratorRepository {

    private final GroupRepository groupRepository;

    public JdbcCuratorRepository(JdbcConnectionFactory jdbcConnectionFactory, GroupRepository groupRepository) {
        super(jdbcConnectionFactory);
        this.groupRepository = groupRepository;
    }

    @Override
    public Stream<Curator> findAll() {
        return useConnection(connection -> {
            final Stream.Builder<Curator> streamBuilder = Stream.builder();

            try (PreparedStatement ps = connection.prepareStatement(SQLNamespace.Query.SELECT_CURATORS_BY_NULL_GROUP_ID);
                 ResultSet rs = ps.executeQuery() // можно так, чтобы вложенность уменьшить
            ) {
                while (rs.next()) {
                    streamBuilder.add(readCurator(rs));
                }
            }

            return Stream.concat(streamBuilder.build(),
                    groupRepository.findAll()
                            .map(Group::getCurator)
                            .filter(Objects::nonNull));
        });
    }

    @Override
    public Curator findById(Integer id) {
        checkIdForNull(id);

        return useConnection(connection -> {
            int groupId;

            try (PreparedStatement ps = connection.prepareStatement(SQLNamespace.Query.SELECT_CURATOR_BY_ID)) {
                ps.setInt(1, id);

                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return null;
                    }

                    groupId = rs.getInt(SQLNamespace.Curator.KEY_GROUP_ID);
                    if (rs.wasNull()) {
                        return readCurator(rs);
                    }

                    return groupRepository.findById(groupId).getCurator();
                }
            }
        });
    }

    private Curator insert(Curator curator) {
        return useConnection(connection -> {
            if (curator.getGroup() == null) {
                try (PreparedStatement ps = connection.prepareStatement(SQLNamespace.Query.INSERT_CURATOR, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, curator.getName());
                    ps.setString(2, curator.getEmail());
                    ps.setInt(3, curator.getExperience());
                    ps.setNull(4, JDBCType.INTEGER.getVendorTypeNumber());
                    ps.executeUpdate();

                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) {
                            curator.setId(rs.getInt(1));
                        }
                    }
                }
            } else {
                curator.getGroup().setCurator(curator);
                groupRepository.save(curator.getGroup());
            }

            return curator;
        });
    }

    private Curator readCurator(ResultSet rs) throws SQLException {
        Curator curator = new Curator();
        curator.setId(rs.getInt(SQLNamespace.Curator.KEY_ID));
        curator.setName(rs.getString(SQLNamespace.Curator.KEY_NAME));
        curator.setEmail(rs.getString(SQLNamespace.Curator.KEY_EMAIL));
        curator.setExperience(rs.getInt(SQLNamespace.Curator.KEY_EXPERIENCE));

        return curator;
    }

    @Override
    public Curator removeById(Integer id) {
        checkIdForNull(id);

        Curator curator = findById(id);
        if (curator == null) {
            return null;
        }

        useConnection(connection -> {
            try (PreparedStatement ps = connection.prepareStatement(SQLNamespace.Query.DELETE_CURATOR_BY_ID)) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
        });

        return curator;
    }

    @Override
    public Curator save(Curator curator) {
        checkEntityForNull(curator);

        if (curator.getId() == null) {
            return insert(curator);
        } else {
            return update(curator);
        }
    }

    private Curator update(Curator curator) {
        return useConnection(connection -> {
            if (curator.getGroup() == null) {
                try (PreparedStatement ps = connection.prepareStatement(SQLNamespace.Query.UPDATE_CURATOR)) {
                    ps.setString(1, curator.getName());
                    ps.setString(2, curator.getEmail());
                    ps.setInt(3, curator.getExperience());
                    ps.setNull(4, JDBCType.INTEGER.getVendorTypeNumber());

                    ps.setInt(5, curator.getId());
                    ps.executeUpdate();
                }
            } else {
                curator.getGroup().setCurator(curator);
                groupRepository.save(curator.getGroup());
            }

            return curator;
        });
    }
}
