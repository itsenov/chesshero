package com.chesshero.ui.chessboard;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.chesshero.R;
import com.kt.game.Position;

/**
 * Created by Vasil on 6.12.2014 г..
 */
public final class Tile extends ImageView {

    private static final int BLACK_BACKGROUND = R.drawable.transperant_black_cube;

    private static final int WHITE_BACKGROUND = R.drawable.transperant_white_cube;

    private int mCurrentTileImageId;

    private int mCol;

    private int mRow;

    private Position mPosition;

    private boolean mIsFlipped = false;

    private boolean mIsAvailableMove = false;

    public Tile(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void initTile(int position, boolean isFlipped) {
        mIsFlipped = isFlipped;

        //set row, column and position
        setRow(position);
        setCol(position);
        mPosition = Position.positionFromBoardPosition(this.toString());

        //set background
        if ((mCol + mRow) % 2 == 0) {
            setBackgroundResource(WHITE_BACKGROUND);
        } else {
            setBackgroundResource(BLACK_BACKGROUND);
        }

        setScaleX(0.95f);
        setScaleY(0.95f);
    }

    public void setChessPiece(String chessPieceCode) {

        char piece = chessPieceCode.charAt(1);

        // change code letters
        // in order to flip black and white chess pieces
        // because in my implementation element[0][0] is up-left,
        // and on the server element[0][0] is down-left
        if(!mIsFlipped) {
            if (piece > 64 && piece < 91) piece += 32;
            else piece -= 32;
        }

        switch (piece) {
            case '-':
                mCurrentTileImageId = 0;
                break;
            case 'P':
                mCurrentTileImageId = R.drawable.white_pawn;
                break;
            case 'p':
                mCurrentTileImageId = R.drawable.black_pawn;
                break;
            case 'K':
                mCurrentTileImageId = R.drawable.white_king;
                break;
            case 'k':
                mCurrentTileImageId = R.drawable.black_king;
                break;
            case 'B':
                mCurrentTileImageId = R.drawable.white_bishop;
                break;
            case 'b':
                mCurrentTileImageId = R.drawable.black_bishop;
                break;
            case 'R':
                mCurrentTileImageId = R.drawable.white_rook;
                break;
            case 'r':
                mCurrentTileImageId = R.drawable.black_rook;
                break;
            case 'Q':
                mCurrentTileImageId = R.drawable.white_queen;
                break;
            case 'q':
                mCurrentTileImageId = R.drawable.black_queen;
                break;
            case 'N':
                mCurrentTileImageId = R.drawable.white_knight;
                break;
            case 'n':
                mCurrentTileImageId = R.drawable.black_knight;
                break;
        }
        setImageResource(mCurrentTileImageId);

        if (isMine()) {
            mIsAvailableMove = true;
        }
    }

    public int getCol() {
        return mCol;
    }

    public void setCol(int position) {
        mCol = position % 8;
    }

    public int getRow() {
        return mRow;
    }

    public void setRow(int position) {
        mRow = position / 8;
    }

    public Position getPosition() {
        return mPosition;
    }

    public boolean isAvailable() {
        return mIsAvailableMove;
    }

    public void setAvailable(boolean isAvailable) {
        mIsAvailableMove = isAvailable;
    }

    public int getTileImageId() {
        return mCurrentTileImageId;
    }

    public void setTileImageId(int imageId) {
        mCurrentTileImageId = imageId;
        setImageResource(mCurrentTileImageId);
    }

    public boolean isMine() {
        return !mIsFlipped && isWhiteFigure() || mIsFlipped && isBlackFigure();
    }

    public boolean isOponent() {
        return !isEmpty() && !isMine();
    }

    public boolean isEmpty() {
        return mCurrentTileImageId == 0;
    }

    public boolean isBlackFigure() {
        if (isEmpty()) return false;

        switch (mCurrentTileImageId) {
            case R.drawable.black_pawn:
                return true;
            case R.drawable.black_knight:
                return true;
            case R.drawable.black_rook:
                return true;
            case R.drawable.black_bishop:
                return true;
            case R.drawable.black_queen:
                return true;
            case R.drawable.black_king:
                return true;
        }
        return false;
    }

    public boolean isWhiteFigure() {
        if (isEmpty()) return false;

        switch (mCurrentTileImageId) {
            case R.drawable.white_pawn:
                return true;
            case R.drawable.white_knight:
                return true;
            case R.drawable.white_rook:
                return true;
            case R.drawable.white_bishop:
                return true;
            case R.drawable.white_queen:
                return true;
            case R.drawable.white_king:
                return true;
        }
        return false;
    }

    public void applyHighlight() {
        setScaleX(0.80f);
        setScaleY(0.80f);
    }

    public void removeHighlight() {
        setScaleX(0.95f);
        setScaleY(0.95f);
    }

    public String getColumnString() {
        int col = mCol;
        if (mIsFlipped) {
            col = 7 - col;
        }
        switch (col) {
            case 0:
                return "A";
            case 1:
                return "B";
            case 2:
                return "C";
            case 3:
                return "D";
            case 4:
                return "E";
            case 5:
                return "F";
            case 6:
                return "G";
            case 7:
                return "H";
            default:
                return null;
        }
    }

    public String getRowString() {
        // To get the actual mRow, add 1 since 'mRow' is 0 indexed.
        int row = mRow + 1;
        // handle straight case
        if (!mIsFlipped) {
            row = 9 - row;
        }
        return String.valueOf(row);
    }

    public String toString() {
        return getColumnString() + getRowString();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = getMeasuredWidth();
        setMeasuredDimension(width, width);
    }
}