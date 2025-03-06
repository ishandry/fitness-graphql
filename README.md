# Демонсрація роботи з graphql-java для фітнес-застосунку (альтернатива e-commerce з лекції)

## Опис

Застосунок демонструє роботу з фітнес-тренуваннями. Користувач може:

- Додавати вправи до списку доступних (`template`).
- Комбінувати вправи та їхню інтенсивність у тренування (`workout`).
- Позначати тренування як виконане.

### Основні характеристики:
- Вкладена ієрархія (до 4 рівнів у глибину).
- Початкове зчитування даних у пам'ять з JSON-файлів.
- 2 мутації.
- 6 запитів (квері).
- Кастомні дата-фетчери.

Для зручності `Main` та файл зі схемою GraphQL винесені в корінь репозиторію.

Файл `README-execution-protocol.txt` містить протокол виконання основного коду.

---

## Основний код протоколу

```java
// 1. Запит: Отримання всіх шаблонів вправ
String templatesQuery = FileUtils.readFileContent(TEMPLATES_LIST_QUERY_PATH);
System.out.println("Query Templates: " + templatesQuery);
ExecutionResult templatesResult = graphQL.execute(templatesQuery);
System.out.println("Result Templates: " + JsonUtils.serializeToJson(templatesResult.toSpecification()));

// 2. Запит: Отримання всіх користувачів з їхніми тренуваннями
String usersQuery = FileUtils.readFileContent(USERS_QUERY_PATH);
System.out.println("Query Users: " + usersQuery);
ExecutionResult usersResult = graphQL.execute(usersQuery);
System.out.println("Result Users: " + JsonUtils.serializeToJson(usersResult.toSpecification()));

// 3. Мутація: Створення нового тренування
String createWorkoutMutation = FileUtils.readFileContent(CREATE_WORKOUT_MUTATION_PATH);
System.out.println("Mutation CreateWorkout: " + createWorkoutMutation);
ExecutionResult createWorkoutResult = graphQL.execute(createWorkoutMutation);
System.out.println("Result CreateWorkout: " + JsonUtils.serializeToJson(createWorkoutResult.toSpecification()));

// Отримання ID створеного тренування
String newWorkoutId = null;
Map<String, Object> dataCreateWorkout = createWorkoutResult.getData();
if (dataCreateWorkout != null && dataCreateWorkout.get("createWorkout") instanceof Map) {
    Map<String, Object> workoutMap = (Map<String, Object>) dataCreateWorkout.get("createWorkout");
    newWorkoutId = (String) workoutMap.get("id");
}

// 4. Запит: Отримання тренувань користувача (фільтр: userId "u1", completed false)
String workoutsQuery = FileUtils.readFileContent(WORKOUTS_QUERY_PATH);
System.out.println("Query Workouts (before complete): " + workoutsQuery);
ExecutionResult workoutsResult = graphQL.execute(workoutsQuery);
System.out.println("Result Workouts (before complete): " + JsonUtils.serializeToJson(workoutsResult.toSpecification()));

// 5. Мутація: Позначення тренування як завершеного (якщо ID отримано)
if (newWorkoutId != null) {
    // Зчитуємо файл мутації completeWorkout, що містить плейсхолдер "WORKOUT_ID_PLACEHOLDER"
    String completeWorkoutMutationTemplate = FileUtils.readFileContent(COMPLETE_WORKOUT_MUTATION_PATH);
    // Замінюємо плейсхолдер на фактичний ID тренування
    String completeWorkoutMutation = completeWorkoutMutationTemplate.replace("WORKOUT_ID_PLACEHOLDER", newWorkoutId);
    System.out.println("Mutation CompleteWorkout: " + completeWorkoutMutation);
    ExecutionResult completeWorkoutResult = graphQL.execute(completeWorkoutMutation);
    System.out.println("Result CompleteWorkout: " + JsonUtils.serializeToJson(completeWorkoutResult.toSpecification()));
}

// 6. Запит: Повторне отримання тренувань користувача для перевірки оновлення
System.out.println("Query Workouts (after complete): " + workoutsQuery);
ExecutionResult workoutsResultAfter = graphQL.execute(workoutsQuery);
System.out.println("Result Workouts (after complete): " + JsonUtils.serializeToJson(workoutsResultAfter.toSpecification()));
