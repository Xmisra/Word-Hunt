import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.sql.SQLException;  
import java.util.concurrent.ConcurrentHashMap;  

public class Server {
    private static final int GRID_SIZE = 4;
    private static final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DICTIONARY_FILE = "dictionary.txt";
    private static final int GAME_DURATION_SECONDS = 120;
    private static final int SERVER_PORT = 12345; 
    private static Set<String> dictionary;
    private static Map<String, Integer> playerScores = new ConcurrentHashMap<>();
    private static Map<String, Set<String>> playerWords = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        dictionary = loadDictionary(DICTIONARY_FILE);
        if (dictionary == null) {
            System.err.println("Failed to load dictionary. Exiting.");
            return;
        }
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            DatabaseHandler.createTablesIfNotExists();
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found. Database features disabled.");
        } catch (Exception e) {
            System.err.println("Database initialization error: " + e.getMessage());
        }
        try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
            System.out.println("Server started. Listening on port " + SERVER_PORT);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            System.err.println("Error in server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket clientSocket;
        private BufferedReader input;
        private PrintWriter output;
        private char[][] grid;
        private String playerName;
        private int score = 0;
        private int timeLeft = GAME_DURATION_SECONDS;
        private ScheduledExecutorService timer;
        private Set<String> usedWords = new HashSet<>();

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
            this.grid = generateGrid();
        }

        @Override
        public void run() {
            try {
                this.input = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
                this.output = new PrintWriter(this.clientSocket.getOutputStream(), true);

                String initialMessage = input.readLine();
                if (initialMessage != null && initialMessage.startsWith("PLAYER_NAME:")) {
                    this.playerName = initialMessage.substring("PLAYER_NAME:".length());
                    try {
                        DatabaseHandler.createPlayerIfNotExists(playerName);
                    } catch (Exception e) {
                        System.err.println("Player creation failed: " + e.getMessage());
                    }

                    System.out.println("Player connected: " + this.playerName);
                    output.println("Welcome, " + this.playerName + "! You have 2 minutes to guess as many words as possible.");
                    output.println("Here is your grid:");
                    output.println(gridToString(grid));

                    startTimer();
                }

                String message;
                while ((message = input.readLine()) != null) {
                    System.out.println("Received from client: " + message);
                    if (message.startsWith("WORD:")) {
                        String word = message.substring("WORD:".length());
                        String upperWord = word.toUpperCase();

                        if (usedWords.contains(upperWord)) {
                            output.println("You've already used this word!");
                        } else {
                            usedWords.add(upperWord);
                            int points = calculatePoints(upperWord);
                            score += points;
                            playerScores.put(playerName, score);

                            if (points > 0) {
                                output.println(word + " is valid! +" + points + " points. Total: " + score);
                            } else {
                                String reason = isValidWord(upperWord) ? " (uses invalid letters)" : " (not in dictionary)";
                                output.println("Invalid word" + reason + ". -1 point. Total score: " + score);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void startTimer() {
            timer = Executors.newSingleThreadScheduledExecutor();
            timer.scheduleAtFixedRate(() -> {
                timeLeft--;
                if (timeLeft >= 0) {
                    output.println(String.format("%d:%02d", timeLeft / 60, timeLeft % 60));
                }
            }, 0, 1, TimeUnit.SECONDS);

            timer.schedule(() -> {
                output.println("Time's up! Game over.");
                output.println("Your final score: " + score);
                declareWinner();
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }, GAME_DURATION_SECONDS, TimeUnit.SECONDS);
        }

        private void declareWinner() {
            String winner = "";
            int maxScore = Integer.MIN_VALUE;
            for (Map.Entry<String, Integer> entry : playerScores.entrySet()) {
                if (entry.getValue() > maxScore) {
                    maxScore = entry.getValue();
                    winner = entry.getKey();
                }
            }
            System.out.println("Game over. Winner: " + winner + " with " + maxScore + " points.");
            try {
                DatabaseHandler.saveGameResult(playerName, score, usedWords);
                DatabaseHandler.updateLeaderboard(playerName, score);
            } catch (Exception e) {
                System.err.println("Failed to save game results: " + e.getMessage());
            }
        }

        private char[][] generateGrid() {
            char[][] grid = new char[GRID_SIZE][GRID_SIZE];
            List<Character> letterList = new ArrayList<>();

            for (char c = 'A'; c <= 'Z'; c++) {
                letterList.add(c);
            }

            Collections.shuffle(letterList);

            int index = 0;
            for (int i = 0; i < GRID_SIZE; i++) {
                for (int j = 0; j < GRID_SIZE; j++) {
                    grid[i][j] = letterList.get(index++);
                }
            }

            return grid;
        }

        private String gridToString(char[][] grid) {
            StringBuilder sb = new StringBuilder();
            for (char[] row : grid) {
                for (char c : row) {
                    sb.append(c).append(" ");
                }
                sb.append("\n");
            }
            return sb.toString();
        }

        private boolean isValidWord(String word) {
            return dictionary.contains(word);
        }

        private boolean isWordFormableFromGrid(String word) {
            Set<Character> gridLetters = new HashSet<>();
            for (int i = 0; i < GRID_SIZE; i++) {
                for (int j = 0; j < GRID_SIZE; j++) {
                    gridLetters.add(grid[i][j]);
                }
            }
            for (char c : word.toCharArray()) {
                if (!gridLetters.contains(Character.toUpperCase(c))) {
                    return false;
                }
            }
            return true;
        }

        private int calculatePoints(String word) {
            String upperWord = word.toUpperCase();
            if (isValidWord(upperWord) && isWordFormableFromGrid(upperWord)) {
                return upperWord.length() >= 5 ? 3 : 1;
            } else {
                return -1;
            }
        }
    }

    private static Set<String> loadDictionary(String filePath) {
        Set<String> dictionary = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                dictionary.add(line.trim().toUpperCase());
            }
            System.out.println("Dictionary loaded successfully.");
            return dictionary;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
