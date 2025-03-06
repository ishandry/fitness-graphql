package ua.edu.lnu.fitnessql.fitness;

import java.util.List;

public record FitnessUser(String id, String name, String email, List<Workout> workouts) {}
