package aston.h2.repository;

import aston.h2.entity.Group;

/**
 * Репозиторий групп.
 *
 * @author Максим Яськов
 * @see CrudRepository
 * @see Group
 */
public interface GroupRepository extends CrudRepository<Group, Integer> {

    /**
     * Возвращает группу по уникальному имени.
     *
     * @param name уникальное имя
     * @return группа с указанным именем или null, если группа не найдена
     */
    Group findByName(String name);

}
