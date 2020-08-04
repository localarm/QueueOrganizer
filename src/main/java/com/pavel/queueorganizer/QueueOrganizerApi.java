package com.pavel.queueorganizer;


import java.util.List;

public interface QueueOrganizerApi {

    /**Проверяет, есть ли клиент в базе данных, если его нет, то добавляет
     * @param clientId айди клиента
     * @param firstName имя клиента
     * @param lastName фамилия клиента, может бьть null
     * @throws QueueOrganizerException если произошла ошибка в работе с базой данных
     */
    void addClient(long clientId, String firstName, String lastName) throws QueueOrganizerException;

    /**Проводит проверку на количество открытых очередей у пользователя, если их меньше 10, то создает очередь, иначе
     * выдает ошибку. Так же при успешном создании очереди добавляет ее создателя первым исполнителем.
     * @param queueName название очереди, не более 64 символов
     * @param adminFirstName имя создателя очереди
     * @param adminLastName фамилия создателя очереди, может быть null
     * @param latitude широта в радианах
     * @param longitude долгота в радианах
     * @param adminId id пользователся, вызвавшего метод
     * @throws OverlimitException если количество рабочих очередей у клиента равно или превышает максимум
     * @throws NameCollisionException если очередь с заданым именем уже есть
     * @throws NameCharsLimitException если название очереди больше допустимого значения (64 символа)
     * @throws QueueOrganizerException если произошла ошибка в работе с базой данных
     */
    void createQueue(String queueName, String adminFirstName, String adminLastName, float longitude, float latitude,
                     long adminId) throws OverlimitException, QueueOrganizerException, NameCollisionException,
            NameCharsLimitException;

    /**Закрывает выбранную очередь, выставляя статус invalid = true и end_time = current_timestamp в таблице queue,
     * увольняет всех исполнителей, исключает всех клиентов из очереди
     * @param adminId id пользователя, вызвавшего метод. Должен быть администратором очереди
     * @param queueId id очереди
     * @return список клиентов, которые необходимо оповестить о закрытии очереди, может быть пустым
     * @throws NonexistentQueueIdException если очереди с заданным id не существует
     * @throws AccessException если clientId не является администратором очереди
     * @throws QueueOrganizerException если произошла ошибка в работе с базой данных
     * @throws ClosedQueueException если очередь является закрытой
     */
    List<ClientInQueue> closeQueue(long adminId, long queueId) throws AccessException, QueueOrganizerException,
            NonexistentQueueIdException, ClosedQueueException;

    /**Возвращает список очередей, которые находятся под управлением администратора
     * @param adminId id администратора очередй
     * @return Список очередей под управлением администратора, пустой список, если очередей нет
     * @throws QueueOrganizerException если произошла ошибка в работе с базой данных
     */
    List<Queue> getQueuesByAdmin(long adminId) throws QueueOrganizerException;

    /**Устанавливает режим и период работы очереди
     * @param adminId id пользователя, вызвавшего метод. Должен быть администратором очереди
     * @param queueId id череди
     * @param period диапазон времени, в течении которого очередь работает, по умолчанию бессрочное
     * @param hours рабочие часы по дням недели, по умолчанию бессрочное
     * @throws NonexistentQueueIdException если очереди с заданным id не существует
     * @throws AccessException если clientId не является администратором очереди
     * @throws QueueOrganizerException если произошла ошибка в работе с базой данных
     * @throws ClosedQueueException если очередь является закрытой
     */
    void changeWorkHours(long adminId, long queueId, String period, String hours) throws AccessException,
            QueueOrganizerException, NonexistentQueueIdException, ClosedQueueException;

    /**Добавляет исполнителя в выбранной очереди, делая проверку на допустимое количество исполнителей в очереди.
     * Максимальное количество исполнителей - 100
     * @param execId id исполнителя
     * @param adminId id пользователя, вызвавшего метод. Должен быть администратором очереди
     * @param name имя исполнителя, не более 511 символов
     * @param queueId id очереди
     * @throws AccessException если clientId не является администратором очереди
     * @throws NonexistentQueueIdException если очереди с заданным id не существует
     * @throws NameCharsLimitException если имя исполнителя превышает допустимое значение (511 символов)
     * @throws OverlimitException если количество исполнителей у очереди равно или превышает максимум
     * @throws NameCollisionException если переданное имя совпдает с именем другого исполнителя в очереди
     * @throws QueueOrganizerException если произошла ошибка в работе с базой данных
     * @throws ClosedQueueException если очередь является закрытой
     */
    void addExecutor(long execId, long adminId, long queueId, String name) throws AccessException,
            OverlimitException, NameCollisionException, QueueOrganizerException, NonexistentQueueIdException,
            NameCharsLimitException, ClosedQueueException;

    /**отстраняет исполнителя выбранной очереди, выставляя статус invalid = true в таблице executor
     * @param execId id исполнителя
     * @param adminId id пользователя, вызвавшего метод. Должен быть администратором очереди
     * @param queueId id очереди
     * @throws NonexistentQueueIdException если очереди с заданным id не существует
     * @throws NonexistentExecutorIdException если исполнителя с заданным id не существует
     * @throws AccessException если clientId не является администратором очереди
     * @throws QueueOrganizerException если произошла ошибка в работе с базой данных
     * @throws ClosedQueueException если очередь является закрытой
     */
    void fireExecutor(long execId, long adminId, long queueId) throws AccessException, QueueOrganizerException,
            NonexistentQueueIdException, NonexistentExecutorIdException, ClosedQueueException;

    /**Возвращает список исполнителей, обслуживающих данную очередь, 100 исполнителей
     * @param adminId id пользователя, вызвавшего метод. Должен быть администратором очереди
     * @param queueId id очереди
     * @return список исполнителей
     * @throws AccessException если clientId не является администратором очереди
     * @throws NonexistentQueueIdException если очереди с заданным id не существует
     * @throws QueueOrganizerException если произошла ошибка в работе с базой данных
     * @throws ClosedQueueException если очередь является закрытой
     */
    List<Executor> getExecList (long adminId, long queueId) throws AccessException,
            QueueOrganizerException, NonexistentQueueIdException, ClosedQueueException;

    /**Изменяет возможность вступать в определенную очередь
     * @param adminId id пользователя, вызвавшего метод
     * @param queueId id очереди
     * @param status статус обслуживания у очереди, true  - обслуживание активно, false - обслуживане приостановлено
     * @throws AccessException если clientId не является администратором очереди
     * @throws NonexistentQueueIdException если очереди с заданным id не существует
     * @throws QueueOrganizerException если произошла ошибка в работе с базой данных
     * @throws ClosedQueueException если очередь является закрытой
     */
    void switchQueueActiveStatus(long adminId, long queueId, boolean status) throws AccessException,
            QueueOrganizerException, NonexistentQueueIdException, ClosedQueueException;

    /**Меняет имя у выбранного пользователя, при этом проводится проверка
     * @param execId id исполнителя
     * @param adminId id пользователя, вызвавшего функуцию
     * @param queueId id очереди
     * @param newName новое имя исполнителя
     * @throws AccessException если clientId не является администратором очереди
     * @throws NonexistentQueueIdException если очереди с заданным id не существует
     * @throws NonexistentExecutorIdException если исполнителя с заданным id не существует
     * @throws NameCharsLimitException если имя исполнителя превышает допустимое значение (511 символов)
     * @throws NameCollisionException если переданное имя совпдает с именем другого исполнителя в очереди
     * @throws QueueOrganizerException если произошла ошибка в работе с базой данных
     * @throws ClosedQueueException если очередь является закрытой
     */
    void changeExecName(long execId,long queueId, long adminId, String newName) throws AccessException,
            QueueOrganizerException, NameCollisionException, NonexistentQueueIdException,
            NonexistentExecutorIdException, NameCharsLimitException, ClosedQueueException;

    /**Возвращает список очередей в радиусе 50 м от заданной широты и долготы. Список отсортирован по количеству
     * уникальных клиентов.
     * @param longitude долгота в радианах
     * @param latitude широта в радианах
     * @return список очередей, удовлетворяющий условию. Максимально количество очередей - 30
     * @throws QueueOrganizerException если произошла ошибка в работе с базой данных
     */
    List<Queue> getQueuesAround(float longitude, float latitude) throws QueueOrganizerException;

    /**Возвращает информацию о выбранной очереди
     * @param queueId id очереди
     * @return заполненную модель очереди
     * @throws NonexistentQueueIdException если очереди с заданным id не существует
     * @throws QueueOrganizerException если произошла ошибка в работе с базой данных
     * @throws ClosedQueueException если очередь является закрытой
     */
    Queue getQueueInfo(long queueId) throws QueueOrganizerException, NonexistentQueueIdException, ClosedQueueException;

    /**Передает исполнителю следующего клиента. Если исполнитель обслуживал клиента на данный момент, его обслуживание
     * считается завершенным. Если нет клиентов в очереди - исполнитель встает в режим ожидания
     * @param queueId id очереди
     * @param execId id исполнителя
     * @throws AccessException если пользователь с execId не является исполнителем очереди
     * @throws InactiveExecutorException если исполнитель не находится в режиме обслуживания клиентов
     * @throws NonexistentQueueIdException если очереди с заданным id не существует
     * @throws NonexistentExecutorIdException если исполнителя с заданным id не существует
     * @throws WrongWorkingTimeException если рабочие часы очереди не совпадают с текущим временем
     * @throws WrongTimeException если время работы очереди не совпадает с периодом работы очереди
     * @throws QueueOrganizerException если возникает ощибка в работе с базами данных
     * @throws WaitingException если исполнитлеь уже находится в стадии ожидания
     * @throws ClosedQueueException если очередь является закрытой
     * @return если есть текущий клиент и следующий за ним, null если в очереди нет свободных клиентов
     */
    Notify<ClientInQueue> nextClient(long queueId, long execId) throws QueueOrganizerException, AccessException,
            NonexistentQueueIdException, WrongTimeException, NonexistentExecutorIdException,
            WrongWorkingTimeException, InactiveExecutorException, WaitingException, ClosedQueueException;

    /**Текущий исполнитель вступает в режим обслуживания клиентов,устанавливается соответсвующий статус в базе данных,
     * при условии что он находится в радиусе 50 м от указанной очереди
     * @param queueId id очереди
     * @param execId id исполнителя
     * @throws AccessException если пользователь с execId не является исполнителем очереди
     * @throws NonexistentQueueIdException если очереди с заданным id не существует
     * @throws NonexistentExecutorIdException если исполнителя с заданным id не существует
     * @throws QueueOrganizerException если произошла ошибка в работе с базой данных
     * @throws ClosedQueueException если очередь является закрытой
     */
    void enterExecutorMode(long queueId, long execId) throws AccessException,
            QueueOrganizerException, NonexistentQueueIdException, WrongLocationException,
            NonexistentExecutorIdException, ClosedQueueException;

    /**Текущий исполнитель покидает режим обслуживания клиентов. Текущий клиент считается обслуженным
     * @param queueId id очереди
     * @param execId id исполнителя
     * @throws AccessException если execId не является исполнителем очереди
     * @throws NonexistentExecutorIdException если исполнителся с заданным id не существует
     * @throws NonexistentQueueIdException если очереди с заданным id не существует
     * @throws QueueOrganizerException если произошла ошибка в работе с базой данных
     * @throws ClosedQueueException если очередь является закрытой
     */
    void quitExecutorMode(long queueId, long execId) throws QueueOrganizerException, AccessException,
            NonexistentExecutorIdException, NonexistentQueueIdException, ClosedQueueException;

    /**возвращает список клиентов очереди, обслуженных данным исполнителем. Выводит не более 20 последних клиентов
     * @param queueId id очереди
     * @param execId id исполнителя
     * @return список последних обслуженных клиентов. Максимум 20. Может быть пустым, если нет обслуженных клиентов
     * @throws NonexistentExecutorIdException если очереди с заданным id не существует
     * @throws NonexistentQueueIdException если очереди с заданным id не существует
     * @throws AccessException если execId не является исполнителем очереди
     * @throws QueueOrganizerException если произошла ошибка в работе с базой данных
     * @throws ClosedQueueException если очередь является закрытой
     */
    List<ServedClient> getClientsList(long queueId, long execId) throws QueueOrganizerException,
            NonexistentExecutorIdException, AccessException, NonexistentQueueIdException, ClosedQueueException;

    /**Добавляет указаного пользователя в очередь. Возвращает исполнителя, если клиента готовы сразу принять.
     * Максимальное число ожидающих - 10000
     * @param clientId id пользователя
     * @param queueId id очереди
     * @return если есть свободный исполнитель, то возвращает данные о нем и List<Client> с единственным объектом внутри
     * - данные о пользователе, иначе null
     * @throws OverlimitException если количество клиентов в очереди равно или превышает максимум
     * @throws NonexistentQueueIdException если очереди с заданным id не существует
     * @throws WrongTimeException если время вступления в очередь не совпадает с периодом работы очереди
     * @throws WrongWorkingTimeException если время встпуление в очередь не совпадает с временем работы очереди
     * @throws RepeatedGetInTheQueueException если клиент ытается повторон вступить в очередь, в которой он уже стоит
     * @throws QueueOrganizerException если произошла ошибка в работе с базой данных
     * @throws ClosedQueueException если очередь является закрытой
     */
    Notify<Client> getInQueue (long clientId, long queueId) throws OverlimitException,
            QueueOrganizerException, NonexistentQueueIdException, WrongTimeException, RepeatedGetInTheQueueException,
            InactiveQueueException, WrongWorkingTimeException, ClosedQueueException;

    /**Возвращает информацию о клиенте в очереди
     * @param clientId id пользователя
     * @param queueId id очереди
     * @return заполненную модель клиента в очереди
     * @throws QueueOrganizerException если клиент не ожидает в очереди
     * @throws NonexistentClientException если произошла ошибка в работе с базой данных
     */
    ClientInQueue getClientInQueueInfo(long clientId, long queueId) throws QueueOrganizerException,
            NonexistentClientException;

    /**Указанный пользователь покидает очередь
     * @param queueId id очереди
     * @param clientId id пользователя
     * @throws NonexistentQueueIdException если очереди с заданным id не существует
     * @throws NonexistentClientException если клиент не находится в очереди
     * @throws QueueOrganizerException если произошла ошибка в работе с базой данных
     * @throws ClosedQueueException если очередь является закрытой
     */
    void leaveQueue(long queueId,long clientId) throws QueueOrganizerException, NonexistentQueueIdException,
            NonexistentClientException, ClosedQueueException;

    /**Выставляет режим уведомлений о изменении позиции пользователя в данной очереди
     * @param queueId id очереди
     * @param clientId id пользователя
     * @param status режим уведмолений: true - уведомления включены, false - уведомления отключены
     * @throws NonexistentQueueIdException если очереди с заданным id не существует
     * @throws NonexistentClientException если клиент не ожидает в очереди
     * @throws QueueOrganizerException если произошла ошибка в работе с базой данных
     * @throws ClosedQueueException если очередь является закрытой
     */
    void switchNotification(long queueId, long clientId, boolean status) throws QueueOrganizerException,
            NonexistentClientException, NonexistentQueueIdException, ClosedQueueException;

    /**Возвращает список очередей, в которых находится переданный исполнитель
     * @param execId id исполнителя
     * @param longitude долгота в радианах
     * @param latitude широта в радианах
     * @return Список очередей
     * @throws QueueOrganizerException если произошла ошибка в работе с базой данных
     */
    List<Queue> getQueuesListByExecutor(long execId, float longitude, float latitude) throws QueueOrganizerException;
}
