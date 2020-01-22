package tp.server;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private ServerSocket serverSocket;
    private Socket[] clientSockets;
    private PrintWriter[] out;
    private BufferedReader[] in;
    private GameLogic game = new GameLogic();
    private int gameID = 0;
    private int moveNumber = 0;
    private boolean load = false;
    private ArrayList<String> moves;
    private HibernateUtil hu;

    public Server(int port) {
        try {

            serverSocket = new ServerSocket(port);
            Socket startingClient = serverSocket.accept();
            BufferedReader startReader = new BufferedReader(new InputStreamReader(startingClient.getInputStream()));
            PrintWriter startWriter = new PrintWriter(startingClient.getOutputStream(), true);
            String line = startReader.readLine();

            moves = new ArrayList<String>();
            if (line.matches("START [12]")) {
                switch (line.charAt(6)) {
                    case '1':
                        clientSockets = new Socket[1];
                        out = new PrintWriter[1];
                        in = new BufferedReader[1];
                        clientSockets[0] = startingClient;
                        out[0] = startWriter;
                        in[0] = startReader;

                        startWriter.println("READY B");
                        break;
                    case '2':
                        clientSockets = new Socket[2];
                        out = new PrintWriter[2];
                        in = new BufferedReader[2];
                        clientSockets[0] = startingClient;
                        out[0] = startWriter;
                        in[0] = startReader;

                        startWriter.println("WAITING");
                        clientSockets[1] = serverSocket.accept();
                        out[1] = new PrintWriter(clientSockets[1].getOutputStream(), true);
                        in[1] = new BufferedReader(new InputStreamReader(clientSockets[1].getInputStream()));
                        in[1].readLine();

                        out[0].println("READY B");
                        out[1].println("READY W");

                        break;
                    default:
                        System.out.println("SWITCH ERROR");
                        break;
                }
            } else if (line.startsWith("LOAD ")) {
                clientSockets = new Socket[1];
                out = new PrintWriter[1];
                in = new BufferedReader[1];
                clientSockets[0] = startingClient;
                out[0] = startWriter;
                in[0] = startReader;

                while (!load) {
                    String[] command = line.split(" ");
                    try {
                        gameID = Integer.parseInt(command[1]);
                        load = true;
                        startWriter.println("OK");
                    } catch (Exception e) {
                        startWriter.println("BAD ID");
                        line = startReader.readLine();
                    }
                }
            } else startWriter.println("NO");
        } catch (IOException e) {
            System.out.println("IO ERROR");
            System.exit(1);
        }

    }

    boolean isLoaded() {
        return load;
    }

    void gameCourse() throws IOException {
        boolean pauseFlag = false;
        String line;
        String clientLine;

        while (true) {
            for (int i = 0; i < out.length; i++) {
                line = "MOVE";
                while (line.equals("MOVE")) {
                    out[i].println(line);
                    clientLine = in[i].readLine();
                    if (clientLine.matches("PAUSE [BW]")) {
                        if (pauseFlag || (in.length == 1 && clientLine.endsWith("B"))) {
                            line = game.endGame();
                            updateClients(line);
                            return;
                        } else pauseFlag = true;
                        line = clientLine;
                    } else if (clientLine.matches("SURRENDER [BW]")) {
                        line = clientLine;
                        updateClients(line);
                        return;
                    } else line = game.move(clientLine);
                }
                updateClients(line);
                for (String msg : game.getRemoved()) updateClients(msg);
                if (!line.startsWith("PAUSE ")) pauseFlag = false;
                updateClients("EOF");
            }

            if (out.length == 1) {
                updateClients(game.getBotMove());
                for (String msg : game.getRemoved()) {
                    updateClients(msg);
                }
                updateClients("EOF");
            }
        }
    }

    void gameEnd() throws IOException {
        for (int i = 0; i < in.length; i++) {
            in[i].close();
            out[i].close();
            clientSockets[i].close();
        }
        serverSocket.close();
    }

    public void save() {
        Session s = HibernateUtil.getSessionFactory().openSession();
        gameID = (Integer)(s.createQuery("SELECT DISTINCT gameId FROM Move ORDER BY gameId DESC").list().get(0)) + 1;
        s.close();

        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        for(String move : moves){
            session.save(new Move(gameID,moves.indexOf(move),move));
        }
        session.getTransaction().commit();

        session.close();
    }

    void loadGame() throws InterruptedException, IOException {
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        Session session = sessionFactory.openSession();
        List<String> rows = session.createSQLQuery("SELECT message FROM move WHERE gameId="+gameID+" ORDER BY moveNumber ASC").list();
        updateClients(rows.size()+" LINES");
        for( String  string: rows){
            updateClients(string);
        }
    }

    void updateClients(String line) {
        for (PrintWriter out : out) out.println(line);
        if (!line.equals("EOF")) {
            moves.add(line);
            moveNumber++;
        }
    }

    public static void main(String[] args) {
        try {
            Server s = new Server(9100);
            if (s.isLoaded()) s.loadGame();
            else {
                s.gameCourse();
                s.save();
            }
            s.gameEnd();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }


}
