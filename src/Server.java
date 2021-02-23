public class Server {

    static void generateTypes(Player[] players) {
        players[0].setPlayerType('o');
        players[1].setPlayerType('x');
    }


    public static ServerEntity server = new ServerEntity();

    public static StringBuffer[] message = new StringBuffer[2];

    public static Player[] players = new Player[2];
    public static GameLogic gameboard;

    private static boolean createServersConnections() {
        for (int i = 0; i < ServersAddresses.Addresses.length; i++) {
            if (server.initialization(i) >= 0)
                break;
        }

        if (!server.isServerInitialized()) {
            System.out.println(ConsoleColors.RED_BOLD
                    + "Невозможно запустить сервер - все доступные порты уже заняты."
                    + ConsoleColors.RESET);

            return false;
        }

        return true;
    }

    private static void newGame() {
        for (int i = 0; i < 2; i++) {
            players[i] = new Player();
            message[i] = null;
        }

        gameboard = new GameLogic();

        generateTypes(players);
    }

    public static void main(String[] args) {


        if (!createServersConnections()) {
            return;
        }

        newGame();

        server.waitConnect();
        server.connectToOtherServers();

        boolean reservFlag = false;

        while(true) {
            do {
                if (!reservFlag) {
                    System.out.println(ConsoleColors.GREEN_BOLD
                            + "Это резервный сервер."
                            + ConsoleColors.RESET);

                    reservFlag = true;
                }

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException exception) {
                    exception.printStackTrace();
                }
            } while (!server.isGeneralServer());


            reservFlag = false;

            System.out.println(ConsoleColors.GREEN_BOLD
                    + "Это главный сервер."
                    + ConsoleColors.RESET);
            boolean successRestore = false;
            for (int i = 0; i < 2; i++) {
                if (players[i].connection == false)
                    players[i].waitConnection(server.getClientsSocket());

                if (message[i] != null) {
                    successRestore = server.restoreGame(message[i].substring(0), gameboard, players);
                }
            }

            if (!successRestore) {
                message[0] = null;
                message[1] = null;
                gameboard = new GameLogic();
            }

            while (players[0].connection && players[1].connection) {
                System.out.println(ConsoleColors.YELLOW_BOLD
                        + "\nИгровое поле на сервере: " + gameboard.toString()
                        + ConsoleColors.RESET);

                char win = gameboard.getWinner();

                for (int i = 0; i < 2; i++) {
                    message[i] = new StringBuffer("");
                    message[i].append(players[i].getPlayerType());
                    message[i].append("/");
                    message[i].append(gameboard.currentMove());
                    message[i].append("/");
                    message[i].append(win);
                    message[i].append("/");
                    message[i].append(gameboard.toString());
                    message[i].append("/");

                    players[i].send(message[i].substring(0));

                    server.sendToAnotherServers(message[i].substring(0));

                    players[i].send("servers/" + server.onlineServersList());
                }

                if (win != '_') {
                    newGame();
                    for (int i = 0; i < 2; i++) {
                        message[i] = new StringBuffer("");
                        message[i].append(players[i].getPlayerType());
                        message[i].append("/");
                        message[i].append(gameboard.currentMove());
                        message[i].append("/");
                        message[i].append("_");
                        message[i].append("/");
                        message[i].append("_________");
                        message[i].append("/");

                        server.sendToAnotherServers(message[i].substring(0));
                    }
                    break;
                }

                String move = null;

                for (int i = 0; i < 2; i++) {
                    String currentMove = players[i].readMoveIfActive(gameboard.currentMove());
                    if (currentMove == null)
                        continue;
                    move = currentMove;
                }

                if (move != null) {
                    if (!gameboard.process(move)) {
                        for (int i = 0; i < 2; i++)
                            players[i].send("error/bad_data");
                    }
                } else {
                    for (int i = 0; i < 2; i++)
                        if (players[i].getPlayerType() != gameboard.currentMove())
                            players[i].send("error/opponent_connection");
                    newGame();
                }
            }
        }
    }
}

