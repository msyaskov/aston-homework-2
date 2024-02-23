package aston.hw2.configuration;

import aston.hw2.context.AbstractObjectContainerBuilder;
import aston.hw2.mapper.CuratorMapper;
import aston.hw2.mapper.GroupMapper;
import aston.hw2.mapper.StudentMapper;
import aston.hw2.repository.*;
import aston.hw2.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.Objects;

// Максим, контекст... моей почтение (тоном Гоблина)
// Молодец, очень хорошо постарался
public class ProductionObjectContainerBuilder extends AbstractObjectContainerBuilder {

    @Override
    protected void configure() {
        final ObjectMapper objectMapper = Objects.requireNonNullElseGet(get(ObjectMapper.class), () -> {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true);

            return mapper;
        });

        final GroupMapper groupMapper = Objects.requireNonNullElseGet(get(GroupMapper.class), GroupMapper::new);
        final StudentMapper studentMapper = Objects.requireNonNullElseGet(get(StudentMapper.class), StudentMapper::new);
        final CuratorMapper curatorMapper = Objects.requireNonNullElseGet(get(CuratorMapper.class), CuratorMapper::new);

        final JdbcConnectionFactory jdbcConnectionFactory = Objects.requireNonNullElseGet(get(JdbcConnectionFactory.class),
                () -> new JdbcConnectionFactory("org.postgresql.Driver", "jdbc:postgresql://db:5432/aston_hw2_db", "user", "pass"));

        final GroupRepository groupRepository = Objects.requireNonNullElseGet(get(GroupRepository.class),
                () -> new JdbcGroupRepository(jdbcConnectionFactory));

        final StudentRepository studentRepository = Objects.requireNonNullElseGet(get(StudentRepository.class),
                () -> new JdbcStudentRepository(jdbcConnectionFactory, groupRepository));

        final CuratorRepository curatorRepository = Objects.requireNonNullElseGet(get(CuratorRepository.class),
                () -> new JdbcCuratorRepository(jdbcConnectionFactory, groupRepository));

        final GroupService groupService = Objects.requireNonNullElseGet(get(GroupService.class),
                () -> new DefaultGroupService(groupRepository, curatorRepository, studentRepository));

        final StudentService studentService = Objects.requireNonNullElseGet(get(StudentService.class),
                () -> new DefaultStudentService(studentRepository, groupRepository));

        final CuratorService curatorService = Objects.requireNonNullElseGet(get(CuratorService.class),
                () -> new DefaultCuratorService(curatorRepository, groupRepository));

        add(objectMapper, ObjectMapper.class);
        add(groupService, GroupService.class);
        add(curatorService, CuratorService.class);
        add(studentService, StudentService.class);
        add(groupMapper, GroupMapper.class);
        add(curatorMapper, CuratorMapper.class);
        add(studentMapper, StudentMapper.class);
    }

}
