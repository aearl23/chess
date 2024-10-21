package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;

public class AdminService {
  private final DataAccess dataAccess;

  public AdminService(DataAccess dataAccess) {
    this.dataAccess = dataAccess;
  }
  public void clearApplication() throws DataAccessException{
    dataAccess.clear();
  }
}
