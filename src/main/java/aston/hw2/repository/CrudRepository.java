package aston.hw2.repository;

import java.util.stream.Stream;

/**
 * Репозиторий с базовыми CRUD-операциями.
 *
 * @param <E> тип сущности
 * @param <ID> тип идентификатора сущности
 *
 * @author Максим Яськов
 * @see Repository
 */
public interface CrudRepository<E,ID> extends Repository<E,ID> {

    /**
     * Возвращает коллекцию всех сущностей.
     *
     * @return коллекцию всех сущностей, всегда не null
     */
    Stream<E> findAll();

    /**
     * Возвращает сущность по идентификатору.
     *
     * @param id идентификатор сущности
     * @return сущность с указанным идентификатором
     * @throws IllegalArgumentException если идентификатор равен null
     */
    E findById(ID id);

    /**
     * Удаляет сущность по идентификатору.
     *
     * @param id идентификатор сущности
     * @return удаленная сущность или null, если сущность по указанному id не найдена
     * @throws IllegalArgumentException если идентификатор равен null
     */
    E removeById(ID id);

    /**
     * Сохраняет сущность.
     *
     * @param entity сохраняемая сущность
     * @return указанная сохраненная сущность, всегда не null
     * @throws IllegalArgumentException если сущность равна null
     */
    E save(E entity);

}
