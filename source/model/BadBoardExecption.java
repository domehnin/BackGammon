package model;
/**
 * BadBoardException for squawking about wrong number of pieces on board.
 * 
 */
@SuppressWarnings("serial")
public class BadBoardException extends Exception {
  public BadBoardException() {
  }

  public BadBoardException(String msg) {
    super(msg);
  }
} /* class BadBoardException */