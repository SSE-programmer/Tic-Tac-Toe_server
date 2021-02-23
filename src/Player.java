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
            System.out.println("Ожидание подключения клиента.");
            clientSocket = serverSocket.accept();
        } catch (IOException e) {
            System.out.println("Не удалось принять соединение с клиентом.");
            return;
        }
        System.out.print("\nПодключение подтверждено.\n");
        connection = true;

        host = clientSocket.getInetAddress().getHostAddress();
        port = clientSocket.getPort();

        /********************Адрес подключенного клиента****************/
        System.out.println("Host: " + host);
        System.out.println("Port: " + port);
        /**************************************************************/

        try {
            in = new MyDataInputStream(clientSocket.getInputStream());
            out = new DataOutputStream(clientSocket.getOutputStream());
        } catch (IOException e) {
            System.out.println("Не удалось открыть каналы ввода/вывода для сокета "
                    + clientSocket.getInetAddress().getHostAddress()
                    + "-" + clientSocket.getPort() + ".");
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

                System.out.println("Получены данные от клиента: " + result);
            } catch (EOFException e) {
                System.out.println("Данные от клиента: " + host + " " + port + " полученны не полностью, проверьте соединение.");
                try {
                    clientSocket.close();
                    connection = false;
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            } catch (UTFDataFormatException e) {
                System.out.println("Данные от клиента: " + host + " " + port + " полученны в неверном формате.");
                try {
                    clientSocket.close();
                    connection = false;
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            } catch (IOException e) {
                System.out.println("Нет связи с клиентом: " + host + " " + port + ".");
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

                System.out.println("Сообщение: " + message + " отправленно клиенту " + host + "-" + port + ".");
            } catch (IOException e) {
                System.out.println("Не удалось отправить сообщение клиенту " + host + "-" + port + ".");

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