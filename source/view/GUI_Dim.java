package view;
import java.awt.*;

/**
 * 	Setting the GUI Dimension
 * 
 * @version 2013 Jun 26
 */
public class GUI_Dim
{
    final static int MARGIN = 25; // space between playing board and command gui
    public final static int LEFT_EDGE = view.BoardPict.BOARD_RIGHT_EDGE + MARGIN; /* 430 + 25 */
    public final static int BTN_LEFT_EDGE = LEFT_EDGE + 20; /* 475. should be resizeable someday */
    public final static int BTN_WIDTH = 135;
    public final static int BTN_HEIGHT = 25;
    public final static int STATS_TOP_MARGIN = 30; /* 30 */
    public final static int TEXT_LINE_HEIGHT = 18; /* 18 */

    
    public final static int GUI_WIDTH = 150; /* ? */
    public final static int GUI_HEIGHT = BoardPict.BOARD_HEIGHT; /* 360 */
    
        // Dice pictures
    public static final int DOT_SIZE = 4;
    public static final int DICE_SIZE = 25;
    public static final int DICE_MARGIN = 2; /* how close the dots are to the edge of the dice face */

    public final static int DICE1_LEFT = LEFT_EDGE + 24; /* 479 */
    public final static int DICE2_LEFT = DICE1_LEFT + 50;
    public final static int DICE_TOP = 200;
    
    /* triangle constructor receives left,top,width,height */
    public final static Rectangle guiRect = new Rectangle(LEFT_EDGE, BoardPict.BOARD_TOP_EDGE, GUI_WIDTH, GUI_HEIGHT);

    
} /* class GUI_Dim */
