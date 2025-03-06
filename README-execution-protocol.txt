Task :ua.edu.lnu.fitnessql.Main.main()
Query Templates: {
    templates {
        id
        name
        description
    }
}
Result Templates: {
  "data" : {
    "templates" : [ {
      "id" : "t1",
      "name" : "Push Up",
      "description" : "A classic push up exercise"
    }, {
      "id" : "t2",
      "name" : "Squat",
      "description" : "A basic squat exercise"
    } ]
  }
}
Query Users: {
    users {
        id
        name
        email
        workouts { id }
    }
}
Result Users: {
  "data" : {
    "users" : [ {
      "id" : "u1",
      "name" : "Alice",
      "email" : "alice@example.com",
      "workouts" : [ ]
    } ]
  }
}
Mutation CreateWorkout: mutation {
    createWorkout(input: {
        userId: "u1",
        timestamp: "2024-02-20T10:00:00Z",
        exercises: [
            { templateId: "t1", weight: 80, sets: 3, reps: 10, rest: 60 },
            { templateId: "t2", weight: 50, sets: 4, reps: 12, rest: 90 }
        ]
    }) {
        id
        completed
        timestamp
    }
}

Result CreateWorkout: {
  "data" : {
    "createWorkout" : {
      "id" : "fc8afc87-6f16-432c-b24f-dbf71206a292",
      "completed" : false,
      "timestamp" : "2024-02-20T10:00:00Z"
    }
  }
}
Query Workouts (before complete): {
    workouts(filter: { userId: "u1", completed: false }) {
        id
        completed
        timestamp
        exercises {
            template { id name description }
            weight
            sets
            reps
            rest
        }
    }
}

Result Workouts (before complete): {
  "data" : {
    "workouts" : [ {
      "id" : "fc8afc87-6f16-432c-b24f-dbf71206a292",
      "completed" : false,
      "timestamp" : "2024-02-20T10:00:00Z",
      "exercises" : [ {
        "template" : {
          "id" : "t1",
          "name" : "Push Up",
          "description" : "A classic push up exercise"
        },
        "weight" : 80,
        "sets" : 3,
        "reps" : 10,
        "rest" : 60
      }, {
        "template" : {
          "id" : "t2",
          "name" : "Squat",
          "description" : "A basic squat exercise"
        },
        "weight" : 50,
        "sets" : 4,
        "reps" : 12,
        "rest" : 90
      } ]
    } ]
  }
}
Mutation CompleteWorkout: mutation {
    completeWorkout(workoutId: "fc8afc87-6f16-432c-b24f-dbf71206a292") {
        id
        completed
        timestamp
    }
}

Result CompleteWorkout: {
  "data" : {
    "completeWorkout" : {
      "id" : "fc8afc87-6f16-432c-b24f-dbf71206a292",
      "completed" : true,
      "timestamp" : "2024-02-20T10:00:00Z"
    }
  }
}
Query Workouts (after complete): {
    workouts(filter: { userId: "u1", completed: false }) {
        id
        completed
        timestamp
        exercises {
            template { id name description }
            weight
            sets
            reps
            rest
        }
    }
}

Result Workouts (after complete): {
  "data" : {
    "workouts" : [ ]
  }
}

BUILD SUCCESSFUL in 2s
3 actionable tasks: 2 executed, 1 up-to-date
1:53:49 PM: Execution finished ':ua.edu.lnu.fitnessql.Main.main()'.
