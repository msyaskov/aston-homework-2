Конечные точки:
1. Группы:
* POST /groups - создать группу.
* GET /groups - получить все группы
* GET /groups/:groupId - получить конкретную группу без связей со студентами
* PUT /groups/:groupId - изменить информацию о группе
* DELETE /groups/:groupId - удалить группу (студенты группы не удаляются)

1. Студенты:
* POST /students - создать студента.
* GET /students - получить всех студентов.
* GET /students/:sId - получить конкретного студента.
* PUT /students/:sId - изменить информацию о студенте.
* DELETE /students/:sId - удалить студента.

1. Отношения
* GET /groups/:gId/students - студенты по группе
* PUT /groups/:gId/students/:sId - назначение студента в группу
* DELETE /groups/:gId/students/:sId - снять студента с группы
* GET /students/:sId/groupDto - получить группу студента
* PUT /students/:sId/groupDto/:gId - назначить студента в группу
* DELETE /students/:sId/groupDto - удалить студента из группы