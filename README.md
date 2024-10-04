# ♕ BYU CS 240 Chess

This project demonstrates mastery of proper software design, client/server architecture, networking using HTTP and WebSocket, database persistence, unit testing, serialization, and security.

## 10k Architecture Overview

The application implements a multiplayer chess server and a command line chess client.

[![Sequence Diagram](10k-architecture.png)](https://sequencediagram.org/index.html#initialData=C4S2BsFMAIGEAtIGckCh0AcCGAnUBjEbAO2DnBElIEZVs8RCSzYKrgAmO3AorU6AGVIOAG4jUAEyzAsAIyxIYAERnzFkdKgrFIuaKlaUa0ALQA+ISPE4AXNABWAexDFoAcywBbTcLEizS1VZBSVbbVc9HGgnADNYiN19QzZSDkCrfztHFzdPH1Q-Gwzg9TDEqJj4iuSjdmoMopF7LywAaxgvJ3FC6wCLaFLQyHCdSriEseSm6NMBurT7AFcMaWAYOSdcSRTjTka+7NaO6C6emZK1YdHI-Qma6N6ss3nU4Gpl1ZkNrZwdhfeByy9hwyBA7mIT2KAyGGhuSWi9wuc0sAI49nyMG6ElQQA)

## Modules

The application has three modules.

- **Client**: The command line program used to play a game of chess over the network.
- **Server**: The command line program that listens for network requests from the client and manages users and games.
- **Shared**: Code that is used by both the client and the server. This includes the rules of chess and tracking the state of a game.

## Starter Code

As you create your chess application you will move through specific phases of development. This starts with implementing the moves of chess and finishes with sending game moves over the network between your client and server. You will start each phase by copying course provided [starter-code](starter-code/) for that phase into the source code of the project. Do not copy a phases' starter code before you are ready to begin work on that phase.

## IntelliJ Support

Open the project directory in IntelliJ in order to develop, run, and debug your code using an IDE.

## Maven Support

You can use the following commands to build, test, package, and run your code.

| Command                    | Description                                     |
| -------------------------- | ----------------------------------------------- |
| `mvn compile`              | Builds the code                                 |
| `mvn package`              | Run the tests and build an Uber jar file        |
| `mvn package -DskipTests`  | Build an Uber jar file                          |
| `mvn install`              | Installs the packages into the local repository |
| `mvn test`                 | Run all the tests                               |
| `mvn -pl shared test`      | Run all the shared tests                        |
| `mvn -pl client exec:java` | Build and run the client `Main`                 |
| `mvn -pl server exec:java` | Build and run the server `Main`                 |

These commands are configured by the `pom.xml` (Project Object Model) files. There is a POM file in the root of the project, and one in each of the modules. The root POM defines any global dependencies and references the module POM files.

## Running the program using Java

Once you have compiled your project into an uber jar, you can execute it with the following command.

```sh
java -jar client/target/client-jar-with-dependencies.jar

♕ 240 Chess Client: chess.ChessPiece@7852e922




## UML WEB API DIAGRAM LINK

https://sequencediagram.org/index.html?presentationMode=readOnly#initialData=IYYwLg9gTgBAwgGwJYFMB2YBQAHYUxIhK4YwDKKUAbpTngUSWDABLBoAmCtu+hx7ZhWqEUdPo0EwAIsDDAAgiBAoAzqswc5wAEbBVKGBx2ZM6MFACeq3ETQBzGAAYAdAE5M9qBACu2GADEaMBUljAASij2SKoWckgQaIEA7gAWSGBiiKikALQAfOSUNFAAXDAA2gAKAPJkACoAujAA9D4GUAA6aADeAETtlMEAtih9pX0wfQA0U7jqydAc45MzUyjDwEgIK1MAvpjCJTAFrOxclOX9g1AjYxNTs33zqotQyw9rfRtbO58HbE43FgpyOonKUCiMUyUAAFJForFKJEAI4+NRgACUh2KohOhVk8iUKnU5XsKDAAFUOrCbndsYTFMo1Kp8UYdKUAGJITgwamURkwHRhOnAUaYRnElknUG4lTlNA+BAIHEiFRsyXM0kgSFyFD8uE3RkM7RS9Rs4ylBQcDh8jqM1VUPGnTUk1SlHUoPUKHxgVKw4C+1LGiWmrWs06W622n1+h1g9W5U6Ai5lCJQpFQSKqJVYFPAmWFI6XGDXDp3SblVZPQN++oQADW6ErU32mGACCEPnDMEdlEL8GQ5nKACYnE5umWhmKxjAq48prXUvWm2gW6sDigEAYYABRKDeKB9kGnbLDyoAFnHzR6U1G6mA5PGU33h-KeltkLRGMmm+3hgUBBdQ4MJ6mAVdjwHM8MHKCorwAZhvO8WUfe4XwPaByg7YCwnkVdfzMf89wwo9ixPQpoLAWCAFZr1LZCHyfKtX0wmBYQ4NQdWIAhEhgCAADMYEoQ9MQI9AODMThTC8Xx-ACaB2HJGAABkIGiJIAjSDIsiHPIi2KEtqjqJpWgMdQEjQSdRVGWYXjeDgDjItl8xLKdbhndcF2efRXiWTy+gBc4C1lNUUHKBA1J5WFVPU1F0VibEE0MF0wzdMkKQNWlyxnE0iR7SMORgbleQNQVhRgazDFDPK3QHJLylK7ReyStl6pgAA1SgkH4sIqh8+zHWdAlUpZco4F1TJY39KaQ1daUCvKaMYBm7RBsTZMgpLGKeWzXMYF7FzyKKahXIGbLRn8msgxXZs51bAL207chuzdY8k3ySjSjHCdejO6cLru6tF2uxtbvnB6tx3SlgiDaAkAALxQCSnPewccioy8nAARiQvp71UVDnz6Fi0x8GG-ThxHlhgP8dxJyDUc+mAAEgKlopxcfxwnmJI8p2M4qBuIsvjBOE6BRJpySJPE6TvD8QIvBQdAVLU3xmE09JMkwSjWoMtMKmkXdlN3epd2aFozNUCzuiXG60AZwpDqw2HBfh+JEnKAAeW3QbQfJMEO3XQvC1XfQDEHV0SuVkuGmrRpgckwBW+Rw7rX3cqZWqFqKnkYyDMqwh9iC5vNVG2uT4A1pjmQRtJGQtwpFAK9hCuM7NCNCktdNhggGhlvz1aS4jEKnXlZ7wyrgcnfH17A7LvXyjIF6WUersJ5R09dIxipvpvWnDGhpdKaR5q9agrfYKvHHSzxlCmPQt8KvJ1Jj+WffiMPU+TqOpm2bo-oub32JrzNiHFVBcWwDxJIAkhIkQlpuKSMtPByzkpCW0yloQwAAOIzlZBrbS2st5BxKLBLBJtzb2BnDbCO6AHZnCBCWI+rt3ZoC9kXdA-s576WDipaEOD7yp2XOnSeKU4510Ts3dhaA275U7oVYqec4xNXKlI6qmd5oj3BP3JR8gRGx3UXXDBsR+FqFhDIrOcjyiRAsEgPuHYEAJ1wWo9udVo7lBMfGaOU9NppiMWAExqhdpPQDj41x38rhTEofeZ8FR+hRJQAASWkM+LGI54IXieFpTIBoKwTC+DoBAoAGw5I8nkp48SAByM48l7BgI0A4HY16vQ3hRC+lRd70T6PE1QMS4kziSSktJGSphZP1Ode4fR8mFJAMU8ZKwviVOqZM2p9TCJQ2fq-Bmm90aX2xpzO+aFgGPzJkwhGSMxJEXpi0tG54-4c06YAw5JM+ZgIgVAkWsCRJiUQVJZBslAjYB8FAbA3B4ATUMCYlImsdLo2IYZWoDQKFUOfnbScizRiOTPhtBhaZTluygWwmhftphdJnFUgG4MQk4uIVo8aXpMgmMEXbWY6KUBR1ChqWu7oE4UkkUS8x81LE515BXIUhciXOPyposeFc9E1zEdyulepGXxNmlyi0hUlqqsHuq+ePCTFJJEdi1MY1wUmKCcwKlqYwkkM6fEgZ-xV4zxXtcz6O9-6RP6ck8oqT0mS0hoYK5WLtnDjgh62+jEnkgI-DAL88UwAXPWXik+WzWk7LDdfW8EaCZAOeU-ZN1N96ppuTBO5+zI1EzzfzcBgtIHCxgWLKA8CpYttlv8gIlgtwRWSDAAAUhAHk2CZyBAKUUwhsKR7wspCZFo8TqFp1XJOEFwBO1QDgBACKUAWVesxd-ZyPjnYU2YQSmA3siX5FmFm5dq712bpWAAdRYAk02LQABCykFBwAANILK9YMv1gVqXSrCn2gdaBGVSNmP2nk5qUDfgSnKoe6Uk4DxTlIgVpchUKO0cGZR4qF3K0lVnYDi1UOVxaqIgx3LE4qq9Rhju7JyjYe1fIMVjjRiGqHjarRLHyNeJIzAEo3UwjxKEgAD2hKyc4MAQAbugDAGIMAQi-F0KCxDXLyh+C0AymcsJ7XSFmFp5VM41UKo1Zp7A2mIUmdWhR41wJyjQfAzOC1B1Ql6ttU52DOZOxOqXuvYNabQ0dJ6O-INe7GYXzDfcrNjzK3RuAJ+OD8bE0Hw2a7FNrqotX3LTmqNxz0tnMLWswwYFVxf2OJFnZmNEIPIOfFx+2EvQgRgHhW6YWSLFrdezXL3MH6sWrW8+tos4HfOlr8mS8sAheBXYOL0sBgDYBBYQFhUKCE60nfrQ2xtTbm2MHQ6eIBuB4AUEt5AIAWFmKtcFbho8QNHfm6d5bF2oFXYo-o9u5QOLcEmsqRkV2uPZ0iD3OxyoKodFUNu+8swpEaEBwJ-zzT+P2ZLIjlec9NuL2XuaPz2ONBZeq+6mLfQ0fqAhpczrBPbk9ZvnFnmj9Bu1veQ20b-qfkSSAA
```
