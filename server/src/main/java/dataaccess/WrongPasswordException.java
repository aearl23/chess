package dataaccess;

public class WrongPasswordException extends DataAccessException {
  public WrongPasswordException(String message) {
    super(message);
  }
}
