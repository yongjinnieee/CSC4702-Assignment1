package com.csc4702;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class GeneticAlgorithm {
    // Small constant to avoid division by zero in fitness calculation
    public static final double EPSILON = 1e-8;
    
    // 1. GA Parameters
    private static final int POPULATION_SIZE = 250;
    private static final double MUTATION_RATE = 0.1;
    private static final double CROSSOVER_RATE = 0.90;
    private static final int ELITISM_COUNT = 5;
    
    // 2. Population and Dependencies
    private List<Chromosome> population;
    private final RobotArm2D robotArm; 
    private final double targetX;
    private final double targetY;
    
    public GeneticAlgorithm(RobotArm2D robotArm, double targetX, double targetY) {
        this.robotArm = robotArm;
        this.targetX = targetX;
        this.targetY = targetY;
        this.population = new ArrayList<>();
    }
    
    // Initialize the population
    public void initializePopulation() {
        for (int i = 0; i < POPULATION_SIZE; i++) {
            population.add(new Chromosome());
        }
    }
    
    // The Main Loop Engine
    public List<Chromosome> evolvePopulation() {
        List<Chromosome> newPopulation = new ArrayList<>();
        
        // 1. Elitism: Preserve the best individuals
        // Sort population by fitness (descending)
        population.sort(Comparator.comparing(Chromosome::getFitness).reversed());
        
        // Add the top N chromosomes (Elites)
        for (int i = 0; i < ELITISM_COUNT; i++) {
            newPopulation.add(population.get(i));
        }

        // 2. Main Loop: Generate the rest of the new population
        while (newPopulation.size() < POPULATION_SIZE) {
            
            // a. Selection 
            Chromosome parent1 = selectParent(); 
            Chromosome parent2 = selectParent(); 
            
            // b. Crossover 
            Chromosome child = crossover(parent1, parent2); 
            
            // c. Mutation 
            mutate(child); 
            
            newPopulation.add(child);
        }
        
        this.population = newPopulation;
        
        // 3. Fitness Calculation 
        calculateFitness(); 
        
        return this.population;
    }
    
    // Selection (Tournament Selection)
    private Chromosome selectParent() {
        // 1. Create a "Tournament" of random candidates
        int tournamentSize = 5; 
        Chromosome best = null;

        for (int i = 0; i < tournamentSize; i++) {
            // Pick a random index
            int randomIndex = (int) (Math.random() * population.size());
            Chromosome candidate = population.get(randomIndex);

            // 2. Determine if this candidate is the best so far
            if (best == null || candidate.getFitness() > best.getFitness()) {
                best = candidate;
            }
        }
        // 3. Return the winner
        return best;
    }
    
    // Crossover (Uniform Crossover)
    private Chromosome crossover(Chromosome p1, Chromosome p2) {
        // Check Crossover Rate (e.g. 90% chance to mix, 10% chance to just clone p1)
        if (Math.random() > CROSSOVER_RATE) {
            return new Chromosome(p1.getQ1(), p1.getQ2());
        }

        // 1. Mix Genes
        // 50% chance to get q1 from p1, otherwise from p2
        double childQ1 = (Math.random() < 0.5) ? p1.getQ1() : p2.getQ1();
        
        // 50% chance to get q2 from p1, otherwise from p2
        double childQ2 = (Math.random() < 0.5) ? p1.getQ2() : p2.getQ2();

        // 2. Return new Child
        return new Chromosome(childQ1, childQ2);
    }
    
    // Mutation
    private void mutate(Chromosome c) {
        // 1. Mutate q1?
        if (Math.random() < MUTATION_RATE) {
            // Add a small random value between -0.1 and 0.1 radians
            double mutationAmount = (Math.random() * 0.2) - 0.1;
            c.setQ1(c.getQ1() + mutationAmount);
        }

        // 2. Mutate q2?
        if (Math.random() < MUTATION_RATE) {
            double mutationAmount = (Math.random() * 0.2) - 0.1;
            c.setQ2(c.getQ2() + mutationAmount);
        }

        // 3. Keep angles valid
        c.clampAngles();
    }

    // Fitness Calculation
    public void calculateFitness() {
        for (Chromosome c : population) {
            double[] endEffectorPos = robotArm.getEndEffectorPosition(c.getQ1(), c.getQ2());
            double xE = endEffectorPos[0];
            double yE = endEffectorPos[1];

            // Raw fitness function: Euclidean distance to target
            double error = Math.sqrt(Math.pow(targetX - xE, 2) + Math.pow(targetY - yE, 2));

            // Fitness function: higher is better, avoid division by zero
            double fitness = 1.0 / (error + EPSILON);
            c.setFitness(fitness);
        }
    }
    
    // Public method to get the current best chromosome
    public Chromosome getBestChromosome() {
        // Ensure population is sorted (or sort it)
        population.sort(Comparator.comparing(Chromosome::getFitness).reversed());
        return population.get(0);
    }
}
