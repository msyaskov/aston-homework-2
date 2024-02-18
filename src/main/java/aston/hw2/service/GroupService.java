package aston.hw2.service;

import aston.hw2.entity.Group;

import java.util.stream.Stream;

/**
 * Сервис CRUD-операций над группами.
 *
 * @author Максим Яськов
 */
public interface GroupService {

    /**
     * Создает связь группы с указанным куратором.
     *
     * @param curatorId идентификатор куратора
     * @param groupId идентификатор группы
     * @throws CuratorNotFoundException если куратор с указанным идентификатором не найден
     * @throws GroupNotFoundException если группа с указанным идентификатором не найдена
     */
    void assignCurator(int groupId, int curatorId);

    /**
     * Создает связь группы с указанным студентом.
     *
     * @param groupId идентификатор группы
     * @param studentId идентификатор студента
     * @throws GroupNotFoundException если группа с указанным идентификатором не найдена
     * @throws StudentNotFoundException если студент с указанным идентификатором не найден
     */
    void assignStudent(int groupId, int studentId);

    /**
     * Создает новую группу по значениям кандидата.
     *
     * Кандидат используется только на чтение.
     * Реализация решает значения каких свойств кандидата использовать для создания новой группы.
     *
     * @param candidate кандидат для создания новой группы
     * @return новая группа, всегда не null
     * @throws InvalidCandidateException если указанный кандидат считается недействительным
     */
    Group createGroupByCandidate(Group candidate);

    /**
     * Возвращает поток всех групп.
     *
     * @return поток всех групп, всегда не null
     */
    Stream<Group> getAllGroups();

    /**
     * Возвращает группу по идентификатору.
     *
     * @param groupId идентификатор требуемой группы
     * @return группа с указанным идентификатором, всегда не null
     * @throws GroupNotFoundException если группа с указанным идентификатором не найдена
     */
    Group getGroup(int groupId);

    /**
     * Удаляет группу по указанному идентификатору.
     *
     * @param groupId идентификатор удаляемой группы.
     * @throws GroupNotFoundException если группа с указанным идентификатором не найдена
     */
    void removeGroup(int groupId);

    /**
     * Разрывает связь, если возможно, группы со связанным с нею куратором.
     *
     * @param groupId идентификатор группы
     * @throws GroupNotFoundException если группа с указанным идентификатором не найдена
     */
    void unassignCurator(int groupId);

    /**
     * Разрывает связь, если возможно, группы со связанным с нею студентом.
     *
     * @param groupId идентификатор группы
     * @param studentId идентификатор студента
     * @throws GroupNotFoundException если группа с указанным идентификатором не найдена
     * @throws StudentNotFoundException если студент с указанным идентификатором не найден
     */
    void unassignStudent(int groupId, int studentId);

    /**
     * Обновляет группу по указанному идентификатору значениями свойств кандидата.
     *
     * Кандидат используется только на чтение.
     * Реализация решает значения каких свойств кандидата использовать для обновления группы.
     *
     * @param groupId идентификатор обновляемой группы.
     * @return обновленная группа, всегда не null
     * @throws GroupNotFoundException если группа с указанным идентификатором не найдена
     * @throws InvalidCandidateException если указанный кандидат считается недействительным
     */
    Group updateGroupByCandidate(int groupId, Group candidate);

}
