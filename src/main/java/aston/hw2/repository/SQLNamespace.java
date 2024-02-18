package aston.hw2.repository;

import java.sql.PreparedStatement;

public abstract class SQLNamespace {

    private SQLNamespace() {}

    public static abstract class Student {

        public static final String TABLE_NAME = "students";
        public static final String KEY_ID = "id";
        public static final String KEY_NAME = "name";
        public static final String KEY_DATE_OF_BIRTH = "date_of_birth";
        public static final String KEY_GROUP_ID = "group_id";

        public static final String FULL_KEY_ID = "%s.%s".formatted(TABLE_NAME, KEY_ID);
        public static final String FULL_KEY_NAME = "%s.%s".formatted(TABLE_NAME, KEY_NAME);
        public static final String FULL_KEY_DATE_OF_BIRTH = "%s.%s".formatted(TABLE_NAME, KEY_DATE_OF_BIRTH);
        public static final String FULL_KEY_GROUP_ID = "%s.%s".formatted(TABLE_NAME, KEY_GROUP_ID);

        private Student() {}
    }

    public static abstract class Group {

        public static final String TABLE_NAME = "groups";
        public static final String KEY_ID = "id";
        public static final String KEY_NAME = "name";
        public static final String KEY_GRADUATION_DATE = "graduation_date";

        public static final String FULL_KEY_ID = "%s.%s".formatted(TABLE_NAME, KEY_ID);
        public static final String FULL_KEY_NAME = "%s.%s".formatted(TABLE_NAME, KEY_NAME);
        public static final String FULL_KEY_GRADUATION_DATE = "%s.%s".formatted(TABLE_NAME, KEY_GRADUATION_DATE);

        private Group() {}
    }

    public static abstract class Curator {

        public static final String TABLE_NAME = "curators";
        public static final String KEY_ID = "id";
        public static final String KEY_NAME = "name";
        public static final String KEY_EMAIL = "email";
        public static final String KEY_EXPERIENCE = "experience";
        public static final String KEY_GROUP_ID = "group_id";

        public static final String FULL_KEY_ID = "%s.%s".formatted(TABLE_NAME, KEY_ID);
        public static final String FULL_KEY_NAME = "%s.%s".formatted(TABLE_NAME, KEY_NAME);
        public static final String FULL_KEY_EMAIL = "%s.%s".formatted(TABLE_NAME, KEY_EMAIL);
        public static final String FULL_KEY_EXPERIENCE = "%s.%s".formatted(TABLE_NAME, KEY_EXPERIENCE);
        public static final String FULL_KEY_GROUP_ID = "%s.%s".formatted(TABLE_NAME, KEY_GROUP_ID);

        private Curator() {}
    }

    public static abstract class Query {

        /**
         * SQL-запрос на удаление группы по идентификатору.
         *
         * Запрос ДОЛЖЕН содержать следующие параметры:
         *  1. SQLNamespace.Group.KEY_ID
         *
         * @see JdbcGroupRepository#removeById
         */
        public static final String DELETE_GROUP_BY_ID = """
                DELETE
                FROM %s
                WHERE %s = ?
            """.formatted(Group.TABLE_NAME, Group.KEY_ID);

        /**
         * SQL-запрос на удаление студента по идентификатору.
         *
         * Запрос ДОЛЖЕН содержать следующие параметры:
         *  1. SQLNamespace.Student.KEY_ID
         *
         * @see JdbcStudentRepository#removeById
         */
        public static final String DELETE_STUDENT_BY_ID = """
                DELETE
                FROM %s
                WHERE %s = ?
            """.formatted(Student.TABLE_NAME, Student.KEY_ID);

        /**
         * SQL-запрос на удаление студента по идентификатору.
         *
         * Запрос ДОЛЖЕН содержать следующие параметры:
         *  1. SQLNamespace.Student.KEY_ID
         *
         * @see JdbcStudentRepository#removeById
         */
        public static final String DELETE_CURATOR_BY_ID = """
                DELETE
                FROM %s
                WHERE %s = ?
            """.formatted(Curator.TABLE_NAME, Curator.KEY_ID);

        /**
         * SQL-запрос на вставку только группы.
         *
         * Запрос ДОЛЖЕН содержать следующие параметры:
         *  1. SQLNamespace.Group.KEY_NAME
         *  2. SQLNamespace.Group.KEY_GRADUATION_DATE
         *
         * идентификатор вставленной группы должен быть доступен через {@link PreparedStatement#getGeneratedKeys()}.
         *
         * @see JdbcGroupRepository#save
         */
        public static final String INSERT_GROUP = """
                INSERT INTO %s
                    (%s, %s)
                VALUES (?, ?);
            """.formatted(Group.TABLE_NAME, Group.KEY_NAME, Group.KEY_GRADUATION_DATE);

        /**
         * SQL-запрос на вставку только студента.
         *
         * Запрос ДОЛЖЕН содержать следующие параметры:
         *  1. SQLNamespace.Student.KEY_NAME
         *  2. SQLNamespace.Student.KEY_DATE_OF_BIRTH
         *
         * идентификатор вставленного студента должен быть доступен через {@link PreparedStatement#getGeneratedKeys()}.
         *
         * @see JdbcStudentRepository#save
         */
        public static final String INSERT_STUDENT = """
                INSERT INTO %s
                    (%s, %s, %s)
                VALUES (?, ?, ?);
            """.formatted(Student.TABLE_NAME, Student.KEY_NAME, Student.KEY_DATE_OF_BIRTH, Student.KEY_GROUP_ID);

        public static final String INSERT_CURATOR = """
                INSERT INTO %s
                    (%s, %s, %s, %s)
                VALUES (?, ?, ?, ?);
            """.formatted(Curator.TABLE_NAME, Curator.KEY_NAME, Curator.KEY_EMAIL, Curator.KEY_EXPERIENCE, Curator.KEY_GROUP_ID);

        /**
         * SQL-запрос на получение всех групп и соответствующих им кураторов.
         *
         * Запрос ДОЛЖЕН возвращать следующие свойства:
         *  * SQLNamespace.Group.KEY_ID
         *  * SQLNamespace.Group.KEY_NAME
         *  * SQLNamespace.Group.KEY_GRADUATION_DATE
         *  * SQLNamespace.Curator.KEY_ID
         *  * SQLNamespace.Curator.KEY_NAME
         *  * SQLNamespace.Curator.KEY_EMAIL
         *  * SQLNamespace.Curator.KEY_EXPERIENCE
         *
         * @see JdbcGroupRepository#findAll
         */
        public static final String SELECT_ALL_GROUPS_WITH_CURATORS = """
                SELECT %s, %s, %s, %s, %s, %s, %s
                FROM %s
                    LEFT JOIN %s ON %s = %s;
            """.formatted(Group.FULL_KEY_ID, Group.FULL_KEY_NAME, Group.FULL_KEY_GRADUATION_DATE, Curator.FULL_KEY_ID, Curator.FULL_KEY_NAME, Curator.FULL_KEY_EMAIL, Curator.FULL_KEY_EXPERIENCE, Group.TABLE_NAME, Curator.TABLE_NAME, Group.FULL_KEY_ID, Curator.FULL_KEY_GROUP_ID);

        public static final String SELECT_CURATORS_BY_NULL_GROUP_ID = """
                SELECT *
                FROM %s
                WHERE %s IS NULL
            """.formatted(Curator.TABLE_NAME, Curator.KEY_GROUP_ID);

        public static final String SELECT_STUDENT_BY_ID = """
                SELECT *
                FROM %s
                WHERE %s = ?;
            """.formatted(Student.TABLE_NAME, Student.KEY_ID);

        public static final String SELECT_GROUP_ID_BY_STUDENT_ID = """
                SELECT %s
                FROM %s
                    LEFT JOIN %s ON %s = %s
                WHERE %s = ?;
            """.formatted(Group.KEY_ID, Group.TABLE_NAME, Student.TABLE_NAME, Group.KEY_ID, Student.KEY_GROUP_ID, Student.KEY_ID);

        /**
         * SQL-запрос на получение студента по идентификатору вместе с его группой и куратором.
         *
         * Запрос ДОЛЖЕН содержать один параметр:
         *  1. SQLNamespace.Student.KEY_ID
         *
         * Запрос ДОЛЖЕН возвращать следующие свойства:
         *  * SQLNamespace.Student.KEY_NAME
         *  * SQLNamespace.Student.KEY_DATE_OF_BIRTH
         *  * SQLNamespace.Group.KEY_ID
         *  * SQLNamespace.Group.KEY_NAME
         *  * SQLNamespace.Group.KEY_GRADUATION_DATE
         *  * SQLNamespace.Curator.KEY_ID
         *  * SQLNamespace.Curator.KEY_NAME
         *  * SQLNamespace.Curator.KEY_EMAIL
         *  * SQLNamespace.Curator.KEY_EXPERIENCE
         *
         * @see JdbcStudentRepository#findById
         */
        public static final String SELECT_STUDENT_WITH_GROUP_WITH_CURATOR_BY_ID = """
                SELECT *
                FROM %s
                    LEFT JOIN %s ON %s = %s
                    LEFT JOIN %s ON %s = %s
                WHERE %s = ?
            """.formatted(Student.TABLE_NAME,
                Group.TABLE_NAME, Group.KEY_ID, Student.KEY_GROUP_ID,
                Curator.TABLE_NAME, Group.KEY_ID, Curator.KEY_GROUP_ID,
                Student.KEY_ID);

        /**
         * SQL-запрос на получение группы и куратора с ней связанного по идентификатору группы.
         *
         * Запрос ДОЛЖЕН содержать один параметр:
         *  1. SQLNamespace.Group.KEY_ID
         *
         * Запрос ДОЛЖЕН возвращать следующие свойства:
         *  * SQLNamespace.Group.KEY_NAME
         *  * SQLNamespace.Group.KEY_GRADUATION_DATE
         *  * SQLNamespace.Curator.KEY_ID
         *  * SQLNamespace.Curator.KEY_NAME
         *  * SQLNamespace.Curator.KEY_EMAIL
         *  * SQLNamespace.Curator.KEY_EXPERIENCE
         *  
         * @see JdbcGroupRepository#findById
         */
        public static final String SELECT_GROUP_WITH_CURATOR_BY_ID = """
                SELECT *
                FROM %s
                    LEFT JOIN %s ON %s = %s
                WHERE %s = ?;
            """.formatted(Group.TABLE_NAME, Curator.TABLE_NAME, Group.FULL_KEY_ID, Curator.FULL_KEY_GROUP_ID, Group.FULL_KEY_ID);

        public static final String SELECT_GROUP_ID_BY_NAME = """
                SELECT %s
                FROM %s
                WHERE %s = ?
            """.formatted(Group.KEY_ID, Group.TABLE_NAME, Group.KEY_NAME);

        /**
         * SQL-запрос на получение группы и куратора с ней связанного по имени группы.
         *
         * Запрос ДОЛЖЕН содержать один параметр:
         *  1. SQLNamespace.Group.KEY_NAME
         *
         * Запрос ДОЛЖЕН возвращать следующие свойства:
         *  * SQLNamespace.Group.KEY_ID
         *  * SQLNamespace.Group.KEY_GRADUATION_DATE
         *  * SQLNamespace.Curator.KEY_ID
         *  * SQLNamespace.Curator.KEY_NAME
         *  * SQLNamespace.Curator.KEY_EMAIL
         *  * SQLNamespace.Curator.KEY_EXPERIENCE
         *
         * @see JdbcGroupRepository#findById
         */
        public static final String SELECT_GROUP_WITH_CURATOR_BY_NAME = """
                SELECT *
                FROM %s
                    LEFT JOIN %s ON %s = %s
                WHERE %s = ?;
            """.formatted(Group.TABLE_NAME, Curator.TABLE_NAME, Group.KEY_ID, Curator.KEY_GROUP_ID, Group.KEY_NAME);

        public static final String SELECT_CURATOR_BY_ID = """
                SELECT *
                FROM %s
                WHERE %s = ?;
            """.formatted(Curator.TABLE_NAME, Curator.KEY_ID);

        /**
         * SQL-запрос на получение студентов по идентификатору группы.
         *
         * Запрос ДОЛЖЕН содержать один параметр:
         *  1. SQLNamespace.Student.KEY_GROUP_ID
         *
         * Запрос ДОЛЖЕН возвращать следующие свойства:
         *  * SQLNamespace.Student.KEY_ID
         *  * SQLNamespace.Student.KEY_NAME
         *  * SQLNamespace.Student.KEY_DATE_OF_BIRTH
         *
         * @see JdbcGroupRepository#findById
         */
        public static final String SELECT_STUDENTS_BY_GROUP_ID = """
                SELECT *
                FROM %s
                WHERE %s = ?;
            """.formatted(Student.TABLE_NAME, Student.KEY_GROUP_ID);

        public static final String SELECT_STUDENTS_BY_NULL_GROUP_ID = """
                SELECT *
                FROM %s
                WHERE %s IS NULL;
            """.formatted(Student.TABLE_NAME, Student.KEY_GROUP_ID);

        public static final String SELECT_CURATORS_BY_GROUP_ID = """
                SELECT *
                FROM %s
                WHERE %s = ?;
            """.formatted(Curator.TABLE_NAME, Curator.KEY_GROUP_ID);

        /**
         * SQL-запрос на обновление идентификатора группы куратора.
         *
         * Запрос ДОЛЖЕН содержать следующие параметры:
         *  1. SQLNamespace.Curator.KEY_GROUP_ID
         *  2. SQLNamespace.Curator.KEY_ID
         *
         * @see JdbcGroupRepository#save
         */
        public static final String UPDATE_CURATOR_GROUP_ID = """
                UPDATE %s
                SET %s = ?
                WHERE %s = ?
            """.formatted(Curator.TABLE_NAME, Curator.KEY_GROUP_ID, Curator.KEY_ID);

        /**
         * SQL-запрос на обновление идентификатора группы куратора.
         *
         * Запрос ДОЛЖЕН содержать следующие параметры:
         *  1. SQLNamespace.Student.KEY_GROUP_ID
         *  2. SQLNamespace.Student.KEY_ID
         *
         * @see JdbcGroupRepository#save
         */
        public static final String UPDATE_STUDENT_GROUP_ID = """
                UPDATE %s
                SET %s = ?
                WHERE %s = ?
            """.formatted(Student.TABLE_NAME, Student.KEY_GROUP_ID, Student.KEY_ID);

        public static final String UPDATE_CURATOR = """
                UPDATE %s
                SET %s = ?, %s = ?, %s = ?, %s = ?
                WHERE %s = ?
            """.formatted(Curator.TABLE_NAME, Curator.KEY_NAME, Curator.KEY_EMAIL, Curator.KEY_EXPERIENCE, Curator.KEY_GROUP_ID, Curator.KEY_ID);

        /**
         * SQL-запрос на обновление только группы.
         *
         * Запрос ДОЛЖЕН содержать следующие параметры:
         *  1. SQLNamespace.Group.KEY_NAME
         *  2. SQLNamespace.Group.KEY_GRADUATION_DATE
         *  3. SQLNamespace.Group.KEY_ID
         *
         * @see JdbcGroupRepository#save
         */
        public static final String UPDATE_GROUP = """
                UPDATE %s
                SET %s = ?, %s = ?
                WHERE %s = ?
            """.formatted(Group.TABLE_NAME, Group.KEY_NAME, Group.KEY_GRADUATION_DATE, Group.KEY_ID);

        /**
         * SQL-запрос на обновление только студента.
         *
         * Запрос ДОЛЖЕН содержать следующие параметры:
         *  1. SQLNamespace.Student.KEY_NAME
         *  2. SQLNamespace.Student.KEY_DATE_OF_BIRTH
         *  3. SQLNamespace.Student.KEY_GROUP_ID
         *  4. SQLNamespace.Student.KEY_ID
         *
         * @see JdbcGroupRepository#save
         */
        public static final String UPDATE_STUDENT = """
                UPDATE %s
                SET %s = ?, %s = ?, %s = ?
                WHERE %s = ?
            """.formatted(Student.TABLE_NAME, Student.KEY_NAME, Student.KEY_DATE_OF_BIRTH, Student.KEY_GROUP_ID, Student.KEY_ID);

        public static final String SELECT_ALL_STUDENTS = """
                SELECT *
                FROM %s;
            """.formatted(Student.TABLE_NAME);

        public static final String UPDATE_STUDENTS_SET_NULL_GROUP_ID_BY_GROUP_ID = """
                UPDATE %s
                SET %s = NULL
                WHERE %s = ?;
            """.formatted(Student.TABLE_NAME, Student.KEY_GROUP_ID, Student.KEY_GROUP_ID);

        public static final String UPDATE_CURATOR_SET_NULL_GROUP_ID_BY_GROUP_ID = """
                UPDATE %s
                SET %s = NULL
                WHERE %s = ?;
            """.formatted(Curator.TABLE_NAME, Curator.KEY_GROUP_ID, Curator.KEY_GROUP_ID);

        public static final String UPDATE_STUDENTS_SET_GROUP_ID_BY_IDS = """
                UPDATE %s
                SET %s = ?
                WHERE %s IN (?);
            """.formatted(Student.TABLE_NAME, Student.KEY_GROUP_ID, Student.KEY_ID);

        private Query() {}
    }


}
