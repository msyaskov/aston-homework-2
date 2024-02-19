# Aston homework 2

## Задачи
1. Сделать RESTful приложение с CRUD-операциями.
2. Использовать сервлеты.
3. Не использовать Spring и Hibernate в основной логике.
4. Должны присутствовать следующие слои: servlet, service, repository, mapper, dto, entity.
5. Покрыть логику unit-тестами (+ интеграционные).
6. Разрешен Lombok.
7. Должна быть реализация One-to-One и One-to-Many.
8. В качестве базы данных использовать PostgreSQL.
9. Руководствоваться принципами SOLID.
10. Проверка через Postman.
11. Реализовать автодеплой с помощью Jenkins.

## Описание
Реализовано RESTful приложение. Описанное API по OpenAPI Specification находится в файле [api.yaml](./api.yaml).

Для тестов использовалась база данных H2, Spring Boot для интеграционных тестов и Mockito для unit-тестов сервисов.

Используется три сущности Curator, Group, Student. По сути своей они независимы, т.е. куратор и студент могут существовать без группы, а группа без них.
* One-to-One: Curator to Group.
* One-to-Mane: Group to Student.

Демонстрация работы приложения через Postman: [Youtube](https://youtu.be/HgEfLrYhL7U)  
Демонстрация работы автодеплоя с помощью Jenkins: [Youtube](https://youtu.be/ZXkxwd8fnTk)  
Jenkins pipeline: [Jenkinsfile](./Jenkinsfile)