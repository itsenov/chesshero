package com.chesshero.ui.chessboard;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.chesshero.R;
import com.kt.game.BoardField;

/**
 * Created by Vasil on 6.12.2014 г..
 */
public class ChessboardAdapter extends BaseAdapter {

    private static final int ROWS = 8;

    private static final int COLS = 8;

    private Tile[][] allTiles = new Tile[ROWS][COLS];

    private Tile[] allTilesByPosition = new Tile[ROWS * COLS];

    private Context mContext;

    private View mTile;

    private boolean mIsFlipped;

    private BoardField[][] mBoardField;


    public ChessboardAdapter(Context context, boolean isFlipped, BoardField[][] boardField) {
        mContext = context;
        mIsFlipped = isFlipped;
        mBoardField = boardField;
    }

    public Tile[][] getAllTiles() {
        return allTiles;
    }

    public Tile getTileAt(int row, int col) {
        return allTiles[row][col];
    }

    @Override
    public int getCount() {
        return ROWS * COLS;
    }

    @Override
    public Object getItem(int position) {
        return allTilesByPosition[position];
    }

    @Override
    public long getItemId(int position) {
        return allTilesByPosition[position].getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            mTile = inflater.inflate(R.layout.tile, null);

            Tile tile = (Tile) mTile.findViewById(R.id.single_tile);
            tile.initTile(position, mIsFlipped);
            tile.setChessPiece(mBoardField[tile.getCol()][tile.getRow()].toString());

            allTiles[tile.getRow()][tile.getCol()] = tile;
            allTilesByPosition[position] = tile;
        } else {
            mTile = convertView;
        }
        return mTile;
    }
}
