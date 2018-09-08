package com.jan.main;

import java.util.ArrayList;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealMatrix;

import com.jan.dto.Constraint;
import com.jan.dto.Equation;

public class StandardForm {

	/*
	 * xdash - simplex tableau 
	 * A - coefficient matrix 
	 * b - constants matrix 
	 * solIndex- contains the variables which are currently in the basis
	 */

	static double xdash[][];
	static double A[][];
	static double b[];
	static double B[][];
	static double[] objDash;
	static int[] solIndex;
	static String solutionDes;
	static double[] solution;

	static ArrayList<Constraint> constraints = new ArrayList<>();
	static int[][] comb;
	static int row = 0;

	// Incase of artificial variables

	static double xartif[][];
	static int artifCount = 0;
	static int[] artifIndex;

	// Enter the objective and the constraints here.

	static String objString = "MAX";
	static Equation obj = new Equation(new double[] { 30, 12, 15 }, 0);
	static Constraint Cons1 = new Constraint(new double[] { 9, 3, 5 }, "<=", 500);
	static Constraint Cons2 = new Constraint(new double[] { 5, 4, 0 }, "<=", 350);
	static Constraint Cons3 = new Constraint(new double[] { 3, 0, 2 }, "<=", 150);
	static Constraint Cons4 = new Constraint(new double[] { 0, 0, 1 }, "<=", 20);

	public static void main(String[] args) {

		String main = "Linear Programming\n";
		System.out.println(main);

		constraints.add(Cons1);
		constraints.add(Cons2);
		constraints.add(Cons3);
		constraints.add(Cons4);

		// Converting the objective to Min.

		int matLength = obj.a.length;
		if (objString.equals("MAX")) {
			for (int i = 0; i < obj.a.length; i++)
				obj.a[i] *= -1;
		}

		// finding position to add slack variable

		int pos = 0;
		for (Constraint cons : constraints) {
			if (matLength <= cons.a.length) {
				matLength = cons.a.length;
			}
		}
		// array to store the positions of artificial variables in tableau

		artifIndex = new int[constraints.size()];
		int f = 0;

		// Adding slack and artificial variables

		for (int z = 0; z < 2; z++) {
			for (Constraint cons : constraints) {
				if (cons.sign.equals(">=")) {
					// adding slack variable in the first round and artificial variable in the
					// second round
					pos = pos + 1;

					int newLength = pos + matLength;
					int i = 0;
					objDash = new double[newLength];
					for (; i < cons.a.length; i++) {
						objDash[i] = cons.a[i];
					}
					for (; i < newLength - 2; i++) {
						objDash[i] = 0;
					}

					if (z == 1) {
						objDash[i] = 1;
						artifIndex[f] = i;
						artifCount++;
						f++;
					} else {
						objDash[i] = -1;
					}
					cons.a = objDash;
				}

				if (cons.sign.equals("<=")) {
					// adding slack variable in the first round only
					pos = pos + 1;
					int newLength = pos + matLength;
					int i = 0;
					objDash = new double[newLength];
					for (; i < cons.a.length; i++) {
						objDash[i] = cons.a[i];
					}
					for (; i < newLength - 1; i++) {
						objDash[i] = 0;
					}
					objDash[i] = 1;
					cons.sign = "-";
					cons.a = objDash;
				}

				if (cons.sign.equals("=")) {
					// artificial variable in the second round only
					if (z == 1) {
						pos = pos + 1;
						int newLength = pos + matLength;
						int i = 0;
						objDash = new double[newLength];
						for (; i < cons.a.length; i++) {
							objDash[i] = cons.a[i];
						}
						for (; i < newLength - 1; i++) {
							objDash[i] = 0;
						}

						objDash[i] = 1;
						artifIndex[f] = i;
						artifCount++;
						f++;

						cons.a = objDash;
					}
				}
			}
		}

		// Initial BFS Starting with slack variables in the matrix
		solIndex = new int[constraints.size()];
		for (int i = matLength + pos, j = constraints.size() - 1; j >= 0; i--, j--) {
			solIndex[j] = i;
		}

		pos = matLength + pos;

		// Completing tableau with values.

		for (Constraint cons : constraints) {
			if (pos != matLength) {
				int newLength = pos;
				int i = 0;
				objDash = new double[newLength];
				for (; i < cons.a.length; i++) {
					objDash[i] = cons.a[i];
				}
				for (; i < newLength; i++) {
					objDash[i] = 0;
				}
				cons.a = objDash;

			}

		}
		// Adding the objective and constraint values to the tableau
		if (pos != 1) {
			int newLength = pos;
			int i = 0;
			objDash = new double[newLength];
			for (; i < obj.a.length; i++) {
				objDash[i] = obj.a[i];
			}
			for (; i < newLength; i++) {
				objDash[i] = 0;
			}
			obj.a = objDash;

		}

		xdash = new double[constraints.size() + 1][pos + 1];

		int j = 0;
		for (j = 0; j < pos; j++) {
			xdash[0][j] = obj.a[j];
		}
		xdash[0][j] = obj.b;
		int i = 1;
		for (Constraint cons : constraints) {
			for (j = 0; j < pos; j++) {
				xdash[i][j] = cons.a[j];
			}
			xdash[i][j] = cons.b;
			i++;
		}
		// printing the newly formed simplex Tableau
		System.out.println("Simplex Tableu : \n\n");
		printSimplexTable(xdash);

		// Finding if there is a feasible solution to the LP.
		int flag = bfsprinter();
		// only if there iss feasible solution available the code proceeds with simplex.

		if (flag == 0) {
			// only if artificial variables are there in the model, the code implements
			// phase 1 simplex, else, moves to phase 2.
			if (artifCount > 0) {
				
				double[][] xstar = phaseOneSimplex();
				if(!solutionDes.equals("The problem is infeasible")) {
					phaseTwoSimplex(xstar);
				}
				
			}
			phaseTwoSimplex(xdash);
		}
	}

	// this function prints the simplex table.
	public static void printSimplexTable(double[][] x) {

		for (int i = 0; i < x.length; i++) {

			for (int j = 0; j < x[i].length; j++) {
				System.out.format("%8.2f", x[i][j]);
				System.out.print(" |");
			}
			System.out.println("");
		}

	}

	// finds the minimum value in the array.
	public static int minIndex(double[] d) {
		double min = 1000000;
		int minIndex = 0;
		int i = 0;
		for (double temp : d) {
			if (temp < min) {
				min = temp;
				minIndex = i;
				i++;
			} else {
				i++;
			}
		}

		return minIndex;
	}

	// finds the first index with minimum value - satisfying Bland's rule
	public static int blandIndex(double[] d) {
		double min = 1000000;
		int minIndex = 0;
		int i = 0;
		for (double temp : d) {
			if (temp < 0) {
				min = temp;
				minIndex = i;
				break;
			} else {
				i++;
			}
		}

		return minIndex;

	}

	public static int blandIndex2(double[] d) {
		double min = 1000000;
		int minIndex = 0;
		int i = 0;
		for (double temp : d) {
			if (temp < min) {
				min = temp;
				minIndex = i;
				i++;
			} else {
				i++;
			}
		}

		return minIndex;

	}

	// Phase I simplex implementation
	public static double[][] phaseOneSimplex() {

		System.out.println("\n\nPhase I Implementation : \n");
		RealMatrix xMatrix = new Array2DRowRealMatrix(xdash);
		RealMatrix xMatrix2 = xMatrix.copy();
		xartif = xMatrix2.getData();

		// initialising phase one tableau with values
		for (int i = 0; i < xartif[0].length; i++) {
			xartif[0][i] = 0;
		}

		for (int i = 0; i < artifCount; i++) {
			xartif[0][artifIndex[i]] = -1;
		}
		int k = 0;
		int minIndex = 0;
		int leavingIndex = 0;

		for (int j = artifCount; j > 0; j--)
			for (int i = 0; i < xartif[0].length; i++) {
				xartif[0][i] = xartif[0][i] - xartif[j][i];
			}

		int m = xartif.length;
		int n = xartif[0].length;

		printSimplexTable(xartif);
		// initialising phase one tableau with values
		do {
			System.out.println("");
			System.out.println("Iteration : " + (k + 1) + ":");
			double[] minartif = new double[xartif[0].length - 1];

			for (int i = 0; i < xartif[0].length - 1; i++) {
				minartif[i] = xartif[0][i];
			}
			minIndex = minIndex(minartif);
			double[] ratio = new double[m];
			ratio[0] = 10000;
			for (int i = 1; i < m; i++) {
				ratio[i] = xartif[i][n - 1] / xartif[i][minIndex];
			}
			leavingIndex = minIndex(ratio);
			solIndex[leavingIndex - 1] = minIndex;
			double division = xartif[leavingIndex][minIndex];
			for (int j = 0; j < n; j++) {
				xartif[leavingIndex][j] = xartif[leavingIndex][j] / division;
			}
			for (int i = 0; i < m; i++) {
				if (i == leavingIndex) {
					continue;
				} else {
					double factor = xartif[i][minIndex];
					double[] facRow = new double[n];
					for (int j = 0; j < n; j++) {
						facRow[j] = xartif[leavingIndex][j] * factor;
					}
					for (int j = 0; j < n; j++) {
						xartif[i][j] = xartif[i][j] - facRow[j];
					}
				}
			}
			printSimplexTable(xartif);
			int flag2 = 0;
			for (int i = 0; i < xartif[0].length; i++) {
				for (int j = 0; j < artifCount; j++) {
					if (artifIndex[j] == i) {
						if (xartif[0][i] == -1)
							flag2++;
						break;
					}
				}
				if (xartif[0][i] == 0)
					flag2++;
			}
			if (flag2 == xartif[0].length) {
				k++;
			}
			System.out.println("");
		} while (k == 0);

		for (int i = 0; i < xdash[0].length - 1; i++) {
			xartif[0][i] = xdash[0][i];
		}
		int j = 0;

		double[][] xdash2 = new double[xdash.length][xdash[0].length - artifCount];
		for (int i = 0; i < xdash.length; i++) {
			for (j = 0; j < xdash[0].length - artifCount - 1; j++) {
				xdash2[i][j] = xartif[i][j];
			}
		}
		int o = j;
		j = j + artifCount;
		for (int s = 0; s < xdash.length; s++) {
			xdash2[s][o] = xartif[s][j];
		}
		if(xdash2[0][xdash[0].length]>0) {
			System.out.println("This problem is infeasible");
			solutionDes = "The problem is infeasible";
		}
		printSimplexTable(xdash2);
		return xdash2;
	}

	public static void phaseTwoSimplex(double[][] xdash) {
		// Phase II
		int minIndex = 0;
		int leavingIndex = 0;
		int m = xdash.length;
		int n = xdash[0].length;
		System.out.println("\n\nPhase II implementation:\n");
		printSimplexTable(xdash);

		// Iteration1
		// finding entering variable
		for (int k = 0; k < 6; k++) {
			System.out.println("\nIteration : " + k);
			if (k > 20) {
				minIndex = blandIndex(xdash[0]);
			} else {
				minIndex = minIndex(xdash[0]);
			}

			// optimal solution is obtained
			if (xdash[0][minIndex] >= 0) {
				solutionDes = "Optimal Solution Obtained";
				presentSolution(xdash);
				break;
			}
			// not optimal yet
			else {

				// finding which variable leaves - ratio test
				double[] ratio = new double[m];

				ratio[0] = 10000;
				for (int i = 1; i < m; i++) {
					ratio[i] = xdash[i][n - 1] / xdash[i][minIndex];
				}
				int flag = 0;
				for (int i = 1; i < m; i++)
					if (xdash[i][minIndex] < 0) {
						flag++;
					}
				if (flag == m) {
					solutionDes = "\n\nThe Problem is Unbounded";
					break;
				}
				if (flag < m) {
					for (int i = 1; i < m; i++)
						if (ratio[i] < 0) {
							ratio[i] = 100000;
						}
				}
				if (k > 20) {
					leavingIndex = blandIndex2(ratio);
				} else {
					leavingIndex = minIndex(ratio);
				}
				solIndex[leavingIndex - 1] = minIndex;
			}
			// operation
			double division = xdash[leavingIndex][minIndex];
			for (int j = 0; j < n; j++) {
				xdash[leavingIndex][j] = xdash[leavingIndex][j] / division;
			}
			for (int i = 0; i < m; i++) {
				if (i == leavingIndex) {
					continue;
				} else {
					double factor = xdash[i][minIndex];
					double[] facRow = new double[n];
					for (int j = 0; j < n; j++) {
						facRow[j] = xdash[leavingIndex][j] * factor;
					}
					for (int j = 0; j < n; j++) {
						xdash[i][j] = xdash[i][j] - facRow[j];
					}
				}
			}
			printSimplexTable(xdash);
			System.out.println("");
			System.out.println("");
		}
	}

	public static int fac(int number) {
		int result = 1;
		for (int factor = 2; factor <= number; factor++) {
			result *= factor;
		}
		return result;
	}

	static void combUtil(int arr[], int data[], int start, int end, int index, int r) {

		if (index == r) {
			for (int j = 0; j < r; j++) {
				comb[row][j] = data[j] - 1;
			}
			row++;
			return;
		}
		for (int i = start; i <= end && end - i + 1 >= r - index; i++) {
			data[index] = arr[i];
			combUtil(arr, data, i + 1, end, index + 1, r);
		}
	}

	static void printCombination(int arr[], int n, int r) {
		int data[] = new int[r];
		combUtil(arr, data, 0, n - 1, 0, r);
	}

	// This function compute solves for Basic feasible solution
	public static int bfsprinter() {
		A = new double[xdash.length - 1][xdash[0].length - 1];
		b = new double[xdash.length - 1];
		int combination = fac(xdash[0].length - 1) / fac(xdash[0].length - xdash.length) * fac(xdash[0].length - 1);
		comb = new int[combination][xdash.length - 1];
		int arr[] = new int[xdash[0].length - 1];
		for (int i = 0; i < xdash[0].length - 1; i++)
			arr[i] = i + 1;
		int r = xdash.length - 1;
		int n = arr.length;
		printCombination(arr, n, r);
		double[][] pdash;

		for (int i = 1, k = 0; i < xdash.length; i++, k++) {
			for (int j = 0; j < xdash[0].length - 1; j++) {
				A[k][j] = xdash[i][j];
			}
			b[k] = xdash[i][xdash[0].length - 1];
		}
		RealMatrix bMatrix = new Array2DRowRealMatrix(b);

		double[][] B = new double[xdash.length - 1][xdash.length - 1];
		int flag;
		int k = 0;
		do {
			flag = 0;
			for (int x = 0, l = 0; x < r; x++, l++) {
				for (int j = 0; j < xdash.length - 1; j++) {
					B[j][l] = A[j][comb[k][x]];

				}
			}
			RealMatrix BMatrix = new Array2DRowRealMatrix(B);
			RealMatrix BInverse = new LUDecomposition(BMatrix).getSolver().getInverse();
			RealMatrix p = BInverse.multiply(bMatrix);
			pdash = p.getData();
			for (int i = 0; i < pdash.length; i++) {
				if (pdash[i][0] < 0) {
					flag++;
				}
			}
			k++;
		} while (flag != 0);
		// when no basic feasible solution - the program returns infeasible
		if (flag != 0) {
			System.out.println("This problem is infeasible");
			solutionDes = "The problem is infeasible";
		}
		return flag;
	}

	public static void presentSolution(double[][] xdash) {
		System.out.println("\n\n" + solutionDes);
		for (int i = 1; i < xdash.length; i++)
			System.out.println("X" + (solIndex[i - 1] + 1) + " = " + xdash[i][xdash[0].length - 1]);
	}

	public static int checkrowrank() {
		int rank = A.length;

		// proportionate values
		for (int i = 1; i < xdash.length; i++)
			for (int j = 1; j < xdash[0].length; j++) {

			}

		// linear sum of 2 rows
		for (int i = 1; i < xdash.length; i++)
			for (int j = 1; j < xdash[0].length; j++) {

			}

		return artifCount;
	}

}
