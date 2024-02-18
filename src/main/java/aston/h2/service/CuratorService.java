package aston.h2.service;

import aston.h2.entity.Curator;

import java.util.stream.Stream;

/**
 * Сервис CRUD-операций над кураторами.
 *
 * @author Максим Яськов
 */
public interface CuratorService {

    /**
     * Создает связь куратора с указанной группой.
     *
     * @param curatorId идентификатор куратора
     * @param groupId идентификатор группы
     * @throws CuratorNotFoundException если куратор с указанным идентификатором не найден
     * @throws GroupNotFoundException если группа с указанным идентификатором не найдена
     */
    void assignGroup(int curatorId, int groupId);

    /**
     * Создает нового куратора по значениям кандидата.
     *
     * Кандидат используется только на чтение.
     * Реализация решает значения каких свойств кандидата использовать для создания нового куратора.
     *
     * @param candidate кандидат для создания нового куратора
     * @return новый куратор, всегда не null
     * @throws InvalidCandidateException если указанный кандидат считается недействительным
     */
    Curator createCuratorByCandidate(Curator candidate);

    /**
     * Возвращает поток всех кураторов.
     *
     * @return поток всех кураторов, всегда не null
     */
    Stream<Curator> getAllCurators();

    /**
     * Возвращает куратора по идентификатору.
     *
     * @param curatorId идентификатор требуемого куратора
     * @return куратора с указанным идентификатором, всегда не null
     * @throws CuratorNotFoundException если куратор с указанным идентификатором не найден
     */
    Curator getCurator(int curatorId);

    /**
     * Удаляет куратора по указанному идентификатору.
     *
     * @param curatorId идентификатор удаляемого куратора.
     * @throws CuratorNotFoundException если куратор с указанным идентификатором не найден
     */
    void removeCurator(int curatorId);

    /**
     * Разрывает связь, если возможно, куратора со связанной с ним группой.
     *
     * @param curatorId идентификатор куратора
     * @throws CuratorNotFoundException если куратор с указанным идентификатором не найден
     */
    void unassignGroup(int curatorId);

    /**
     * Обновляет куратора по указанному идентификатору значениями свойств кандидата.
     *
     * Кандидат используется только на чтение.
     * Реализация решает значения каких свойств кандидата использовать для обновления студента.
     *
     * @param curatorId идентификатор обновляемого куратора
     * @return обновленный куратор, всегда не null
     * @throws InvalidCandidateException если указанный кандидат считается недействительным
     * @throws CuratorNotFoundException если куратор с указанным идентификатором не найден
     */
    Curator updateCurator(int curatorId, Curator candidate);

}
