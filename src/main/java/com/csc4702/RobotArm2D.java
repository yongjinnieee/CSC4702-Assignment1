package com.csc4702;

// Class representing a 2-link planar robot arm and its kinematics

public class RobotArm2D {
    // Link Lengths
    private final double a1;
    private final double a2;

    public RobotArm2D(double a1, double a2) {
        this.a1 = a1;
        this.a2 = a2;
    }

    // --- HELPER: Rotation Matrix (RotZ) ---
    private Matrix3x3 getRotationMatrix(double angleRad) {
        Matrix3x3 mat = new Matrix3x3(true); // Identity
        mat.setValue(0, 0, Math.cos(angleRad));
        mat.setValue(0, 1, -Math.sin(angleRad));
        mat.setValue(1, 0, Math.sin(angleRad));
        mat.setValue(1, 1, Math.cos(angleRad));
        return mat;
    }

    // --- HELPER: Translation Matrix (TransX) ---
    private Matrix3x3 getTranslationMatrix(double length) {
        Matrix3x3 mat = new Matrix3x3(true); // Identity
        mat.setValue(0, 2, length); // Set X translation
        return mat;
    }

    /**
     * Returns the (x, y) position of the end effector for the given joint angles (q1, q2).
     * Uses homogeneous transformation matrices for forward kinematics.
     */
    public double[] getEndEffectorPosition(double q1, double q2) {
        // 1. Transformation for Arm 1 (Rotate q1 -> Translate a1)
        Matrix3x3 t1 = getRotationMatrix(q1).multiply(getTranslationMatrix(a1));

        // 2. Transformation for Arm 2 (Rotate q2 -> Translate a2)
        Matrix3x3 t2 = getRotationMatrix(q2).multiply(getTranslationMatrix(a2));

        // 3. Combine: T_Global = T1 * T2
        Matrix3x3 tGlobal = t1.multiply(t2);

        // 4. Extract (x, y) from the final matrix (Position is at column 2)
        double x = tGlobal.getValue(0, 2);
        double y = tGlobal.getValue(1, 2);

        return new double[]{x, y};
    }

    /**
     * Returns the (x, y) position of the elbow joint for the given q1 angle.
     * Used for visualization.
     */
    public double[] getJoint2Position(double q1) {
        // Only apply T1
        Matrix3x3 t1 = getRotationMatrix(q1).multiply(getTranslationMatrix(a1));
        return new double[]{t1.getValue(0, 2), t1.getValue(1, 2)};
    }
}
