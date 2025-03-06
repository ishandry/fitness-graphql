package ua.edu.lnu.fitnessql;

import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.*;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import ua.edu.lnu.fitnessql.fitness.Exercise;
import ua.edu.lnu.fitnessql.fitness.ExerciseTemplate;
import ua.edu.lnu.fitnessql.fitness.FitnessUser;
import ua.edu.lnu.fitnessql.fitness.Workout;
import ua.edu.lnu.fitnessql.utils.FileUtils;
import ua.edu.lnu.fitnessql.utils.JsonUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.List;

import graphql.language.StringValue;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;

import static graphql.schema.idl.RuntimeWiring.newRuntimeWiring;

public class Main {

    private static final String FITNESS_SCHEMA_PATH = "/graphql/schema/fitness.graphqls";
    private static final String TEMPLATES_LIST_QUERY_PATH = "/graphql/query/fitness/getAllTemplates.graphql";
    private static final String USERS_QUERY_PATH = "/graphql/query/fitness/getAllUsers.graphql";
    private static final String WORKOUTS_QUERY_PATH = "/graphql/query/fitness/getWorkouts.graphql";
    private static final String CREATE_WORKOUT_MUTATION_PATH = "/graphql/mutations/createWorkout.graphql";
    private static final String COMPLETE_WORKOUT_MUTATION_PATH = "/graphql/mutations/completeWorkout.graphql";

    // Використовуємо наші record‑класи для даних:
    private static List<ExerciseTemplate> fitnessTemplates;
    private static List<Workout> fitnessWorkouts;
    private static List<FitnessUser> fitnessUsers;

    public static void main(String[] args) {
        // Завантаження даних з JSON файлів:
        fitnessTemplates = JsonUtils.loadListFromJsonFile("/data/fitnessTemplates.json", ExerciseTemplate[].class);

        List<FitnessUser> usersList = JsonUtils.loadListFromJsonFile("/data/fitnessUsers.json", FitnessUser[].class);
        fitnessUsers = usersList;

        List<Workout> workoutsList = JsonUtils.loadListFromJsonFile("/data/fitnessWorkouts.json", Workout[].class);
        fitnessWorkouts = workoutsList;


        // Демонстрація GraphQL запитів
        fitness();
    }

    private static void fitness() {
        TypeDefinitionRegistry typeDefinitionRegistry = parseSchema(FITNESS_SCHEMA_PATH);
        RuntimeWiring runtimeWiring = buildRuntimeWiringFitness();
        GraphQL graphQL = createGraphQLEntryPoint(typeDefinitionRegistry, runtimeWiring);

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

        // Видобуваємо id створеного тренування з результату мутації
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

        // 5. Мутація: Позначення тренування як завершеного (якщо id отримано)
        if (newWorkoutId != null) {
            // Зчитуємо файл мутації completeWorkout, в якому міститься плейсхолдер "WORKOUT_ID_PLACEHOLDER"
            String completeWorkoutMutationTemplate = FileUtils.readFileContent(COMPLETE_WORKOUT_MUTATION_PATH);
            // Замінюємо плейсхолдер на фактичний id тренування
            String completeWorkoutMutation = completeWorkoutMutationTemplate.replace("WORKOUT_ID_PLACEHOLDER", newWorkoutId);
            System.out.println("Mutation CompleteWorkout: " + completeWorkoutMutation);
            ExecutionResult completeWorkoutResult = graphQL.execute(completeWorkoutMutation);
            System.out.println("Result CompleteWorkout: " + JsonUtils.serializeToJson(completeWorkoutResult.toSpecification()));
        }

        // 6. Запит: Повторне отримання тренувань користувача, щоб перевірити оновлення
        System.out.println("Query Workouts (after complete): " + workoutsQuery);
        ExecutionResult workoutsResultAfter = graphQL.execute(workoutsQuery);
        System.out.println("Result Workouts (after complete): " + JsonUtils.serializeToJson(workoutsResultAfter.toSpecification()));
    }

    private static TypeDefinitionRegistry parseSchema(String schemaPath) {
        String schema = FileUtils.readFileContent(schemaPath);
        SchemaParser schemaParser = new SchemaParser();
        return schemaParser.parse(schema);
    }

    private static RuntimeWiring buildRuntimeWiringFitness() {
        // Визначення кастомного scalar для DateTime
        GraphQLScalarType dateTimeScalar = GraphQLScalarType.newScalar()
                .name("DateTime")
                .description("A custom scalar that handles date-time values as ISO 8601 strings")
                .coercing(new Coercing<String, String>() {
                    @Override
                    public String serialize(Object dataFetcherResult) throws CoercingSerializeException {
                        return dataFetcherResult.toString();
                    }

                    @Override
                    public String parseValue(Object input) throws CoercingParseValueException {
                        return input.toString();
                    }

                    @Override
                    public String parseLiteral(Object input) throws CoercingParseLiteralException {
                        if (input instanceof StringValue) {
                            return ((StringValue) input).getValue();
                        }
                        throw new CoercingParseLiteralException("Expected AST type 'StringValue'");
                    }
                })
                .build();

        // DataFetcher для запиту "users"
        DataFetcher<List<FitnessUser>> usersFetcher = env -> fitnessUsers;

        // DataFetcher для запиту "templates"
        DataFetcher<List<ExerciseTemplate>> templatesFetcher = env -> fitnessTemplates;

        // DataFetcher для запиту "user" (знаходить користувача за id)
        DataFetcher<FitnessUser> userFetcher = env -> {
            String userId = env.getArgument("id");
            return fitnessUsers.stream()
                    .filter(u -> u.id().equals(userId))
                    .findFirst()
                    .orElse(null);
        };

        // DataFetcher для запиту "workout" (знаходить тренування за id)
        DataFetcher<Workout> workoutFetcher = env -> {
            String workoutId = env.getArgument("id");
            return fitnessWorkouts.stream()
                    .filter(w -> w.id().equals(workoutId))
                    .findFirst()
                    .orElse(null);
        };

        // DataFetcher для запиту "workouts" (фільтрація тренувань)
        DataFetcher<List<Workout>> workoutsFetcher = env -> {
            Map<String, Object> filter = env.getArgument("filter");
            String userId = (String) filter.get("userId");
            Boolean completed = (Boolean) filter.get("completed");
            String dateFrom = (String) filter.get("dateFrom");
            String dateTo = (String) filter.get("dateTo");

            return fitnessWorkouts.stream()
                    .filter(w -> w.userId().equals(userId))
                    .filter(w -> completed == null || w.completed() == completed)
                    .filter(w -> dateFrom == null || w.timestamp().compareTo(dateFrom) >= 0)
                    .filter(w -> dateTo == null || w.timestamp().compareTo(dateTo) <= 0)
                    .collect(Collectors.toList());
        };

        // DataFetcher для мутації "createWorkout"
        DataFetcher<Workout> createWorkoutFetcher = env -> {
            Map<String, Object> input = env.getArgument("input");
            String userId = (String) input.get("userId");
            List<Map<String, Object>> exerciseInputs = (List<Map<String, Object>>) input.get("exercises");
            String timestamp = (String) input.get("timestamp");
            String workoutId = UUID.randomUUID().toString();

            // Перетворення вхідних даних для вправ у об'єкти Exercise
            List<Exercise> exercises = exerciseInputs.stream().map(e -> {
                String templateId = (String) e.get("templateId");
                int weight = ((Number) e.get("weight")).intValue();
                int sets = ((Number) e.get("sets")).intValue();
                int reps = ((Number) e.get("reps")).intValue();
                int rest = ((Number) e.get("rest")).intValue();
                ExerciseTemplate template = fitnessTemplates.stream()
                        .filter(t -> t.id().equals(templateId))
                        .findFirst().orElse(null);
                return new Exercise(template, weight, sets, reps, rest);
            }).collect(Collectors.toList());

            Workout newWorkout = new Workout(workoutId, userId, exercises, false, timestamp);
            // Додаємо тренування до загального списку
            List<Workout> updatedWorkouts = new ArrayList<>(fitnessWorkouts);
            updatedWorkouts.add(newWorkout);
            fitnessWorkouts = updatedWorkouts;
            // Оновлюємо список тренувань користувача
            List<FitnessUser> updatedUsers = new ArrayList<>();
            for (FitnessUser user : fitnessUsers) {
                if (user.id().equals(userId)) {
                    List<Workout> userWorkouts = new ArrayList<>(user.workouts());
                    userWorkouts.add(newWorkout);
                    updatedUsers.add(new FitnessUser(user.id(), user.name(), user.email(), userWorkouts));
                } else {
                    updatedUsers.add(user);
                }
            }
            fitnessUsers = updatedUsers;
            return newWorkout;
        };

        // DataFetcher для мутації "completeWorkout"
        DataFetcher<Workout> completeWorkoutFetcher = env -> {
            String workoutId = env.getArgument("workoutId");
            List<Workout> updatedWorkouts = new ArrayList<>();
            Workout updatedWorkout = null;
            for (Workout w : fitnessWorkouts) {
                if (w.id().equals(workoutId)) {
                    updatedWorkout = new Workout(w.id(), w.userId(), w.exercises(), true, w.timestamp());
                    updatedWorkouts.add(updatedWorkout);
                } else {
                    updatedWorkouts.add(w);
                }
            }
            fitnessWorkouts = updatedWorkouts;
            // Оновлюємо тренування у користувача
            List<FitnessUser> updatedUsers = new ArrayList<>();
            for (FitnessUser user : fitnessUsers) {
                if (user.id().equals(updatedWorkout.userId())) {
                    List<Workout> userWorkouts = new ArrayList<>();
                    for (Workout w : user.workouts()) {
                        if (w.id().equals(workoutId)) {
                            userWorkouts.add(updatedWorkout);
                        } else {
                            userWorkouts.add(w);
                        }
                    }
                    updatedUsers.add(new FitnessUser(user.id(), user.name(), user.email(), userWorkouts));
                } else {
                    updatedUsers.add(user);
                }
            }
            fitnessUsers = updatedUsers;
            return updatedWorkout;
        };

        return newRuntimeWiring()
                .scalar(dateTimeScalar)
                .type("Query", builder -> builder
                        .dataFetcher("templates", templatesFetcher)
                        .dataFetcher("user", userFetcher)
                        .dataFetcher("users", usersFetcher) // Додано
                        .dataFetcher("workout", workoutFetcher)
                        .dataFetcher("workouts", workoutsFetcher)
                )
                .type("Mutation", builder -> builder
                        .dataFetcher("createWorkout", createWorkoutFetcher)
                        .dataFetcher("completeWorkout", completeWorkoutFetcher)
                )
                .build();
    }

    private static GraphQL createGraphQLEntryPoint(TypeDefinitionRegistry typeDefinitionRegistry,
                                                   RuntimeWiring runtimeWiring) {
        SchemaGenerator schemaGenerator = new SchemaGenerator();
        GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);
        return GraphQL.newGraphQL(graphQLSchema).build();
    }
}
