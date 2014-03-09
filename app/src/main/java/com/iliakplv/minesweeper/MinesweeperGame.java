package com.iliakplv.minesweeper;

import android.util.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

public class MinesweeperGame {

	private static final int MAX_FIELD_SIZE = 1000;

	final private int fieldWidth;
	final private int fieldHeight;
	final private int minesCount;
	final private Cell[][] minesMatrix;
	private int unpickedNumbers;

	boolean gameOver = false;
	boolean win = false;


	public MinesweeperGame(int fieldWidth, int fieldHeight, int minesCount) {
		if (fieldWidth <= 0 || fieldWidth > MAX_FIELD_SIZE) {
			throw new IllegalArgumentException("Wrong fieldArray width: " + fieldWidth);
		}
		if (fieldHeight <= 0 || fieldHeight > MAX_FIELD_SIZE) {
			throw new IllegalArgumentException("Wrong fieldArray height: " + fieldHeight);
		}
		if (minesCount > fieldWidth * fieldHeight) {
			throw new IllegalArgumentException("Number of mines greater than number of cells");
		}

		this.fieldWidth = fieldWidth;
		this.fieldHeight = fieldHeight;
		this.minesCount = minesCount;
		minesMatrix = new Cell[fieldHeight][fieldWidth];
		unpickedNumbers = fieldWidth * fieldHeight - minesCount;

		initializeField();
	}


	private void initializeField() {
		plantMines();
		shuffleMines();
		countAdjacentMines();
	}

	private void plantMines() {
		int minesToPlant = minesCount;
		for (int row = 0; row < fieldHeight; row++) {
			for (int col = 0; col < fieldWidth; col++) {
				if (minesToPlant > 0) {
					minesMatrix[row][col] = new Cell(CellState.UnpickedMine, 0);
					minesToPlant--;
				} else {
					minesMatrix[row][col] = new Cell(CellState.UnpickedNumber, 0);
				}
			}
		}
	}

	private void shuffleMines() {
		final Random random = new Random();
		for (int row = 0; row < fieldHeight; row++) {
			for (int col = 0; col < fieldWidth; col++) {
				final int newRow = random.nextInt(fieldHeight);
				final int newCol = random.nextInt(fieldWidth);
				swapCells(row, col, newRow, newCol);
			}
		}
	}

	private void swapCells(int row1, int col1, int row2, int col2) {
		Cell temp = minesMatrix[row1][col1];
		minesMatrix[row1][col1] = minesMatrix[row2][col2];
		minesMatrix[row2][col2] = temp;
	}

	private Collection<Pair<Integer, Integer>> getAdjacentCells(int row, int col) {
		final ArrayList<Pair<Integer, Integer>> result = new ArrayList<Pair<Integer, Integer>>(8);

		for (int verticalOffset = -1; verticalOffset <= 1; verticalOffset++) {
			final int adjacentRow = row + verticalOffset;
			if (adjacentRow >= 0 && adjacentRow < fieldHeight) {
				for (int horizontalOffset = -1; horizontalOffset <= 1; horizontalOffset++) {
					final int adjacentCol = col + horizontalOffset;
					if (adjacentCol >= 0 && adjacentCol < fieldWidth) {
						result.add(new Pair<Integer, Integer>(adjacentRow, adjacentCol));
					}
				}
			}
		}

		return result;
	}

	private void countAdjacentMines() {
		for (int row = 0; row < fieldHeight; row++) {
			for (int col = 0; col < fieldWidth; col++) {
				if (minesMatrix[row][col].state == CellState.UnpickedNumber) {
					int minesCount = 0;
					for (Pair<Integer, Integer> cell : getAdjacentCells(row, col)) {
						if (minesMatrix[cell.first][cell.second].isMine()) {
							minesCount++;
						}
					}
					minesMatrix[row][col].numberOfAdjacentMines = minesCount;
				}
			}
		}
	}


	/**
	 * @return current mines field
	 */
	public Cell[][] getMinesField() {
		final Cell[][] result = new Cell[fieldHeight][fieldWidth];
		for (int row = 0; row < fieldHeight; row++) {
			System.arraycopy(minesMatrix[row], 0, result[row], 0, fieldWidth);
		}
		return result;
	}

	/**
	 * Set/Clear Flag or Question on cell
	 *
	 * @return {@code true} if something changed on mines field
	 */
	public boolean markCell(int row, int col) {
		if (isGameOver()) {
			return false;
		}
		checkCoordinates(row, col);

		boolean changed = false;
		final Cell cell = minesMatrix[row][col];
		if (cell.state == CellState.UnpickedNumber) { // flag
			cell.state = CellState.FlaggedNumber;
			changed = true;
		} else if (cell.state == CellState.UnpickedMine) { // flag
			cell.state = CellState.FlaggedMine;
			changed = true;
		} else if (cell.state == CellState.FlaggedNumber) { // question
			cell.state = CellState.QuestionNumber;
			changed = true;
		} else if (cell.state == CellState.FlaggedMine) { // question
			cell.state = CellState.QuestionMine;
			changed = true;
		} else if (cell.state == CellState.QuestionNumber) { // clear
			cell.state = CellState.UnpickedNumber;
			changed = true;
		} else if (cell.state == CellState.QuestionMine) { // clear
			cell.state = CellState.UnpickedMine;
			changed = true;
		}

		return changed;
	}


	/**
	 * Pick cell on mines field
	 *
	 * @param pickedRow
	 * @param pickedCol
	 * @return {@code true} if something changed on mines field
	 */
	public boolean pickCell(int pickedRow, int pickedCol) {
		if (isGameOver()) {
			return false;
		}
		checkCoordinates(pickedRow, pickedCol);

		final Cell pickedCell = minesMatrix[pickedRow][pickedCol];
		if (pickedCell.state == CellState.UnpickedNumber ||
				pickedCell.state == CellState.UnpickedMine) {

			if (pickedCell.state == CellState.UnpickedMine) {
				onMinePicked(pickedRow, pickedCol);
			} else {
				onNumberPicked(pickedRow, pickedCol);
			}

			return true;
		}

		return false;
	}

	private void checkCoordinates(int pickedRow, int pickedCol) {
		if (pickedRow < 0 || pickedRow >= fieldHeight) {
			throw new IllegalArgumentException("Wrong row number: " + pickedRow);
		}
		if (pickedCol < 0 || pickedCol >= fieldWidth) {
			throw new IllegalArgumentException("Wrong column number: " + pickedCol);
		}
	}

	private void onNumberPicked(int pickedRow, int pickedCol) {
		final Cell pickedCell = minesMatrix[pickedRow][pickedCol];
		pickedCell.state = CellState.PickedNumber;

		if (pickedCell.numberOfAdjacentMines == 0) {
			// Pick all unpicked adjacent cells
			for (Pair<Integer, Integer> cell : getAdjacentCells(pickedRow, pickedCol)) {
				if (minesMatrix[cell.first][cell.second].state == CellState.UnpickedNumber) {
					pickCell(cell.first, cell.second);
				}
			}
		}

		// Check if all cells without mines picked
		unpickedNumbers--;
		if (unpickedNumbers == 0) {
			setWin();
			showUnpickedMines();
		}
	}

	private void onMinePicked(int pickedRow, int pickedCol) {
		minesMatrix[pickedRow][pickedCol].state = CellState.GameOverPickedMine;
		setLose();
		showUnpickedMines();
	}

	private void showUnpickedMines() {
		for (int row = 0; row < fieldHeight; row++) {
			for (int col = 0; col < fieldWidth; col++) {
				if (minesMatrix[row][col].state == CellState.UnpickedMine ||
						minesMatrix[row][col].state == CellState.QuestionMine) {
					// flagged mines not changing
					minesMatrix[row][col].state = CellState.GameOverUnpickedMine;
				}
			}
		}
	}

	private void setWin() {
		gameOver = true;
		win = true;
	}

	private void setLose() {
		gameOver = true;
		win = false;
	}

	public boolean isGameOver() {
		return gameOver;
	}

	public boolean isWin() {
		return gameOver && win;
	}

	public boolean isLose() {
		return gameOver && !win;
	}


	/**
	 * **************************
	 * Inner classes
	 * ***************************
	 */

	public static enum CellState {
		// initial states
		UnpickedNumber,
		UnpickedMine,

		// in-game states
		FlaggedNumber,
		FlaggedMine,
		QuestionNumber,
		QuestionMine,
		PickedNumber,

		// game over states
		GameOverPickedMine,
		GameOverUnpickedMine
		// TODO add GameOverFlaggedMine
		// TODO add GameOverFlaggedNumber
	}

	public static class Cell {
		private CellState state;
		private int numberOfAdjacentMines;

		public Cell(CellState state, int numberOfAdjacentMines) {
			this.state = state;
			this.numberOfAdjacentMines = numberOfAdjacentMines;
		}

		public boolean isMine() {
			switch (state) {
				case UnpickedMine:
				case FlaggedMine:
				case QuestionMine:
				case GameOverUnpickedMine:
				case GameOverPickedMine:
					return true;

				case UnpickedNumber:
				case FlaggedNumber:
				case QuestionNumber:
				case PickedNumber:
					return false;

				default:
					throw new RuntimeException("Unknown cell state: " + state.toString());
			}
		}

		public CellState getState() {
			return state;
		}

		public int getNumberOfAdjacentMines() {
			return numberOfAdjacentMines;
		}

	}
}
