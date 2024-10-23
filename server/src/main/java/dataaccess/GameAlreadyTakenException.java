package dataaccess;

public class GameAlreadyTakenException extends DataAccessException {
  public GameAlreadyTakenException(String message) {
    super(message);
  }
}
