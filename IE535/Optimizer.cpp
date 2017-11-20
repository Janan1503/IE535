/*
 * IE535 - Linear Programming Project
 * Problem 20
 *
 A manufacturing firm has discontinued the production of a certain unprofitable product line. This act created considerable excess production capacity. Management is considering devoting this excess capacity to one or more of three products; call them products 1, 2, and 3. The available capacity on the machines that might limit output is summarized in the following table:
 Machine type		Available time (in machine hours per week)
 Milling machine	500
 Lathe				50
 Grinder			150
 The number of machine hours required for each unit of the respective products is

 Productivity coefficient (in machine hours per unit)

 Machine type		Product 1	Product 2	Product 3
 Milling machine	9			3			5
 Lathe				5			4			0
 Grinder			3			0			2
 The sales department indicates that the sales potential for products 1 and 2 exceeds the maximum production rate and that the sales potential for product 3 is 20 units per week. The unit profit would be $30, $12, and $15, respectively, on products 1, 2, and 3. The objective is to determine how much of each product the firm should produce to maximize profit.

 Formulate the linear programming model for this problem.
 *
 *
 Programming Requirements

 To correctly implement the simplex method to solve an LP, you need to at least
 consider the following in your code.

 1. If your LP model is not in the standard form, you can just manually convert them
 into the standard form before inputting to computers. For larger problems,
 however, you may need to design a routine to do the conversion in your code.

 2. Your code needs to be able to detect if the LP at hand is feasible or not.

 3. Your code needs to be able to detect if the coefficient matrix associated with the
 standard form of the LP has full row rank; and if not, how to remove the
 redundant constraints. (Note just checking if the coefficient matrix has full rank or
 not, such as using the Matlab function “rref”, is not sufficient. Your code needs to
 be able to reduce redundant constraints regardless if the specific LP problem you
 chose has redundant constraints or not.)

 4. How to start the simplex method if an initial basis is not readily available. (Your
 code needs to check if a basic feasible solution (BFS) with the identity matrix as
 the corresponding basis is readily available. If yes, you can just go ahead to use
 the BFS to start your simplex method; otherwise, it is strongly recommended for
 you to use the methods we learned in class to start your simplex with an identity
 matrix as the initial basis.

 5. Your code needs to implement one rule to prevent the simplex method from
 cycling

 6. Termination: your code needs to be able to handle both cases at termination – a
 finite optimal solution or unbounded (since you don’t know before hand if the LP
 at hand has an optimal solution or is unbounded).

 *
 */

// Header Files
#include <iostream>
#include<conio.h>
using namespace std;

// Class Declaration

class person {
	//Access - Specifier
public:

	//Variable Declaration
	string name;
	int number;
};

//Converting LP to Standdard Form

void standardForm() {

}

//Main Function

int main() {
	// Object Creation For Class
	person obj;

	//Get Input Values For Object Variables
	cout << "Enter the Name :";
	cin >> obj.name;

	cout << "Enter the Number :";
	cin >> obj.number;

	//Show the Output
	cout << obj.name << ": " << obj.number << endl;

	getch();
	return 0;
}

