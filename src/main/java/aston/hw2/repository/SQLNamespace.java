package aston.hw2.repository;

public abstract class SQLNamespace {

    private SQLNamespace() {}

    public static abstract class Student {

        public static final String KEY_ID = "s_id";
        public static final String KEY_NAME = "s_name";
        public static final String KEY_DATE_OF_BIRTH = "s_dateOfBirth";
        public static final String KEY_GROUP_ID = "s_groupId";

        private Student() {}
    }

    public static abstract class Group {

        public static final String KEY_ID = "g_id";
        public static final String KEY_NAME = "g_name";
        public static final String KEY_GRADUATION_DATE = "g_graduationDate";

        private Group() {}
    }

    public static abstract class Curator {

        public static final String KEY_ID = "c_id";
        public static final String KEY_NAME = "c_name";
        public static final String KEY_EMAIL = "c_email";
        public static final String KEY_EXPERIENCE = "c_experience";
        public static final String KEY_GROUP_ID = "c_groupId";

        private Curator() {}
    }

    public static abstract class Query {

        public static final String DELETE_GROUP_BY_ID = """
                DELETE
                FROM groups
                WHERE id = ?
            """;

        public static final String DELETE_STUDENT_BY_ID = """
                DELETE
                FROM students
                WHERE id = ?
            """;

        public static final String DELETE_CURATOR_BY_ID = """
                DELETE
                FROM curators
                WHERE id = ?
            """;

        public static final String INSERT_GROUP = """
                INSERT INTO groups
                    (name, graduation_date)
                VALUES (?, ?);
            """;

        public static final String INSERT_STUDENT = """
                INSERT INTO students
                    (name, date_of_birth, group_id)
                VALUES (?, ?, ?);
            """;

        public static final String INSERT_CURATOR = """
                INSERT INTO curators
                    (name, email, experience, group_id)
                VALUES (?, ?, ?, ?);
            """;

        public static final String SELECT_ALL_GROUPS_WITH_CURATORS = """
                SELECT
                    groups.id as %s,
                    groups.name as %s,
                    groups.graduation_date as %s,
                    curators.id as %s,
                    curators.name as %s,
                    curators.email as %s,
                    curators.experience as %s
                FROM groups
                    LEFT JOIN curators ON groups.id = curators.group_id;
            """.formatted(Group.KEY_ID, Group.KEY_NAME, Group.KEY_GRADUATION_DATE,
                Curator.KEY_ID, Curator.KEY_NAME, Curator.KEY_EMAIL, Curator.KEY_EXPERIENCE);

        public static final String SELECT_CURATORS_BY_NULL_GROUP_ID = """
                SELECT
                    id as %s,
                    name as %s,
                    email as %s,
                    experience as %s
                FROM curators
                WHERE group_id IS NULL
            """.formatted(Curator.KEY_ID, Curator.KEY_NAME, Curator.KEY_EMAIL, Curator.KEY_EXPERIENCE);

        public static final String SELECT_STUDENT_BY_ID = """
                SELECT
                    id as %s,
                    name as %s,
                    date_of_birth as %s,
                    group_id as %s
                FROM students
                WHERE id = ?;
            """.formatted(Student.KEY_ID, Student.KEY_NAME, Student.KEY_DATE_OF_BIRTH, Student.KEY_GROUP_ID);

        public static final String SELECT_GROUP_WITH_CURATOR_BY_ID = """
                SELECT
                    groups.id as %s,
                    groups.name as %s,
                    groups.graduation_date as %s,
                    curators.id as %s,
                    curators.name as %s,
                    curators.email as %s,
                    curators.experience as %s
                FROM groups
                    LEFT JOIN curators ON groups.id = curators.group_id
                WHERE groups.id = ?;
            """.formatted(Group.KEY_ID, Group.KEY_NAME, Group.KEY_GRADUATION_DATE,
                Curator.KEY_ID, Curator.KEY_NAME, Curator.KEY_EMAIL, Curator.KEY_EXPERIENCE);

        public static final String SELECT_GROUP_ID_BY_NAME = """
                SELECT
                    id as %s
                FROM groups
                WHERE name = ?
            """.formatted(Group.KEY_NAME);

        public static final String SELECT_CURATOR_BY_ID = """
                SELECT
                    id as %s,
                    name as %s,
                    email as %s,
                    experience as %s,
                    group_id as %s
                FROM curators
                WHERE id = ?;
            """.formatted(Curator.KEY_ID, Curator.KEY_NAME, Curator.KEY_EMAIL, Curator.KEY_EXPERIENCE, Curator.KEY_GROUP_ID);

        public static final String SELECT_STUDENTS_BY_GROUP_ID = """
                SELECT
                    id as %s,
                    name as %s,
                    date_of_birth as %s,
                    group_id as %s
                FROM students
                WHERE group_id = ?;
            """.formatted(Student.KEY_ID, Student.KEY_NAME, Student.KEY_DATE_OF_BIRTH, Student.KEY_GROUP_ID);

        public static final String SELECT_STUDENTS_BY_NULL_GROUP_ID = """
                SELECT
                    id as %s,
                    name as %s,
                    date_of_birth as %s,
                    group_id as %s
                FROM students
                WHERE group_id IS NULL;
            """.formatted(Student.KEY_ID, Student.KEY_NAME, Student.KEY_DATE_OF_BIRTH, Student.KEY_GROUP_ID);

        public static final String UPDATE_CURATOR = """
                UPDATE curators
                SET name = ?, email = ?, experience = ?, group_id = ?
                WHERE id = ?
            """;

        public static final String UPDATE_GROUP = """
                UPDATE groups
                SET name = ?, graduation_date = ?
                WHERE id = ?
            """;

        public static final String UPDATE_STUDENT = """
                UPDATE students
                SET name = ?, date_of_birth = ?, group_id = ?
                WHERE id = ?
            """;

        public static final String UPDATE_STUDENTS_SET_NULL_GROUP_ID_BY_GROUP_ID = """
                UPDATE students
                SET group_id = NULL
                WHERE group_id = ?;
            """;

        public static final String UPDATE_CURATOR_SET_NULL_GROUP_ID_BY_GROUP_ID = """
                UPDATE curators
                SET group_id = NULL
                WHERE group_id = ?;
            """;

        private Query() {}
    }
}
