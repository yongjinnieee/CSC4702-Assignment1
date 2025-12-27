package com.csc4702;

public class IKOptimizer {
    
    // Target position for the IK problem
    private final double targetX;
    private final double targetY;
    
    // Link lengths 
    private final double a1 = 10.0; 
    private final double a2 = 8.0; 
    
    // GA Parameters
    private static final int MAX_GENERATIONS = 300;
    private static final double ERROR_TOLERANCE = 0.01;
    
    // TODO: Connect Part B GA with Jeevan's and Eugene's classes for visualization
    private final RobotArm2D robotArm;
    
    public IKOptimizer(double targetX, double targetY) {
        this.targetX = targetX;
        this.targetY = targetY;
       
        this.robotArm = new RobotArm2D(a1, a2); 
    }
    
    public void runGA() {
        System.out.println("\n--- Running GA for Target: (" + targetX + ", " + targetY + ")");
        
        // Instantiate GA and initialize operations
        GeneticAlgorithm ga = new GeneticAlgorithm(robotArm, targetX, targetY);   
        ga.initializePopulation();
        ga.calculateFitness();
        
        // Start the evolution loop
        for (int generation = 1; generation <= MAX_GENERATIONS; generation++) {
            
            Chromosome best = ga.getBestChromosome();
            
            // 1. Check Termination Condition 
            double bestError = (1.0 / best.getFitness()) - GeneticAlgorithm.EPSILON;
            
            // Output status 
            double[] endPos = robotArm.getEndEffectorPosition(best.getQ1(), best.getQ2());
            System.out.printf("Gen %d: Best Error=%.6f, Angles=(%.2f, %.2f), Pos=(%.2f, %.2f)\n", 
                                generation, 
                                bestError, 
                                Math.toDegrees(best.getQ1()), 
                                Math.toDegrees(best.getQ2()),
                                endPos[0], endPos[1]);

            if (bestError < ERROR_TOLERANCE) {
                System.out.println("Convergence achieved! Error < " + ERROR_TOLERANCE);
                break;
            }
            
            // 2. Evolve to the Next Generation
            ga.evolvePopulation(); // Calls Selection, Crossover, and Mutation
            
            if (generation == MAX_GENERATIONS) {
                 System.out.println("Stopped after max generations. Best error: " + bestError);
            }
        }
    }
    
    // Main method to test the required scenarios
    public static void main(String[] args) {
        // Test Case 1
        IKOptimizer test1 = new IKOptimizer(13.0, 5.0); 
        test1.runGA();
        
        // Test Case 2
        IKOptimizer test2 = new IKOptimizer(-4.0, 16.0); 
        test2.runGA();

        // // Test Case 3
        IKOptimizer test3 = new IKOptimizer(10.0, 10.0); 
        test3.runGA();
    }
}
