package dataaccess;
public class InvalidUsernameException extends DataAccessException {
  public InvalidUsernameException(String message) {
    super(message);
  }
}

