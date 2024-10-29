**My Notes** 


- Phase0 implementation: 
  - For chess piece, game, move, board, position: add tostring, equals, haschcode, override methods
- use enums 
- 


10/22
- Yet to implement: Handlers for login and Joingame
- Debug: Testing endpoints, spark error "[qtp2051235196-37] INFO spark.http.matching.MatcherFilter - The requested route [/] has not been mapped in Spark for Accept: [*/*]
- TO_DO: create new expection object, change files. This should fix the wrong code errors
- TO_DO: Ask prof. jensen about static files, email him today, inclass tomorrow

10/23
-Errors: overflow errors are due to serilization of chess piece file which contains chess rule instances. ChessRules, which uses a map, is a nightmare for serialzation. Remove ChessRules from the chesspiece file to pass the tests and consider re-writting chessrules to use other data struct besides a map


10/28
-Requirements for Phase 4: DataBase 
  - Add DB interfaces (MySQL) in DataAccess folder  "MySQLDataAccess"
  - Change server to use MYSQL fiel not memoryaccessdata
  - DataBaseManager creates bridge between DB and server to run commands
  - 