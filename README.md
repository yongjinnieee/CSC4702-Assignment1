# 2-Link SCARA Robot Control System

This project implements a 2-link planar robot arm with both forward and inverse kinematics using a genetic algorithm (GA) for inverse kinematics. It provides both a command-line interface (CLI) for testing and a JavaFX graphical user interface (GUI) for visualization and interactive control.

## Features
- **Forward Kinematics (Part A):**
  - Enter link lengths (A1, A2) and joint angles (Q1, Q2) to calculate and visualize the end effector position.
- **Inverse Kinematics (Part B):**
  - Enter a target (X, Y) position and use a genetic algorithm to solve for joint angles that reach the target.
  - Visualize the solution and convergence in the GUI.

## Getting Started

### Prerequisites
- Java 11 or newer
- Maven 3.6+

### Setup (If Maven is Not Installed)

#### Windows
1. Download Maven from https://maven.apache.org/download.cgi
2. Extract the archive to a folder (e.g., `C:\Program Files\Apache\Maven`)
3. Add Maven to your system PATH:
   - Open "Environment Variables" from Windows search
   - Under "System variables", find and select "Path", then click "Edit"
   - Click "New" and add the path to Maven's `bin` folder (e.g., `C:\Program Files\Apache\Maven\bin`)
   - Click OK to save
4. Open a new terminal and verify installation:
   ```
   mvn -version
   ```

#### macOS/Linux
1. Download Maven from https://maven.apache.org/download.cgi
2. Extract the archive to a folder (e.g., `/opt/maven`)
3. Add Maven to your PATH by editing `~/.bashrc` or `~/.zshrc`:
   ```
   export PATH=/opt/maven/bin:$PATH
   ```
4. Reload your shell configuration:
   ```
   source ~/.bashrc
   ```
5. Verify installation:
   ```
   mvn -version
   ```

### Build and Run (GUI)
1. Open a terminal in the project root.
2. Run:
   ```
   mvn clean javafx:run
   ```
   The JavaFX GUI will launch. You can interact with both forward and inverse kinematics in the right panel.

### Build and Run (CLI)
1. Open a terminal in the project root.
2. Compile:
   ```
   mvn clean compile
   ```
3. Run the CLI optimizer:
   ```
   mvn exec:java -Dexec.mainClass="com.csc4702.IKOptimizer"
   ```
   This will run the genetic algorithm for several test cases and print results to the console.

## Project Structure
- `src/main/java/com/csc4702/`
  - `RobotFXApplication.java` — JavaFX GUI application
  - `IKOptimizer.java` — CLI entry point for GA testing
  - `GeneticAlgorithm.java`, `Chromosome.java` — GA logic
  - `RobotArm2D.java` — Robot arm kinematics
  - `Matrix3x3.java` — 2D transformation matrix helper

## Usage
- **Forward Kinematics:**
  - Enter A1, A2, Q1, Q2 in the GUI and click "Calculate Position" to see the end effector (X, Y).
- **Inverse Kinematics:**
  - Enter a target (X, Y) and click "Solve Angles" to find joint angles using the genetic algorithm.
- **CLI:**
  - Run the CLI to see GA results for predefined test cases.