package com.csc4702;

// JavaFX application for visualizing and controlling a 2-link SCARA robot arm
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
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
	private Label lblStatus; // To show "Thinking..." or "Converged"
	private Label lblFKResult; // Shows calculated (X, Y) for Part A

	// State Variables (Current position in DEGREES)
	private double currentQ1 = 45;
	private double currentQ2 = -30;

	// Animation Logic
	private Timeline timeline;

	@Override
	public void start(Stage primaryStage) {
		BorderPane root = new BorderPane();
		canvas = new Canvas(800, 600);
		gc = canvas.getGraphicsContext2D();

		// --- PART A: Forward Kinematics ---
		VBox partABox = new VBox(8);
		partABox.setPadding(new Insets(10));
		partABox.setStyle("-fx-background-color: #e3f2fd; -fx-border-color: #2196f3; -fx-border-width: 2;");
		Label lblPartATitle = new Label("Part A: Forward Kinematics");
		lblPartATitle.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #1565c0;");
		Label lblPartAInstr = new Label("Enter A1, A2, Q1, Q2 and click 'Calculate Position' to see the end effector position.");
		lblPartAInstr.setWrapText(true);
		GridPane gridA = new GridPane();
		gridA.setHgap(8); gridA.setVgap(8);
		txtA1 = new TextField("10");
		txtA2 = new TextField("8");
		txtQ1 = new TextField("45");
		txtQ2 = new TextField("-30");
		gridA.addRow(0, new Label("Length A1:"), txtA1);
		gridA.addRow(1, new Label("Length A2:"), txtA2);
		gridA.addRow(2, new Label("Angle Q1 (°):"), txtQ1);
		gridA.addRow(3, new Label("Angle Q2 (°):"), txtQ2);
		Button btnFK = new Button("Calculate Position");
		btnFK.setStyle("-fx-background-color: #90caf9; -fx-font-weight: bold;");
		lblFKResult = new Label("End Effector (X, Y):");
		lblFKResult.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #0d47a1;");
		btnFK.setOnAction(e -> {
			double tQ1 = parse(txtQ1, currentQ1);
			double tQ2 = parse(txtQ2, currentQ2);
			animateArm(tQ1, tQ2);
			double a1 = parse(txtA1, 10);
			double a2 = parse(txtA2, 8);
			RobotArm2D robot = new RobotArm2D(a1, a2);
			double radQ1 = Math.toRadians(tQ1);
			double radQ2 = Math.toRadians(tQ2);
			double[] endPos = robot.getEndEffectorPosition(radQ1, radQ2);
			lblFKResult.setText(String.format("End Effector (X, Y): (%.2f, %.2f)", endPos[0], endPos[1]));
			lblStatus.setText("Position Calculated");
		});
		partABox.getChildren().addAll(lblPartATitle, lblPartAInstr, gridA, btnFK, lblFKResult);

		// --- PART B: Inverse Kinematics ---
		VBox partBBox = new VBox(8);
		partBBox.setPadding(new Insets(10));
		partBBox.setStyle("-fx-background-color: #fce4ec; -fx-border-color: #d81b60; -fx-border-width: 2;");
		Label lblPartBTitle = new Label("Part B: Inverse Kinematics");
		lblPartBTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #ad1457;");
		Label lblPartBInstr = new Label("Enter a target (X, Y) and click 'Solve Angles' to find joint angles.");
		lblPartBInstr.setWrapText(true);
		GridPane gridB = new GridPane();
		gridB.setHgap(8); gridB.setVgap(8);
		txtTargetX = new TextField("13");
		txtTargetY = new TextField("5");
		gridB.addRow(0, new Label("Target X:"), txtTargetX);
		gridB.addRow(1, new Label("Target Y:"), txtTargetY);
		Button btnIK = new Button("Solve Angles");
		btnIK.setStyle("-fx-background-color: #f8bbd0; -fx-font-weight: bold;");
		btnIK.setOnAction(e -> runGeneticAlgorithm());
		partBBox.getChildren().addAll(lblPartBTitle, lblPartBInstr, gridB, btnIK);

		// Status Label
		lblStatus = new Label("Ready");
		lblStatus.setStyle("-fx-text-fill: blue; -fx-font-size: 13px;");

		VBox allControls = new VBox(18, partABox, new Separator(), partBBox, new Separator(), lblStatus);
		allControls.setPrefWidth(340);
		root.setCenter(canvas);
		root.setRight(allControls);

		Scene scene = new Scene(root, 1200, 600);
		primaryStage.setTitle("2-Link SCARA Robot Control System");
		primaryStage.setScene(scene);

		// Initial Draw
		draw(currentQ1, currentQ2);
		// Show initial FK result
		double a1 = parse(txtA1, 10);
		double a2 = parse(txtA2, 8);
		RobotArm2D robot = new RobotArm2D(a1, a2);
		double radQ1 = Math.toRadians(currentQ1);
		double radQ2 = Math.toRadians(currentQ2);
		double[] endPos = robot.getEndEffectorPosition(radQ1, radQ2);
		lblFKResult.setText(String.format("End Effector (X, Y): (%.2f, %.2f)", endPos[0], endPos[1]));
		primaryStage.show();
	}

	/**
	 * PART B: The AI Integration
	 * This method connects the UI to the part B GA class.
	 */
	private void runGeneticAlgorithm() {
		lblStatus.setText("Thinking...");
        
		// 1. Get Setup Data
		double a1 = parse(txtA1, 10);
		double a2 = parse(txtA2, 8);
		double tx = parse(txtTargetX, 10);
		double ty = parse(txtTargetY, 10);

		// 2. Instantiate Backend Classes
		RobotArm2D robot = new RobotArm2D(a1, a2);
		GeneticAlgorithm ga = new GeneticAlgorithm(robot, tx, ty);
		ga.initializePopulation();
		ga.calculateFitness();

		// 3. Run the GA Loop
		int maxGens = 300;
		double errorTolerance = 0.01;
		Chromosome best = null;

		for (int i = 0; i < maxGens; i++) {
			best = ga.getBestChromosome();
			double error = (1.0 / best.getFitness()) - GeneticAlgorithm.EPSILON;

			// Convergence check
			if (error < errorTolerance) {
				lblStatus.setText("Converged at Gen " + i + "!");
				break;
			}
            
			ga.evolvePopulation();
            
			if (i == maxGens - 1) {
				lblStatus.setText("Stopped (Max Gen reached)");
			}
		}

		// 4. Update UI with the result
		if (best != null) {
			// Backend returns Radians -> Convert to Degrees for UI
			double bestQ1Deg = Math.toDegrees(best.getQ1());
			double bestQ2Deg = Math.toDegrees(best.getQ2());

			// Update Text Fields
			txtQ1.setText(String.format("%.2f", bestQ1Deg));
			txtQ2.setText(String.format("%.2f", bestQ2Deg));

			// Animate the robot to the solution
			animateArm(bestQ1Deg, bestQ2Deg);
		}
	}

	/**
	 * Animation Logic (Step-by-Step Approach)
	 */
	private void animateArm(double targetQ1, double targetQ2) {
		if (timeline != null) timeline.stop();

		int frames = 60; // 1 second animation
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
			draw(currentQ1, currentQ2); // Snap to exact end value
		});

		timeline.play();
	}

	/**
	 * Draw Function
	 */
	private void draw(double q1Deg, double q2Deg) {
		gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

		// 1. Get Lengths
		double a1 = parse(txtA1, 10);
		double a2 = parse(txtA2, 8);

		// 2. Instantiate RobotArm2D
		RobotArm2D robot = new RobotArm2D(a1, a2);

		// 3. Convert Degrees to Radians for Math
		double radQ1 = Math.toRadians(q1Deg);
		double radQ2 = Math.toRadians(q2Deg);

		// 4. Get Coordinates from Backend
		double[] elbowPos = robot.getJoint2Position(radQ1);
		double[] endPos = robot.getEndEffectorPosition(radQ1, radQ2);

		// 5. Transform to Screen Coordinates
		double scale = 20.0; 
		double ox = canvas.getWidth() / 2;
		double oy = canvas.getHeight() / 2;

		double x1 = ox + (elbowPos[0] * scale);
		double y1 = oy - (elbowPos[1] * scale); // Invert Y
		double x2 = ox + (endPos[0] * scale);
		double y2 = oy - (endPos[1] * scale);   // Invert Y

		drawTarget(ox, oy, scale);

		// 6. Draw the Robot
		gc.setLineWidth(5);
        
		// Arm 1 (Blue)
		gc.setStroke(Color.BLUE);
		gc.strokeLine(ox, oy, x1, y1);
        
		// Arm 2 (Red)
		gc.setStroke(Color.RED);
		gc.strokeLine(x1, y1, x2, y2);

		// Joints
		gc.setFill(Color.BLACK);
		gc.fillOval(ox - 5, oy - 5, 10, 10); // Base
		gc.fillOval(x1 - 5, y1 - 5, 10, 10); // Elbow
		gc.setFill(Color.GREEN);
		gc.fillOval(x2 - 5, y2 - 5, 10, 10); // End Effector
	}

	private void drawTarget(double ox, double oy, double scale) {
		double tx = parse(txtTargetX, 0) * scale;
		double ty = parse(txtTargetY, 0) * scale;
		double sx = ox + tx;
		double sy = oy - ty;

		gc.setLineWidth(2);
		gc.setStroke(Color.MAGENTA);
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
