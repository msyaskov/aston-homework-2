package aston.hw2.repository;

import aston.hw2.entity.Student;

/**
 * Репозиторий студентов.
 *
 * @author Максим Яськов
 * @see CrudRepository
 * @see Student
 */
public interface StudentRepository extends CrudRepository<Student, Integer> {
}
