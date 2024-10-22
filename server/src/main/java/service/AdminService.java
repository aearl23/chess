package service;

import dataaccess.DataAccess;
import dataaccess.UnauthorizedException;

public class AdminService {
  private final DataAccess dataAccess;

  public AdminService(DataAccess dataAccess) {
    this.dataAccess = dataAccess;
  }
  public void clearApplication() throws UnauthorizedException{
    dataAccess.clear();
  }
}
