package com.iliakplv.minesweeper;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.TextView;

public class MainActivity extends Activity {

	private static final int HEIGHT = 8;
	private static final int WIDTH = 8;
	private static final int MINES = 10;

	String[] fieldArray = new String[HEIGHT * WIDTH];
	GridView gridView;
	ArrayAdapter<String> adapter;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);

		final MinesweeperGame game = new MinesweeperGame(WIDTH, HEIGHT, MINES);
		updateFieldArray(game.getMinesField());

		adapter = new ArrayAdapter<String>(this, R.layout.cell, R.id.label, fieldArray);
		gridView = (GridView) findViewById(R.id.field);
		gridView.setAdapter(adapter);

		gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				final int clickedRow = position / HEIGHT;
				final int clickedCol = position % WIDTH;

				final boolean fieldChanged =
						game.pickCell(clickedRow, clickedCol);

				if (fieldChanged) {
					updateFieldArray(game.getMinesField());
					adapter.notifyDataSetChanged();

					if (game.isWin()) {
						((TextView) findViewById(R.id.message)).setText("You won! :)");
					} else if (game.isLose()) {
						((TextView) findViewById(R.id.message)).setText("You lost! :(");
					}
				}
			}
		});
	}


	public void updateFieldArray(MinesweeperGame.Cell[][] field) {
		int arrayIndex = 0;
		for (int row = 0; row < HEIGHT; row++) {
			for (int col = 0; col < WIDTH; col++) {
				final MinesweeperGame.CellState cellState = field[row][col].getState();

				String label = ".";
				switch (cellState) {
					case PickedNumber:
						final int numberOfAdjacentMines = field[row][col].getNumberOfAdjacentMines();
						if (numberOfAdjacentMines == 0) {
							label = "";
						} else {
							label = String.valueOf(numberOfAdjacentMines);
						}
						break;
					case GameOverPickedMine:
						label = "(!)";
						break;
					case GameOverUnpickedMine:
						label = "*";
						break;
				}
				fieldArray[arrayIndex] = label;

				arrayIndex++;
			}
		}
	}

}