package ua.edu.lnu.fitnessql.fitness;

import java.util.List;

public record Workout(String id, String userId, List<Exercise> exercises, boolean completed, String timestamp) {}
