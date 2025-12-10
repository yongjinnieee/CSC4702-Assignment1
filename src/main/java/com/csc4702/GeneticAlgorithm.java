package com.csc4702;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class GeneticAlgorithm {
    // 1. GA Parameters (Tuning is P5's job)
    private static final int POPULATION_SIZE = 100;
    private static final double MUTATION_RATE = 0.05;
    private static final double CROSSOVER_RATE = 0.90;
    private static final int ELITISM_COUNT = 5;
    
    // 2. Population and Dependencies
    private List<Chromosome> population;
    private final RobotArm2D robotArm; // P2's class
    private final double targetX;
    private final double targetY;
    
    public GeneticAlgorithm(RobotArm2D robotArm, double targetX, double targetY) {
        this.robotArm = robotArm;
        this.targetX = targetX;
        this.targetY = targetY;
        this.population = new ArrayList<>();
    }
    
    // P3 Task: Initialize the population
    public void initializePopulation() {
        for (int i = 0; i < POPULATION_SIZE; i++) {
            population.add(new Chromosome());
        }
    }
    
    // P3 Task: The Main Loop Engine (P4 implements the details)
    public List<Chromosome> evolvePopulation() {
        List<Chromosome> newPopulation = new ArrayList<>();
        
        // 1. Elitism: Preserve the best individuals (P5 will need this for tuning)
        // Sort population by fitness (descending)
        population.sort(Comparator.comparing(Chromosome::getFitness).reversed());
        
        // Add the top N chromosomes (Elites)
        for (int i = 0; i < ELITISM_COUNT; i++) {
            newPopulation.add(population.get(i));
        }

        // 2. Main Loop: Generate the rest of the new population
        while (newPopulation.size() < POPULATION_SIZE) {
            
            // a. Selection (P4's logic)
            Chromosome parent1 = selectParent(); // P4 will write the selection logic
            Chromosome parent2 = selectParent(); // P4 will write the selection logic
            
            // b. Crossover (P4's logic)
            Chromosome child = crossover(parent1, parent2); // P4 will write the crossover logic
            
            // c. Mutation (P4's logic)
            mutate(child); // P4 will write the mutation logic
            
            newPopulation.add(child);
        }
        
        this.population = newPopulation;
        
        // 3. Fitness Calculation (P4 and P2 coordination)
        calculateFitness(); // P4 will primarily write the fitness math
        
        return this.population;
    }
    
    // P4 Task: Selection (Tournament Selection)
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
    
    // P4 Task: Crossover (Uniform Crossover)
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
    
    // P4 Task: Mutation
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

        // 3. Keep angles valid (Use the helper provided in Chromosome.java)
        c.clampAngles();
    }

    // P4 and P2 Coordination: Fitness Calculation
    public void calculateFitness() {
        for (Chromosome c : population) {
            // P2's FK is called here
            double[] endEffectorPos = robotArm.getEndEffectorPosition(c.getQ1(), c.getQ2());
            double xE = endEffectorPos[0];
            double yE = endEffectorPos[1];
            
            // P4's Euclidean Distance logic is applied here
            double error = Math.sqrt(Math.pow(xE - targetX, 2) + Math.pow(yE - targetY, 2));
            
            // P4's Fitness transformation: Maximize 1 / (1 + Error)
            double fitness = 1.0 / (1.0 + error); 
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
