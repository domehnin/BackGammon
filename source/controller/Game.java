package controller;


import javax.swing.*;

//import one.MainMenuListener;

import model.Move;
import model.PartialMove;

import view.Board;
import view.BoardPict;
import view.FixedButton;
import view.GUI_Dim;
import view.tablaMouseListener;


import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.util.*; 

public class Game extends JFrame implements ActionListener {
    static final String VERSION = "1.4";
    public static final long serialVersionUID = 1L; // mjr, version 1

    // point colors (player colors are only white & black)
    /* Beware: Board has a duplicate list of these colors which had better stay identical! */
    public static final int neutral = 0;
    public static final int white = 1;
    public static final int black = 2;
    public static final int game_unstarted = 3;

    public static final int LEFT_MARGIN = 20;
    public static final int TOP_MARGIN = 60;

    // Buffers for double buffering
    BufferedImage b_bimage;
    Graphics2D g_buffer;
    public BoardPict myBoardPict = new BoardPict( /* could receive board size param someday! */);

    public Board myBoard = null; // this gets set up in constructor or die
    private int currentPlayer = game_unstarted; /* white; */

    // This contains some booleans about the status of the game
    public Status status = null;

    JTextArea msg_display = null;    // display messages between the players
    JScrollPane msg_scrollpane = null;    // for scrolling messages

    // The buttons the gui uses for various purposes.
    // Bummer: the buttons that show legal available moves on the board
    // are part of this, rather than being part of the Board.
    public FixedButton fButton[] = new FixedButton[9]; /* array of buttons 0..8 */

    public static final int btn_CancelChoice = 0;
    public static final int btn_RollDice = 1;
    public static final int btn_BearOff = 2;
    public static final int btn_AtPotentialMove1 = 3;
    public static final int btn_AtPotentialMove2 = 4;
    public static final int btn_Connect = 5; /* only if networked */
    public static final int btn_SendMessage = 6; /* only if networked */
    public static final int btn_NewGame = 7;
    //public static final int btn_AIMove = 8;

    // Button labels
    static final String CANCEL = "Cancel Choice";
    static final String ROLL_DICE = "Roll Dice";
    static final String BEAR_OFF = "Bear Off";
    static final String MOVE1 = "M1";
    static final String MOVE2 = "M2";
    static final String CONNECT = "Connect";
    static final String SEND_MSG = "Send Message";
    static final String NEW_GAME = "New Game";
    //static final String AI_MOVE = "AI Move";

    static final int GUI_WIDTH = 202;
    /* GUI fits in the game BOARD_HEIGHT, sitting next to board */
    static final int BOARD_PADDING = 120;
    static final int MESSAGE_HEIGHT = 80; /* only when networked */

    /**
     * Game class constructor
     * assumes you're playing against AI.
     * (Use the Game(boolean) constructor if you want to set up network game)
     */
    public Game() {
        this(false /* networkedTF */);  // merely call fancier constructor
    }

    /**
     * Game class constructor
     * Sets title bar, size, shows the window, and does the GUI
     */
    public Game(boolean networkTF /* networked true/false */) {
        setTitle("JBackgammon");
        setResizable(false); /* someday this can be resizable when all dimensions are relative */
        status = new Status();
        myBoard = new Board(this);

        addMouseListener(new tablaMouseListener(this));

        // Call pack() since otherwise getItsets() does not work until the frame is shown
        pack();

        for (int i=0; i < fButton.length; i++) {
            /* create all the buttons */
            fButton[i] = new FixedButton(getContentPane(), this);
        }

        
        setSize(myBoardPict.BOARD_WIDTH + GUI_WIDTH/*632*/
         , myBoardPict.BOARD_HEIGHT + BOARD_PADDING);


        // Set up double buffering
        b_bimage = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
        g_buffer = b_bimage.createGraphics();

        setupGUI();
        setVisible(true); // was the deprecated "show()";
    } // Game( ) constructor

    /**
     * Called by Game(boolean) constructor if we're networking
     */
   

    /**
     * Is called by Board, so can't be private.
     */
    public void debug_msg(String dmsg) {
        /*System.out.println("----------------");
        System.out.println("Breakpoint " +  dmsg);
        System.out.println("status.point_selected = " + status.point_selected + "   old_point = " + old_point);
        System.out.println("currentPlayer = " + currentPlayer + "   Dice = " + getMyBoard( ).myDice);
        System.out.println("potDest1 = " + potDest1 + "   potDest2 = " + potDest2);
        System.out.println("doublet_moves = " + doublet_moves + " doublets = " + getMyBoard().myDice.isDoubles( ));
        // System.out.println("networked = " + status.networked + "  observer = " + status.observer);
        /// System.out.println("Number of black = " + myBoard.getBlack());
        // System.out.println("Number of white = " + myBoard.getWhite());
        System.out.println("----------------");
        System.out.println();*/
    } // debug_msg

    /** 
     * calls "Board.doPartialMove( )" method
     * There is a "getUsedDie( )" (in myBoard().myDice) which says which which dice have been used.
     * (getMyBoard().myDice.isDoubles( ) is true when doubles have been rolled, 
     * and Board.myDice.getDoubletMovesCountdown( ) keeps track of 4 moves countdown)
     * 
     * Note: this switches players by calling game.endTurn( )!
     */
    /*not private for testing */ 
    void doMove(Move myMove) {
        debug_msg("doMove()");
        int howManyPartialsDone = 0;
        ArrayList<PartialMove> myPartials = myMove.getMyPartials( );
        for (PartialMove myPartial : myPartials ) {
            myBoard.doPartialMove( myPartial );
            howManyPartialsDone++;
            if (getMyBoard().myDice.isDoubles( )) {
                myBoard.myDice.doubletCountdown( ); /* -1 */
            }
            myBoard.myDice.setUsedDie( myPartial.getWhichDie( ), true );
            // Turn off focus on this point
            endPartialMove();
            repaint();
        }

        if (!getMyBoard().myDice.isDoubles( )) {
            endTurn(); 
        } else if (getMyBoard().myDice.isDoubles( )) {
            endTurn();
        }

    } // doMove( )

    /**
     * Forfeit the current player's turn.
     * Is called by Board, so can't be private.
     */
    public void forfeitTurn() {
        String msg = "You are stuck, you forfeit your turn.";
        JOptionPane.showMessageDialog(this, msg);
        endTurn();
        repaint();
    } // forfeitTurn( )

    /**
     * Checks if there is a winner
     * If there is one, displays appropriate message.
     * Return true if there was a winner, false otherwise
     */
    public boolean checkWin(int color)    {
        String msg;

        if (color==white) {
            msg = "White wins";
        } else if (color==black) {
            msg = "Black wins";
        } else {
            msg = "You win!";
        }

        if (color==white) {
            if (myBoard.white_bear==Board.howManyBlots) {
                repaint();
                JOptionPane.showMessageDialog(this, msg);
                return true;
            }
        }

        if (color==black) {
            if (myBoard.black_bear==15) {
                repaint();
                JOptionPane.showMessageDialog(this, msg);
                return true;
            }
        }
        return false;
    } // checkWin( )

    /** 
     * Roll the dice for the current player.
     * If current player is on the bar then this calls "myBoard.handleBar( )"
     * If the current player can't move, this calls "Game.forfeitTurn( )"
     */
    public void doRoll() {
        myBoard.myDice.roll(); /* sets a doublet countdown (4 or 2), has method isDoubles( ) which knows the truth */

        // Turn off roll dice button
        fButton[btn_RollDice].setEnabled(false); // roll dice

        repaint();

        if (currentPlayer == game_unstarted) {
            /* Dice 1 is white, Dice 2 is black; high player starts using these 2. 
            In case of tie, roll again until no tie. So a first move can never be doubles!*/
            /* maybe this should wait for the players to roll again? */
            while (myBoard.myDice.isDoubles( ) ) {
                myBoard.myDice.roll( );
                drawCurrentDice( ); //callls repaint();??
            }
            if (myBoard.myDice.getDie(1) > myBoard.myDice.getDie(2)) {
                currentPlayer = white;
            } else {
                currentPlayer = black;
            }
        }
        // Check if the player is on the bar and deal with that right away before player tries to move.
        // Does AI know to hear this handleBar??
        if (myBoard.onBar(currentPlayer)) {
            myBoard.handleBar(currentPlayer);
        } else if ( ! myBoard.canMove(currentPlayer) ) {
            forfeitTurn();
        }
    } // doRoll( )

    /**
     * This could handle more than 2 players with slight modification...
     */
    public void changePlayer() {
        if (currentPlayer == white) {
            currentPlayer = black;
        } else {
            currentPlayer = white;
        }
        repaint();
    } /* changePlayer */

    /**
     * End the current player's turn and start the turn
     * of the other player.
     * Is called by Board, so it can't be private.
     */
    public void endTurn() {
        changePlayer(); 

        // Reset vars, turn off new game button (why??)
        myBoard.myDice.reset();  /* calls resetUsedDice(  ),  sets rolled to false and countdown to 0 */
        fButton[btn_NewGame].setEnabled(true);

        repaint();
        startTurn();
    } // endTurn

    /**
     *  Begins a player's turn
     */
    private void startTurn() {
        // Enable roll dice and new game buttons
        fButton[btn_RollDice].setEnabled(true);
        fButton[btn_NewGame].setEnabled(true);
        
    } // startTurn( )

    /** 
     * This is for Partial Move
     * Remove focus from a sertain point which has been selected
     * This allows the player to select a new point.
     * called by Board, so can't be private.
     * Why does this disable the CancelMove button? Because there's not
     * a tentative parial move to cancel!
     */
    public void endPartialMove() {
        status.point_selected = false;
        // Disable potential move buttons, which ought to be part of board someday
        fButton[btn_AtPotentialMove1].setVisible(false); // potential move 1
        fButton[btn_AtPotentialMove2].setVisible(false); // potential move 2
        fButton[btn_CancelChoice].setEnabled(false); // cancel move
    } // endPartialMove( )

    public Board getMyBoard( ) {
        return myBoard;
    }

    /** 
     * returns int white = 1; black = 2; (shouldn't ever have neutral = 0;)
     */
    public int /*PlayerColor*/ getCurrentPlayer( ) {
        return currentPlayer;
    }

    /**
     * note: beware overlapping duties hardcoded into startTurn( ), endTurn( ), ??
     * Should this acknowledge a change somehow? Shouldn't roll dice, I guess.
     */
    public void setCurrentPlayer(int newPlayerColor ) {
        /* only change if currentPlayer will become different from before */
        if ((Board.legitPlayerColor( newPlayerColor )) && (currentPlayer != newPlayerColor)) {
            currentPlayer = newPlayerColor;
            repaint( );
        }
    } 

    /**
     *  Initialize the GUI
     *   Sets up all the buttons
     */
    public void setupGUI() {
        int left = GUI_Dim.BTN_LEFT_EDGE; /* 475 when board is 430 wide */
        int width = GUI_Dim.BTN_WIDTH; /* 135 */
        int height = GUI_Dim.BTN_HEIGHT; /* 25 */
        fButton[btn_CancelChoice].setBounds(left, 355, width, height);
        fButton[btn_CancelChoice].setVisible(true);
        fButton[btn_CancelChoice].setText(CANCEL);
        fButton[btn_CancelChoice].addActionListener(this);
        fButton[btn_CancelChoice].setEnabled(false);

        fButton[btn_RollDice].setBounds(left, 320, width, height);
        fButton[btn_RollDice].setVisible(true);
        fButton[btn_RollDice].setText(ROLL_DICE);
        fButton[btn_RollDice].addActionListener(this);
        fButton[btn_RollDice].setEnabled(true);

        fButton[btn_BearOff].setBounds(left, 285, width, height);
        fButton[btn_BearOff].setVisible(true);
        fButton[btn_BearOff].setText(BEAR_OFF);
        fButton[btn_BearOff].addActionListener(this);
        fButton[btn_BearOff].setEnabled(false);

        // potential move 1. Are these coords so it is hiding off the right side?
        fButton[btn_AtPotentialMove1].setBounds(650, 490, 9, 10);
        fButton[btn_AtPotentialMove1].setVisible(true);
        fButton[btn_AtPotentialMove1].setText(MOVE1);
        fButton[btn_AtPotentialMove1].addActionListener(this);
        fButton[btn_AtPotentialMove1].setEnabled(true);

        // potential move 2. Are these coords so it is hiding off the right side?
        fButton[btn_AtPotentialMove2].setBounds(750, 490, 9, 10); 
        fButton[btn_AtPotentialMove2].setVisible(true);
        fButton[btn_AtPotentialMove2].setText(MOVE2);
        fButton[btn_AtPotentialMove2].addActionListener(this);
        fButton[btn_AtPotentialMove2].setEnabled(true);

        fButton[btn_NewGame].setBounds(left, 250, width, height);
        fButton[btn_NewGame].setVisible(true);
        fButton[btn_NewGame].setText(NEW_GAME);
        fButton[btn_NewGame].addActionListener(this);
        fButton[btn_NewGame].setEnabled(true);

       
    } // setupGUI

   

 
    public void receiveBar(int point) {
        /* int destPointNum, int howMany, int color/* not merely playerColor*/
        myBoard.setPoint((Board.howManyPoints+1) - point, /*howMany:*/0, /*color:*/neutral);
        myBoard.white_bar++;
        repaint();
    } // receivebar

  
    /*=================================================
     * Overridden Methods 
     * (constructor wants boolean re networked? true/false)
     * ================================================*/

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals(ROLL_DICE)) {
            doRoll();
        } else if (e.getActionCommand().equals(CANCEL)) {
            status.point_selected = false;
            fButton[btn_CancelChoice].setEnabled(false);
            fButton[btn_BearOff].setEnabled(false);
            fButton[btn_AtPotentialMove1].setVisible(false);
            fButton[btn_AtPotentialMove2].setVisible(false);
            repaint();
        } else if (e.getActionCommand().equals(BEAR_OFF)) {
            myBoard.bearOff(currentPlayer);
        } else if (e.getActionCommand().equals(MOVE1)) {
            myBoard.doPartialMove(myBoard.getOldPoint(), /*toPoint:*/myBoard.getPotDest(1), /*whichDie:*/1, currentPlayer);
        } else if (e.getActionCommand().equals(MOVE2)) {
            myBoard.doPartialMove(myBoard.getOldPoint(), /*toPoint:*/myBoard.getPotDest(2), /*whichDie:*/2, currentPlayer);
        } else if (e.getActionCommand().equals(NEW_GAME)) {
                int conf = JOptionPane.showConfirmDialog(this, "Start a new game?", "New Game", JOptionPane.YES_NO_OPTION);
                if ( conf == JOptionPane.YES_OPTION ) {
                    resetGame();
                }
            } // if t/f networked
        } // if newgame
    // actionPerformed( )

    public void paint(Graphics g) {
        // Cast the Graphics to a Graphics2D so actual drawing methods
        // are available
        Graphics2D screen = (Graphics2D) g;
        g_buffer.clearRect(0, 0, getWidth( ), getHeight( ));
        drawBoard( );
        drawBar( );
        drawBlots( );
        drawBearStats( );
        drawPipStats( );
        drawBoardScore( );
        drawCurrentPlayer( );

        if (myBoard.myDice.getRolled()) { // 
            drawCurrentDice( );
        }

        // Blit the buffer onto the screen
        screen.drawImage(b_bimage, null, 0, 0);

        fButton[btn_CancelChoice].repaint();
        fButton[btn_RollDice].repaint();
        fButton[btn_BearOff].repaint();
        fButton[btn_AtPotentialMove1].repaint();
        fButton[btn_AtPotentialMove2].repaint();
        fButton[btn_NewGame].repaint();

        
    } // paint( )

    public static void main(String args[]) {
    	Game app = new Game(false);
    	app.addWindowListener(
                new WindowAdapter()
                {
                    public void windowClosing(WindowEvent e)
                    {
                        System.exit(0);
                    }
                }
            );
           // parent.setVisible(false);
    } // main( )

    /*=================================================
     * Drawing Methods 
     * ================================================*/

    /** 
     * Gets the X coordinate(??) of the specified point (aka "column" or "spike")
     */
    public int findX(int point) {
        if (point <= 6) { /* quadrant one is 1..6 (for white, right?) */
            return LEFT_MARGIN + 401 - (32*(point - 1));
        }
        if (point <= 12)  { /* quadrant two is 7..12 ? */
            return LEFT_MARGIN + 161 - (32*(point - 7));
        }
        if (point <= 18) {
            return LEFT_MARGIN + 1 + (32*(point - 13));
        }
        if (point <= 24) {
        	return LEFT_MARGIN + 241 + (32*(point - 19));
        }
        return -1;
    } // findX( )

    /** 
     * Gets the Y coordinate (??) of the specified point (aka "column" or "spike")
     */
    public int findY(int point) {
        if (point <= 12) { /* points 1..12 are in top half of board */
            return TOP_MARGIN;
        }
        if (point <= 24) { /* points 13..24 are in lower half of board */
            return TOP_MARGIN + 361;
        }
        return -1; // wtf??
    } // findY( )

    /* shouldn't be final if board is resizable */
    final static int left = GUI_Dim.BTN_LEFT_EDGE; /* 475 */
    final static int furtherleft = GUI_Dim.LEFT_EDGE; /* 455 */
    final static int bearTop = GUI_Dim.STATS_TOP_MARGIN; /* 30 */
    final static int textLineHeight = GUI_Dim.TEXT_LINE_HEIGHT; /* 18 */

    /**
     * Announce how many pieces each player beared off so far
     */
    public void drawBearStats() {
        String m1, m2;
        m1 = "White Pieces Beared Off: " + myBoard.white_bear;
        m2 = "Black Pieces Beared Off: " + myBoard.black_bear;

        g_buffer.setColor(Color.BLACK);
        g_buffer.fill(new Rectangle2D.Double(left, bearTop, GUI_Dim.GUI_WIDTH, 2*textLineHeight));

        putString(m1, /*X:*/furtherleft, /*Y:*/bearTop + textLineHeight, Color.WHITE, /*fontsize:*/12);
        putString(m2, /*X:*/furtherleft, /*Y:*/bearTop + 2*textLineHeight, Color.WHITE, /*fontsize:*/12);
    } // drawBearStats( )

    public void drawPipStats() {
        String m1, m2;
        m1 = "White Pip count: " + myBoard.getPipCount( white );
        m2 = "Black Pip count: " + myBoard.getPipCount( black );

        g_buffer.setColor(Color.DARK_GRAY);
        g_buffer.fill(new Rectangle2D.Double(furtherleft, bearTop + 2*textLineHeight
            , GUI_Dim.GUI_WIDTH, 2*textLineHeight));

        putString(m1, /*X:*/furtherleft, /*Y:*/bearTop + 3*textLineHeight, Color.WHITE, /*fontsize:*/12);
        putString(m2, /*X:*/furtherleft, /*Y:*/bearTop + 4*textLineHeight, Color.WHITE, /*fontsize:*/12);
    } // drawPipStats( )

    /**
     * Currently turned off since score comes from one of the strategies now 
     * rather than directly from the board
     */
    public void drawBoardScore() {
        //         String m1, m2;
        //         m1 = "White Board Score: " + myBoard.superMegaHappyScore(myAI.getCautious( ), white );
        //         m2 = "Black Board Score: " + myBoard.superMegaHappyScore(myAI.getCautious( ), black );
        // 
        //         g_buffer.setColor(Color.BLACK);
        //         g_buffer.fill(new Rectangle2D.Double(furtherleft, bearTop + 4*textLineHeight
        //             , GUI_Dim.GUI_WIDTH, 2*textLineHeight));
        // 
        //         putString(m1, /*X:*/furtherleft, /*Y:*/bearTop + 5*textLineHeight, Color.WHITE, /*fontsize:*/12);
        //         putString(m2, /*X:*/furtherleft, /*Y:*/bearTop + 6*textLineHeight, Color.WHITE, /*fontsize:*/12);
    } // drawBoardScore( )

    /**
     * Puts their name on board ("white" "black" "game_unstarted")
     */
    public void drawCurrentPlayer( ) {
        String m1 = "Current Player: " + nameOf(currentPlayer );
        String m2 = "NULL";
        g_buffer.setColor(Color.DARK_GRAY);
        g_buffer.fill(new Rectangle2D.Double(furtherleft, bearTop + 6*textLineHeight
            , GUI_Dim.GUI_WIDTH, 1*textLineHeight));

        putString(m1, /*X:*/furtherleft, /*Y:*/bearTop + 7*textLineHeight, Color.WHITE, /*fontsize:*/12);
        putString(m2, /*X:*/furtherleft, /*Y:*/bearTop + 8*textLineHeight, Color.WHITE, /*fontsize:*/12);
    }

    public String nameOf(int playerColor ) {
        switch (playerColor) {
            case white: return "White";
            case black: return "Black";
            case game_unstarted: return "Game Unstarted";
            default: return "??";
        }
    } 

    /* gets nameOf( )  [currentPlayerName] */

    private void putString(String message, int x, int y, Color c, int fontsize) {
        g_buffer.setFont(new Font("Arial", Font.BOLD, fontsize));
        g_buffer.setColor(c);
        g_buffer.drawString(message, x, y);
    } // putString( )

    /**
     * Driver, organizes data and color before calling general purpose "drawDice"
     * [ ]For formal game start we would want to draw one black die and one white die.
     */
    private void drawCurrentDice( ) {
        int dice1x = GUI_Dim.DICE1_LEFT;
        int dice2x = GUI_Dim.DICE2_LEFT;
        int diceTop = GUI_Dim.DICE_TOP;
        Color diceColor1 = myBoardPict.clr_white;
        Color diceColor2 = myBoardPict.clr_white;
        Color dotColor1 = myBoardPict.clr_black;
        Color dotColor2 = myBoardPict.clr_black;
        if (currentPlayer==black) {
            diceColor1 = diceColor2 = myBoardPict.clr_black;
            dotColor1 = dotColor2 = myBoardPict.clr_white;
            //        } else if (currentPlayer==white) {
            //            diceColor1 = diceColor2 = myBoardPict.clr_white;
            //            dotColor1 = dotColor2 = myBoardPict.clr_black;
        } else if (currentPlayer==game_unstarted) {
            //            diceColor1 = myBoardPict.clr_white;
            //            dotColor1 = myBoardPict.clr_black;
            diceColor2 = myBoardPict.clr_black;
            dotColor2 = myBoardPict.clr_white;
        }
        drawDice(myBoard.myDice.getDie(1), dice1x, diceTop, diceColor1, dotColor1);
        drawDice(myBoard.myDice.getDie(2), dice2x, diceTop, diceColor2, dotColor2);
    } /* drawCurrentDice( ) */

    /**
     * Called by "drawCurrentDice( )"
     */
    private void drawDice(int roll, int x, int y, Color dicecolor, Color dotcolor) {
        int diceSize = GUI_Dim.DICE_SIZE; /* 25 */
        int dotSize = GUI_Dim.DOT_SIZE; /* 4 */
        int leftX = GUI_Dim.DICE_MARGIN; /* 2 */
        int topY = leftX;   /* 2 */
        int midX = (diceSize / 2) - GUI_Dim.DICE_MARGIN; /* 11 */
        int midY = midX;
        int rightX = 2 + (2 * (midX - leftX)); /* trying to evenly space. was 19, 20 ugly, trying 22 */
        int lowY = rightX;

        g_buffer.setColor(dicecolor);
        g_buffer.fill(new Rectangle2D.Double(x, y, diceSize, diceSize ));
        g_buffer.setColor(dotcolor);

        switch(roll) {
            case 1:
            g_buffer.fill(new Rectangle2D.Double(x+midX, y+midY, dotSize, dotSize));
            break;
            case 2:
            g_buffer.fill(new Rectangle2D.Double(x+leftX, y+topY, dotSize, dotSize));
            g_buffer.fill(new Rectangle2D.Double(x+rightX, y+lowY, dotSize, dotSize));
            break;
            case 3:
            g_buffer.fill(new Rectangle2D.Double(x+leftX, y+topY, dotSize, dotSize));
            g_buffer.fill(new Rectangle2D.Double(x+midX, y+midY, dotSize, dotSize));
            g_buffer.fill(new Rectangle2D.Double(x+rightX, y+lowY, dotSize, dotSize));
            break;
            case 4:
            g_buffer.fill(new Rectangle2D.Double(x+leftX, y+topY, dotSize, dotSize));
            g_buffer.fill(new Rectangle2D.Double(x+rightX, y+lowY, dotSize, dotSize));
            g_buffer.fill(new Rectangle2D.Double(x+rightX, y+topY, dotSize, dotSize));
            g_buffer.fill(new Rectangle2D.Double(x+leftX, y+lowY, dotSize, dotSize));
            break;
            case 5:
            g_buffer.fill(new Rectangle2D.Double(x+leftX, y+topY, dotSize, dotSize));
            g_buffer.fill(new Rectangle2D.Double(x+rightX, y+lowY, dotSize, dotSize));
            g_buffer.fill(new Rectangle2D.Double(x+rightX, y+topY, dotSize, dotSize));
            g_buffer.fill(new Rectangle2D.Double(x+leftX, y+lowY, dotSize, dotSize));
            g_buffer.fill(new Rectangle2D.Double(x+midX, y+midY, dotSize, dotSize));
            break;
            case 6:
            g_buffer.fill(new Rectangle2D.Double(x+leftX, y+topY, dotSize, dotSize));
            g_buffer.fill(new Rectangle2D.Double(x+rightX, y+lowY, dotSize, dotSize));
            g_buffer.fill(new Rectangle2D.Double(x+rightX, y+topY, dotSize, dotSize));
            g_buffer.fill(new Rectangle2D.Double(x+leftX, y+lowY, dotSize, dotSize));
            g_buffer.fill(new Rectangle2D.Double(x+leftX, y+midY, dotSize, dotSize));
            g_buffer.fill(new Rectangle2D.Double(x+rightX, y+midY, dotSize, dotSize));
            break;
        }
    } // drawDice( )

    /**
     * drawTriangle: Draws a triangle with the point facing downward, 
     * x,y gives left corner coordinates and a number for color.
     * Hooks: status, g_buffer, old_point 
     */
    private void drawTriangle(int x, int y, int point_color) {
        if (point_color==1) {
            g_buffer.setColor(myBoardPict.color_point_white);
        } else {
            g_buffer.setColor(myBoardPict.color_point_black);
        }

        int [ ] myXs = new int[3]; 
        /* re-written in attempt to fix bluej's "cannot parse" but still "cannot parse" */
        myXs[0] = x; myXs[1] = x + myBoardPict.POINT_WIDTH/2; myXs[2] = x + myBoardPict.POINT_WIDTH;
        int [ ] myYs = new int[3]; //= new int[] { y, y + myBoardPict.POINT_HEIGHT, y};
        myYs[0] = y; myYs[1] = y + myBoardPict.POINT_HEIGHT; myYs[2] = y;

        Polygon tri = new Polygon(myXs, myYs, 3);

        g_buffer.fillPolygon(tri);
        if (status.point_selected) {
            debug_data("TRI: Calling getPointNum",0);
            if (myBoard.getOldPoint( ) == getPointNum(x,y)) {
                g_buffer.setColor(Color.RED);
                debug_data("TRI: old_point = ",myBoard.getOldPoint( ) );
            }
        }
        g_buffer.drawPolygon(tri);
    } // drawTriangle( )

    /**
     * drawTriangleRev: Draws a triangle with the point facing upward,
     * x,y gives left corner coordinates and a number for color.
     * Hooks: status, g_buffer, old_point 
     */
    private void drawTriangleRev(int x, int y, int point_color) {
        if (point_color==neutral) {
            g_buffer.setColor(myBoardPict.color_point_white);
        } else {
            g_buffer.setColor(myBoardPict.color_point_black);
        }

        int [ ] myXs = new int[3]; // new int[] { x, x + myBoardPict.POINT_WIDTH/2, x + myBoardPict.POINT_WIDTH};
        myXs[0] = x; myXs[1] = x + myBoardPict.POINT_WIDTH/2; myXs[2] = x + myBoardPict.POINT_WIDTH;
        int [ ] myYs = new int[3]; // = new int[] { y, y - myBoardPict.POINT_HEIGHT, y};
        myYs[0] = y; myYs[1] = y - myBoardPict.POINT_HEIGHT; myYs[2] = y;

        Polygon tri = new Polygon(myXs, myYs, 3);
        g_buffer.fillPolygon(tri);
        if (status.point_selected) {
            debug_data("DEBUG: drawTriangleRev: Calling getPointNum",0);
            if (myBoard.getOldPoint( ) == getPointNum(x,y)) {
                g_buffer.setColor(Color.RED);
                debug_data("drawTriangleRev: old_point = ", myBoard.getOldPoint( ) );
            }
        }
        g_buffer.drawPolygon(tri);
    } // drawTriangleRev

    /**
     * Draws the Game board onto the buffer
     */
    private void drawBoard() {
        // Set the green color
        g_buffer.setColor(new Color(0 , 150, 0));

        // Draw the two (left & right) halves of the board
        Rectangle2D.Double halfBoardA 
        = new Rectangle2D.Double(LEFT_MARGIN, TOP_MARGIN
            , BoardPict.QUADRANT_WIDTH + 2/*192*/, BoardPict.BOARD_HEIGHT/*360*/);
        Rectangle2D.Double halfBoardB 
        = new Rectangle2D.Double(LEFT_MARGIN+BoardPict.BOARD_MIDPOINT_HORIZONTAL_PIXELS/*238*/
            , TOP_MARGIN, BoardPict.QUADRANT_WIDTH + 2/*192*/, BoardPict.BOARD_HEIGHT/*360*/);

        g_buffer.draw(halfBoardA);
        g_buffer.fill(halfBoardA);
        g_buffer.draw(halfBoardB);
        g_buffer.fill(halfBoardB);

        // Draw the bar
        g_buffer.setColor(new Color(128,64,0)); /* brown? */
        Rectangle2D.Double bar = new Rectangle2D.Double(LEFT_MARGIN+BoardPict.QUADRANT_WIDTH + 2/*192*/, TOP_MARGIN, 
                BoardPict.BAR_WIDTH-4/*46*/, BoardPict.BOARD_HEIGHT/*360*/);
        g_buffer.draw(bar);
        g_buffer.fill(bar);

        g_buffer.setColor(Color.WHITE);
        int point_color = white;

        // Draw the points
        for (int i=0; i<=BoardPict.QUADRANT_WIDTH - 10/*180*/; i+=(BoardPict.POINT_WIDTH+2)/*32*/) {
            if (point_color == neutral) {
                point_color = white;
            } else {
                point_color = neutral;
            }

            drawTriangle(LEFT_MARGIN+i, TOP_MARGIN, point_color);
            drawTriangleRev(LEFT_MARGIN+i, TOP_MARGIN+BoardPict.BOARD_HEIGHT/*360*/, point_color);

            drawTriangle(LEFT_MARGIN+240+i, TOP_MARGIN, point_color);
            drawTriangleRev(LEFT_MARGIN+240+i, TOP_MARGIN+BoardPict.BOARD_HEIGHT/*360*/, point_color);
        }
        debug_data("FINISHED THE SPIKES ",0);
    } // drawBoard( )

    private void drawBar() {
        g_buffer.setColor(new Color(100, 50, 0)); /* dark-brown? */
        int left = LEFT_MARGIN + BoardPict.barRect.x + 2 /*192*/;
        int topBlack = TOP_MARGIN + BoardPict.BAR_BLACK_TOP; /* topmarg + 120 */
        int topWhite = TOP_MARGIN + BoardPict.BAR_WHITE_TOP; /* topmarg + 200 */
        g_buffer.drawRect(left,topBlack/*?*/,BoardPict.BAR_WIDTH - 4,BoardPict.BAR_ZONE_HEIGHT);
        g_buffer.fill(new Rectangle2D.Double(left, topBlack, BoardPict.BAR_WIDTH - 4, BoardPict.BAR_ZONE_HEIGHT));
        g_buffer.fill(new Rectangle2D.Double(left, topWhite, BoardPict.BAR_WIDTH - 4, BoardPict.BAR_ZONE_HEIGHT));

        g_buffer.setColor(Color.WHITE);
        g_buffer.fill(new Rectangle2D.Double(left, (topBlack+topWhite)/2, BoardPict.BAR_WIDTH - 3, BoardPict.BAR_ZONE_HEIGHT));
        left = LEFT_MARGIN + BoardPict.barRect.x + BoardPict.BAR_MARGIN_TO_BLOT; /* 201 */
        int blotSize = BoardPict.BLOT_WIDTH; /* 29 */

        if (myBoard.onBar(black)) {
            g_buffer.setColor(myBoardPict.clr_black);
            g_buffer.fill(new Ellipse2D.Double(left, topBlack + 5, blotSize, blotSize));
            if (myBoard.black_bar > 1) {
                putString(String.valueOf(myBoard.black_bar), /*X:*/left+21, /*Y:*/topBlack + 35, Color.RED, /*fontsize:*/15);
            }
        }

        if (myBoard.onBar(white)) {
            g_buffer.setColor(myBoardPict.clr_white);
            g_buffer.fill(new Ellipse2D.Double(left, topWhite + 5, blotSize, blotSize));
            if (myBoard.white_bar > 1) {
                putString(String.valueOf(myBoard.white_bar), /*X:*/left+21, /*Y:*/topWhite + 35, Color.RED, /*fontsize:*/15);
            }
        }

    } // drawBar( )

    private void drawBlots() {
        debug_msg("drawBlots()");
        int blotSize = BoardPict.BLOT_WIDTH; /* 29 */

        for (int point=1; point<=12; point++) {
            int howManyBlots = myBoard.getHowManyBlotsOnPoint(point);
            if ( (0<howManyBlots) && (howManyBlots<=5) ) {
                for (int i=0; i<howManyBlots; i++) {
                    if (myBoard.getColorOnPoint(point)==white) {
                        g_buffer.setColor(myBoardPict.clr_white);
                    } else {
                        g_buffer.setColor(myBoardPict.clr_black);
                    }
                    g_buffer.fill(new Ellipse2D.Double(findX(point), findY(point) + i*30, blotSize, blotSize));
                }
            }
            if (howManyBlots>5) {
                for (int i=0; i<5; i++) {
                    if (myBoard.getColorOnPoint(point)==white) {
                        g_buffer.setColor(myBoardPict.clr_white);
                    } else {
                        g_buffer.setColor(myBoardPict.clr_black);
                    }
                    g_buffer.fill(new Ellipse2D.Double(findX(point), findY(point) + i*30, blotSize, blotSize));
                }
                putString(String.valueOf(howManyBlots)
                , /*X:*/findX(point)+10, /*Y:*/235, Color.RED, /*fontsize:*/15);
            }
        } // for point 1..12

        for (int point=13; point<=24; point++) {
            int howManyBlots = myBoard.getHowManyBlotsOnPoint(point);
            if ((0<howManyBlots) && (howManyBlots<=5)) {
                for (int i=0; i<howManyBlots; i++) {
                    if (myBoard.getColorOnPoint(point)==white) {
                        g_buffer.setColor(myBoardPict.clr_white);
                    } else {
                        g_buffer.setColor(myBoardPict.clr_black);
                    }
                    g_buffer.fill(new Ellipse2D.Double(findX(point), findY(point) - 30 - i*30, blotSize, blotSize));
                }
            }
            if (howManyBlots>5) {
                for (int i=0; i<5; i++) {
                    if (myBoard.getColorOnPoint(point)==white) {
                        g_buffer.setColor(myBoardPict.clr_white);
                    } else {
                        g_buffer.setColor(myBoardPict.clr_black);
                    }
                    g_buffer.fill(new Ellipse2D.Double(findX(point), findY(point) - 30 - i*30, blotSize, blotSize));
                }
                /* note: findX can return -1 if it doesn't know the point */
                putString(String.valueOf(howManyBlots)
                , /*X:*/findX(point)+10, /*Y:*/255, Color.RED, /*fontsize:*/15);
            }
        } // for point 13..24
    } // drawBlots( )

    /**
     * This probably tells which point is touched by the x,y int coordinates
     */
    public int getPointNum(int point_x, int point_y) {
        boolean leftHalf=true;
        boolean topHalf=true;
        int i=1;

        debug_data("point_x = ",point_x);
        debug_data("point_y = ",point_y);
        // Find which portion of the board the click occurred in
        if (point_y >= BoardPict.BOARD_MIDPOINT_VERTICAL_PIXELS) {
            topHalf = false;
        }

        if (point_x >= BoardPict.BOARD_MIDPOINT_HORIZONTAL_PIXELS) {
            point_x -=BoardPict.BOARD_MIDPOINT_HORIZONTAL_PIXELS;
            debug_data("point_x changed to ", point_x);
            leftHalf = false;
        }
        /* debug_data("half = ", half);
        debug_data("quad = ", quad); */
        // Find how many times we can subtract 32 from the position
        // while remaining positive
        for ( i=1; point_x >= 32; point_x -= 32) {
            i++;
        }

        // Compensate for top/bottom and left/right
        if (topHalf) {
            if (leftHalf) {
                i = (6-i) + 7;
            } else {
                i = (6-i) + 1;
            }
        } else { /* bottom half */
            if (leftHalf)  {
                i += 12;
            } else {
                i += 18;
            }
        }
        // Useful debug statements
        debug_data("getPointNum returns ",i);
        return i;
    } // getPointNum( )

    public void debug_data( String msg, int data) {
        /*
        System.out.print("DEBUG: ");
        System.out.print(msg);
        System.out.println(data);
         */
    } // debug_data( )

    /**
     * Set up a new game
     */
    public void resetGame() {
        // System.out.println("GAME RESET WAS HIT");
        // Reset Game data /
        myBoard.myDice.reset( ); /* puts to unrolled, unused, countdown=0 */ 
        myBoard.setOldPoint( 0 );
        currentPlayer = game_unstarted; /* = white; */

        // Reset buttons
        fButton[btn_CancelChoice].setEnabled(false);
        fButton[btn_RollDice].setEnabled(true); /* was false, why? Was this the reason new game didn't work? */
        fButton[btn_BearOff].setEnabled(false);
        fButton[btn_NewGame].setEnabled(false);
        fButton[btn_AtPotentialMove1].setVisible(false);
        fButton[btn_AtPotentialMove2].setVisible(false);

        // Re-create the board
        myBoard = new Board(this);

        // Have the Status object reset game values
        status.newGame();
        Game app = new Game(false);
    	app.addWindowListener(
                new WindowAdapter()
                {
                    public void windowClosing(WindowEvent e)
                    {
                        System.exit(0);
                    }
                }
            );
        repaint();
    } // resetGame( )
    //class Game
}