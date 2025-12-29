package com.csc4702;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*; // Imports Tab, TabPane, etc.
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;


public class RobotFXApplication extends Application {

    // UI Components
    private Canvas canvas;
    private GraphicsContext gc;
    private TextField txtA1, txtA2, txtQ1, txtQ2, txtTargetX, txtTargetY;
    private Label lblStatus; 
    private Label lblFKResult; 

    // State Variables (Where the robot IS currently)
    private double currentQ1 = 45;
    private double currentQ2 = -30;
    
    // History Variables (Where the robot WAS)
    private double prevQ1 = 0; 
    private double prevQ2 = 0;

    // Animation Logic
    private Timeline timeline;

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        canvas = new Canvas(800, 600);
        gc = canvas.getGraphicsContext2D();


        //         TAB 1: Part A (Manual)
        VBox partABox = new VBox(15); // Increased spacing slightly
        partABox.setPadding(new Insets(15));
        partABox.setStyle("-fx-background-color: #f0f4f8;"); 
        
        Label lblPartAInstr = new Label("Enter angles and click 'Calculate' to move. Click 'Reverse' to undo.");
        lblPartAInstr.setWrapText(true);
        lblPartAInstr.setStyle("-fx-font-style: italic; -fx-text-fill: #555;");
        
        GridPane gridA = new GridPane();
        gridA.setHgap(10); gridA.setVgap(10);
        txtA1 = new TextField("10");
        txtA2 = new TextField("8");
        txtQ1 = new TextField("45");
        txtQ2 = new TextField("-30");
        
        gridA.addRow(0, new Label("Length A1:"), txtA1);
        gridA.addRow(1, new Label("Length A2:"), txtA2);
        gridA.addRow(2, new Label("Angle Q1 (°):"), txtQ1);
        gridA.addRow(3, new Label("Angle Q2 (°):"), txtQ2);
        
        Button btnFK = new Button("Calculate Position (Move)");
        btnFK.setMaxWidth(Double.MAX_VALUE);
        btnFK.setStyle("-fx-background-color: #2196f3; -fx-text-fill: white; -fx-font-weight: bold;");
        
        Button btnReverse = new Button("Reverse Motion (Back to Start)");
        btnReverse.setMaxWidth(Double.MAX_VALUE);
        btnReverse.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white; -fx-font-weight: bold;");
        
        lblFKResult = new Label("End Effector (X, Y):");
        lblFKResult.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #0d47a1;");

        partABox.getChildren().addAll(lblPartAInstr, gridA, btnFK, btnReverse, new Separator(), lblFKResult);
        
        Tab tabA = new Tab("Part A: Manual Control", partABox);
        tabA.setClosable(false); // User cannot close the tab


        //         TAB 2: Part B (AI Solver)
        VBox partBBox = new VBox(15);
        partBBox.setPadding(new Insets(15));
        partBBox.setStyle("-fx-background-color: #fff0f5;"); 
        
        Label lblPartBInstr = new Label("Enter a target (X, Y) and click 'Solve Angles' to find joint angles.");
        lblPartBInstr.setWrapText(true);
        lblPartBInstr.setStyle("-fx-font-style: italic; -fx-text-fill: #555;");
        
        GridPane gridB = new GridPane();
        gridB.setHgap(10); gridB.setVgap(10);
        txtTargetX = new TextField("13");
        txtTargetY = new TextField("5");
        gridB.addRow(0, new Label("Target X:"), txtTargetX);
        gridB.addRow(1, new Label("Target Y:"), txtTargetY);
        
        Button btnIK = new Button("Solve Angles (Genetic Algo)");
        btnIK.setMaxWidth(Double.MAX_VALUE);
        btnIK.setStyle("-fx-background-color: #e91e63; -fx-text-fill: white; -fx-font-weight: bold;");
        
        partBBox.getChildren().addAll(lblPartBInstr, gridB, btnIK);
        
        Tab tabB = new Tab("Part B: AI Solver", partBBox);
        tabB.setClosable(false);

        //         Layout Assembly
        TabPane tabPane = new TabPane(tabA, tabB);
        
        // Status Label 
        lblStatus = new Label("System Ready");
        lblStatus.setPadding(new Insets(10));
        lblStatus.setStyle("-fx-text-fill: #333; -fx-font-weight: bold; -fx-background-color: #ddd;");
        lblStatus.setMaxWidth(Double.MAX_VALUE);

        // Right Side Container
        VBox rightControlPanel = new VBox(tabPane, lblStatus);
        rightControlPanel.setPrefWidth(350);
        
        root.setCenter(canvas);
        root.setRight(rightControlPanel);

        
        //         Logic & Event Handlers
        // --- BUTTON LOGIC: MOVE FORWARD ---
        btnFK.setOnAction(e -> {
            double newQ1 = parse(txtQ1, currentQ1);
            double newQ2 = parse(txtQ2, currentQ2);
            
            // 1. Save history
            prevQ1 = currentQ1;
            prevQ2 = currentQ2;

            System.out.println("LOG: Forward Move Initiated.");
            
            // 2. Animate
            animateArm(newQ1, newQ2);
            updateFKLabel(newQ1, newQ2);
            lblStatus.setText("Moved to new Position");
        });

        // --- BUTTON LOGIC: REVERSE / UNDO ---
        btnReverse.setOnAction(e -> {
            System.out.println("LOG: Reverse Move Initiated.");
            
            double targetQ1 = prevQ1;
            double targetQ2 = prevQ2;
            double tempStartQ1 = currentQ1;
            double tempStartQ2 = currentQ2;

            // Update UI
            txtQ1.setText(String.format("%.2f", targetQ1));
            txtQ2.setText(String.format("%.2f", targetQ2));

            // Animate
            animateArm(targetQ1, targetQ2);
            updateFKLabel(targetQ1, targetQ2);
            
            // Update History (Ping-Pong Logic)
            prevQ1 = tempStartQ1;
            prevQ2 = tempStartQ2;

            lblStatus.setText("Reversed to Previous Frame");
        });

        // --- BUTTON LOGIC: AI SOLVE ---
        btnIK.setOnAction(e -> runGeneticAlgorithm());

        Scene scene = new Scene(root, 1150, 600);
        primaryStage.setTitle("2-Link SCARA Robot Control System");
        primaryStage.setScene(scene);

        // Initial Draw
        draw(currentQ1, currentQ2);
        updateFKLabel(currentQ1, currentQ2);
        prevQ1 = 0; prevQ2 = 0;

        primaryStage.show();
    }

    private void updateFKLabel(double q1, double q2) {
        double a1 = parse(txtA1, 10);
        double a2 = parse(txtA2, 8);
        RobotArm2D robot = new RobotArm2D(a1, a2);
        double radQ1 = Math.toRadians(q1);
        double radQ2 = Math.toRadians(q2);
        double[] endPos = robot.getEndEffectorPosition(radQ1, radQ2);
        lblFKResult.setText(String.format("End Effector (X, Y): (%.2f, %.2f)", endPos[0], endPos[1]));
    }

    private void runGeneticAlgorithm() {
        lblStatus.setText("Thinking...");
        double a1 = parse(txtA1, 10);
        double a2 = parse(txtA2, 8);
        double tx = parse(txtTargetX, 10);
        double ty = parse(txtTargetY, 10);

        RobotArm2D robot = new RobotArm2D(a1, a2);
        GeneticAlgorithm ga = new GeneticAlgorithm(robot, tx, ty);
        ga.initializePopulation();
        ga.calculateFitness();

        int maxGens = 300;
        double errorTolerance = 0.01;
        Chromosome best = null;

        for (int i = 0; i < maxGens; i++) {
            best = ga.getBestChromosome();
            double error = (1.0 / best.getFitness()) - GeneticAlgorithm.EPSILON;

            if (error < errorTolerance) {
                lblStatus.setText("Converged at Gen " + i + "!");
                break;
            }
            ga.evolvePopulation();
            if (i == maxGens - 1) {
                lblStatus.setText("Stopped (Max Gen reached)");
            }
        }

        if (best != null) {
            prevQ1 = currentQ1;
            prevQ2 = currentQ2;

            double bestQ1Deg = Math.toDegrees(best.getQ1());
            double bestQ2Deg = Math.toDegrees(best.getQ2());
            
            txtQ1.setText(String.format("%.2f", bestQ1Deg));
            txtQ2.setText(String.format("%.2f", bestQ2Deg));
            animateArm(bestQ1Deg, bestQ2Deg);
        }
    }

    private void animateArm(double targetQ1, double targetQ2) {
        if (timeline != null) timeline.stop();

        int frames = 60; 
        double stepQ1 = (targetQ1 - currentQ1) / frames;
        double stepQ2 = (targetQ2 - currentQ2) / frames;

        timeline = new Timeline(new KeyFrame(Duration.millis(16), e -> {
            currentQ1 += stepQ1;
            currentQ2 += stepQ2;
            draw(currentQ1, currentQ2);
        }));

        timeline.setCycleCount(frames);
        timeline.setOnFinished(e -> {
            currentQ1 = targetQ1;
            currentQ2 = targetQ2;
            draw(currentQ1, currentQ2);
            updateFKLabel(currentQ1, currentQ2);
        });

        timeline.play();
    }

    private void draw(double q1Deg, double q2Deg) {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        double a1 = parse(txtA1, 10);
        double a2 = parse(txtA2, 8);
        RobotArm2D robot = new RobotArm2D(a1, a2);

        double radQ1 = Math.toRadians(q1Deg);
        double radQ2 = Math.toRadians(q2Deg);

        double[] elbowPos = robot.getJoint2Position(radQ1);
        double[] endPos = robot.getEndEffectorPosition(radQ1, radQ2);

        double scale = 20.0; 
        double ox = canvas.getWidth() / 2;
        double oy = canvas.getHeight() / 2;

        double x1 = ox + (elbowPos[0] * scale);
        double y1 = oy - (elbowPos[1] * scale); 
        double x2 = ox + (endPos[0] * scale);
        double y2 = oy - (endPos[1] * scale);  

        drawTarget(ox, oy, scale);

        gc.setLineWidth(5);
        gc.setStroke(Color.BLUE); gc.strokeLine(ox, oy, x1, y1);
        gc.setStroke(Color.RED); gc.strokeLine(x1, y1, x2, y2);

        gc.setFill(Color.BLACK); gc.fillOval(ox - 5, oy - 5, 10, 10);
        gc.fillOval(x1 - 5, y1 - 5, 10, 10);
        gc.setFill(Color.GREEN); gc.fillOval(x2 - 5, y2 - 5, 10, 10);
    }

    private void drawTarget(double ox, double oy, double scale) {
        double tx = parse(txtTargetX, 0) * scale;
        double ty = parse(txtTargetY, 0) * scale;
        double sx = ox + tx;
        double sy = oy - ty;
        gc.setLineWidth(2); gc.setStroke(Color.MAGENTA);
        gc.strokeLine(sx - 10, sy, sx + 10, sy);
        gc.strokeLine(sx, sy - 10, sx, sy + 10);
        gc.strokeText("Target", sx + 5, sy - 5);
    }

    private double parse(TextField field, double defaultVal) {
        try { return Double.parseDouble(field.getText()); } 
        catch (NumberFormatException e) { return defaultVal; }
    }

    public static void main(String[] args) { launch(args); }
}