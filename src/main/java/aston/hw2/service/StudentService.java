package aston.hw2.service;

import aston.hw2.entity.Student;

import java.util.stream.Stream;

/**
 * Сервис CRUD-операций над студентами.
 *
 * @author Максим Яськов
 */
public interface StudentService {

    /**
     * Создает связь студента с указанной группой.
     *
     * @param studentId идентификатор студента
     * @param groupId идентификатор группы
     * @throws GroupNotFoundException если группа с указанным идентификатором не найдена
     * @throws StudentNotFoundException если студент с указанным идентификатором не найден
     */
    void assignGroup(int studentId, int groupId);

    /**
     * Создает нового студента по значениям кандидата.
     *
     * Кандидат используется только на чтение.
     * Реализация решает значения каких свойств кандидата использовать для создания нового студента.
     *
     * @param candidate кандидат для создания нового студента
     * @return новый студент, всегда не null
     * @throws InvalidCandidateException если указанный кандидат считается недействительным
     */
    Student createStudentByCandidate(Student candidate);

    /**
     * Возвращает поток всех студентов.
     *
     * @return поток всех студентов, всегда не null
     */
    Stream<Student> getAllStudents();

    /**
     * Возвращает студента по идентификатору.
     *
     * @param studentId идентификатор требуемого студента
     * @return студент с указанным идентификатором, всегда не null
     * @throws StudentNotFoundException если студент с указанным идентификатором не найден
     */
    Student getStudent(int studentId);

    /**
     * Удаляет студента по указанному идентификатору.
     *
     * @param studentId идентификатор удаляемого студента.
     * @throws StudentNotFoundException если студент с указанным идентификатором не найден
     */
    void removeStudent(int studentId);

    /**
     * Разрывает связь, если возможно, студента со связанной с ним группой.
     *
     * @param studentId идентификатор куратора
     * @throws StudentNotFoundException если студент с указанным идентификатором не найден
     */
    void unassignGroup(int studentId);

    /**
     * Обновляет студента по указанному идентификатору значениями свойств кандидата.
     *
     * Кандидат используется только на чтение.
     * Реализация решает значения каких свойств кандидата использовать для обновления студента.
     *
     * @param studentId идентификатор обновляемого студента.
     * @return обновленный студент, всегда не null
     * @throws InvalidCandidateException если указанный кандидат считается недействительным
     * @throws StudentNotFoundException если студент с указанным идентификатором не найден
     */
    Student updateStudent(int studentId, Student candidate);

}
