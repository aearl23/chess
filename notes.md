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
-TODO Requirements for Phase 4: DataBase 
  - Add DB interfaces (MySQL) in DataAccess folder  "MySQLDataAccess"
  - Change server to use MYSQL file not memoryaccessdata
  - DataBaseManager creates bridge between DB and server to run commands
  - Install the MYSQL database management system (DBMS) on your development machine. Modify db.properties to contain your username and password.
  - Design your database tables (i.e., your database schema)
  - Implement a MySQL implementation of your Data Access Interface. Initially you can just stub out all of the methods.
  - Add the ability to create your database and tables, if they don't exist, when your server starts up.
  - Iteratively write a test for each of your Data Access interface methods along with the backing MySQL code.
  - Ensure that all provided pass off tests work properly, including the PersistenceTests added for this assignment, and the StandardAPITests from the previous assignment.