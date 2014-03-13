package com.sap.sailing.domain.markpassingcalculation.splining;

import java.util.ArrayList;
import java.util.List;

/**
 * A matrix of doubles.
 * @author Martin Hanysz
 *
 */
public class DoubleMatrix {
	// values of the matrix row by row
	private List<List<Double>> matrix = new ArrayList<List<Double>>();
	
	/**
	 * Construct and initialize an {@link DoubleMatrix} of dimensionX to dimensionY values.
	 * @param dimensionX - how many values in x dimension (length of one row)
	 * @param dimensionY - how many values in y direction (length of one column)
	 * @param values - initial values of the matrix ordered row by row
	 */
	public DoubleMatrix(int dimensionX, int dimensionY, double ... values) {
		if (values.length != dimensionX * dimensionY) {
			throw new IllegalArgumentException("Given dimensions of matrix does not match the number of given initial values.");
		}
		for (int y = 0; y < dimensionY; y++) {
			// add one row
			matrix.add(new ArrayList<Double>());
			// fill the row
			for (int x = 0; x < dimensionX; x++) {
				matrix.get(y).add(values[y * dimensionX + x]);
			}
		}
	}
	
	/**
	 * Construct and initialize an {@link DoubleMatrix} of dimensionX to dimensionY values.
	 * @param dimensionX - how many values in x dimension (length of one row)
	 * @param dimensionY - how many values in y direction (length of one column)
	 * @param values - initial values of the matrix ordered row by row
	 */
	public DoubleMatrix(int dimensionX, int dimensionY, List<Double> values) {
		if (values.size() != dimensionX * dimensionY) {
			throw new IllegalArgumentException("Given dimensions of matrix does not match the number of given initial values.");
		}
		for (int y = 0; y < dimensionY; y++) {
			// add one row
			matrix.add(new ArrayList<Double>());
			// fill the row
			for (int x = 0; x < dimensionX; x++) {
				matrix.get(y).add(values.get(y * dimensionX + x));
			}
		}
	}

	/**
	 * Returns the specified entry of the matrix identified by its x and y coordinates.
	 * @param x - the x coordinate (column) of the entry to return
	 * @param y - the y coordinate (row) of the entry to return
	 * @return the value at column x and row y
	 */
	public double get(int x, int y) {
		return matrix.get(y).get(x);
	}

	/**
	 * Returns the number of rows (y dimension) of the matrix.
	 * @return the number of rows of the matrix
	 */
	public int getRowCount() {
		return matrix.size();
	}
	
	/**
	 * Returns the number of columns (x dimension) of the matrix.
	 * @return the number of columns of the matrix
	 */
	public int getColumnCount() {
		return matrix.get(0).size();
	}

	@Override
	public String toString() {
		String result = "DoubleMatrix: ";
		for (List<Double> row : matrix) {
			result += row.toString();
		}
		return result;
	}

	/**
	 * Returns all entries of the column with the index x.
	 * @param x - the number of the column to return
	 * @return a {@link List} of all values of the column number x
	 */
	public List<Double> getColumn(int x) {
		if (x > matrix.get(0).size()) {
			throw new IllegalArgumentException("The matrix does not contain the given amount of columns");
		}
		ArrayList<Double> column = new ArrayList<Double>();
		for (List<Double> row : matrix) {
			column.add(row.get(x));
		}
		return column;
	}
	
	/**
	 * Returns all entries of the row with the index y.
	 * @param y - the number of the row to return
	 * @return a {@link List} of all values of the row number y
	 */
	public List<Double> getRow(int y) {
		if (y > matrix.size()) {
			throw new IllegalArgumentException("The matrix does not contain the given amount of rows");
		}
		return matrix.get(y);
	}

	/**
	 * Multiplies this {@link DoubleMatrix} with the given {@link DoubleMatrix}.
	 * @param matrix - the {@link DoubleMatrix} to multiply this matrix with
	 * @return the {@link DoubleMatrix} representing the result of multiplying this matrix with the given matrix
	 */
	public DoubleMatrix multiply(DoubleMatrix matrix) {
		if (this.getColumnCount() != matrix.getRowCount()) {
			throw new IllegalArgumentException("Matrices can NOT be multiplied. Row count of the given matrix ( " + matrix.toString() + " ) does NOT equal column count of this matrix (" + this.toString() + ").");
		}
		ArrayList<Double> resultValues = new ArrayList<Double>();
		for (int row = 0; row < this.getRowCount(); row++) {
			for (int col = 0; col < matrix.getColumnCount(); col++) {
				double value = 0.0;
				for (int i = 0; i < this.getColumnCount(); i++) {
					value += this.get(i, row) * matrix.get(col, i);
				}
				resultValues.add(value);
			}
		}
		
		return new DoubleMatrix(matrix.getColumnCount(), this.getRowCount(), resultValues);
	}
	
	/**
	 * Multiplies this {@link DoubleMatrix} with the given scalar.
	 * @param scalar - the scalar to multiply this matrix with
	 * @return the {@link DoubleMatrix} representing the result of multiplying this matrix with the given scalar
	 */
	public DoubleMatrix multiply(double scalar) {
		ArrayList<Double> resultValues = new ArrayList<Double>();
		for (int x = 0; x < getColumnCount(); x++) {
			for (int y = 0; y < getRowCount(); y++) {
				resultValues.add(get(x,y) * scalar);
			}
		}
		return new DoubleMatrix(getColumnCount(), getRowCount(), resultValues);
	}
}