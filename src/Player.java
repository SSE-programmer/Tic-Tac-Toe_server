import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Player {

    private DataOutputStream out;
    private MyDataInputStream in;
    private Socket clientSocket;
    private char playerType;

    private String host;
    private int port;

    public boolean connection = false;

    Player() {
        clientSocket = null;
        in = null;
        out = null;
        playerType = '_';
    }

    void waitConnection(ServerSocket serverSocket) {

        try {
            System.out.println(ConsoleColors.YELLOW_BOLD
                    + "\nОжидание подключения клиента..."
                    + ConsoleColors.RESET);

            clientSocket = serverSocket.accept();
        } catch (IOException e) {
            System.out.println(ConsoleColors.RED_BOLD
                    + "Не удалось принять соединение с клиентом."
                    + ConsoleColors.RESET);
            return;
        }
        System.out.print(ConsoleColors.GREEN_BOLD
                + "Подключение подтверждено.\n"
                + ConsoleColors.RESET);
        connection = true;

        host = clientSocket.getInetAddress().getHostAddress();
        port = clientSocket.getPort();

        /********************Адрес подключенного клиента****************/
        System.out.println(ConsoleColors.GREEN
                + "Host: " + host
                + ConsoleColors.RESET);
        System.out.println(ConsoleColors.GREEN
                + "Port: " + port
                + ConsoleColors.RESET);
        /**************************************************************/

        try {
            in = new MyDataInputStream(clientSocket.getInputStream());
            out = new DataOutputStream(clientSocket.getOutputStream());
        } catch (IOException e) {
            System.out.println(ConsoleColors.RED_BOLD
                    + "Не удалось открыть каналы ввода/вывода для сокета "
                    + clientSocket.getInetAddress().getHostAddress()
                    + "-" + clientSocket.getPort() + "."
                    + ConsoleColors.RESET);
        }
    }

/*
    void waitConnection(ServerSocket serverSocket) throws IOException {
        final ExecutorService clientProcessingPool = Executors.newFixedThreadPool(2);


        Runnable playerConnect = new Runnable() {
            @Override
            public void run() {

                try {
                    clientSocket = serverSocket.accept();

                    System.out.print("\nConnection accepted.\n");
                    connection = true;

                    host = clientSocket.getInetAddress().getHostAddress();
                    port = clientSocket.getPort();

                    //Адрес подключенного клиента
                    System.out.println("Host: " + host);
                    System.out.println("Port: " + port);

                    in = new DataInputStream(clientSocket.getInputStream());
                    out = new DataOutputStream(clientSocket.getOutputStream());
                } catch (IOException e) {
                    System.out.println("Подключение клиента провалено.");
                }
            }
        };

        Thread serverThread = new Thread(playerConnect);
        serverThread.start();
    }
*/

        String readMoveIfActive ( char currentPlayerType){
            String result = null;

            if (playerType != currentPlayerType)
                return null;

            try {
                result = in.myReadUTF();

                System.out.println(ConsoleColors.YELLOW
                        + "Получены данные от клиента: "
                        + ConsoleColors.RESET
                        + result);
            } catch (EOFException e) {
                System.out.println(ConsoleColors.RED_BOLD
                        + "Данные от клиента: " + host + " " + port + " полученны не полностью, проверьте соединение."
                        + ConsoleColors.RESET);
                try {
                    clientSocket.close();
                    connection = false;
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            } catch (UTFDataFormatException e) {
                System.out.println(ConsoleColors.RED_BOLD
                        + "Данные от клиента: " + host + " " + port + " полученны в неверном формате."
                        + ConsoleColors.RESET);
                try {
                    clientSocket.close();
                    connection = false;
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            } catch (IOException e) {
                System.out.println(ConsoleColors.RED_BOLD
                        + "Нет связи с клиентом: " + host + " " + port + "."
                        + ConsoleColors.RESET);
                try {
                    clientSocket.close();
                    connection = false;
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            }

            return result;
        }

        void send (String message) {
            try {
                out.writeUTF(message);
                out.flush();

                System.out.println(ConsoleColors.YELLOW
                        + "Сообщение: "
                        + ConsoleColors.RESET
                        + message
                        + ConsoleColors.YELLOW
                        + " отправленно клиенту " + host + "-" + port + "."
                        + ConsoleColors.RESET);
            } catch (IOException e) {
                System.out.println(ConsoleColors.RED_BOLD
                        + "Не удалось отправить сообщение клиенту " + host + "-" + port + "."
                        + ConsoleColors.RED_BOLD);

                connection = false;
            }
        }

        void setPlayerType ( char type){
            playerType = type;
        }

        char getPlayerType () {
            return playerType;
        }

        public int getPort () {
            return port;
        }

        public String getHost () {
            return host;
        }
    }