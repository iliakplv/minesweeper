package com.iliakplv.minesweeper;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private static final int HEIGHT = 8;
	private static final int WIDTH = 8;
	private static final int MINES = 8;

	String[] fieldArray = new String[HEIGHT * WIDTH];
	GridView gridView;
	ArrayAdapter<String> adapter;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		findViewById(R.id.reset_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				resetGame();
				Toast.makeText(MainActivity.this, "New game started", Toast.LENGTH_SHORT).show();
			}
		});
		resetGame();
	}

	private void resetGame() {
		((TextView) findViewById(R.id.message)).setText("");

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

					if (game.isGameOver()) {
						final TextView message = (TextView) findViewById(R.id.message);
						if (game.isWin()) {
							message.setText("You won! :)");
							message.setTextColor(Color.GREEN);
						} else if (game.isLose()) {
							message.setText("You lost! :(");
							message.setTextColor(Color.RED);
						}
					}
				}
			}
		});
		gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				final int clickedRow = position / HEIGHT;
				final int clickedCol = position % WIDTH;

				game.markCell(clickedRow, clickedCol);
				updateFieldArray(game.getMinesField());
				adapter.notifyDataSetChanged();

				return true;
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

					case FlaggedNumber:
					case FlaggedMine:
						label = "(?)";
						break;


					case GameOverPickedMine:
						label = "!*!";
						break;
					case GameOverUnpickedMine:
						label = "*";
						break;
					case GameOverFlaggedMine:
						label = "(*)";
						break;
					case GameOverFlaggedNumber:
						label = "( )";
						break;
				}
				fieldArray[arrayIndex] = label;

				arrayIndex++;
			}
		}
	}

}