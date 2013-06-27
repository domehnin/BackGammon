package model;
/**
 * BadBoardException for squawking about wrong number of partialMoves in a Move, etc.
 * 
 */
public class BadMoveException extends Exception {
  public BadMoveException() {
  }

  public BadMoveException(String msg) {
    super(msg);
  }
} /* class BadMoveException */