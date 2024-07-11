import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    private ServerSocket server;
    private ExecutorService executor;

    public Server() {
        executor = Executors.newFixedThreadPool(2);

        try {
            server = new ServerSocket(9090, 2);
            System.out.println("Servidor iniciado. Aguardando jogadores...");
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        waitForPlayers();
    }

    private void waitForPlayers() {
        try {
            for (int i = 0; i < 2; i++) {
                Socket socket = server.accept();
                System.out.println("Jogador " + (i + 1) + " conectado.");
                executor.execute(new PlayerHandler(socket, i));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            executor.shutdown();
        }
    }

    private static class PlayerHandler implements Runnable {

        private Socket socket;
        private Scanner input;
        private PrintWriter output;
        private int playerNumber;
        private String playerName;
        private int playerHp;
        private int playerStrength;
        private int roundCounter;
        private double hydrationLevel;

        private static PlayerHandler[] players = new PlayerHandler[2];

        public PlayerHandler(Socket socket, int playerNumber) {
            this.socket = socket;
            this.playerNumber = playerNumber;
            try {
                input = new Scanner(socket.getInputStream());
                output = new PrintWriter(socket.getOutputStream(), true);
            } catch (IOException e) {
                e.printStackTrace();
            }
            players[playerNumber] = this;
        }

        @Override
        public void run() {
            try {
                output.println("Conectado como jogador " + (playerNumber + 1) + ", Digite seu nome:");

                playerName = input.nextLine();
                playerHp = 100;
                playerStrength = 10;
                roundCounter = 0;
                hydrationLevel = 100;

                System.out.println("Jogador " + (playerNumber + 1) + " escolheu o nome: " + playerName);

                while (playerHp > 0) {
                    roundCounter++;
                    output.println("Rodada " + roundCounter + ": O que voce quer fazer? 'avancar' ou 'desistir'");
                    if (input.hasNextLine()) {
                        String message = input.nextLine();
                        System.out.println("Jogador " + (playerNumber + 1) + ": " + message);

                        hydrationLevel -= 10;
                        if (hydrationLevel <= 0) {
                            output.println(playerName + " Você jogou " + roundCounter + " rodadas e" + " morreu de desidratacao.");
                        }

                        if (message.equalsIgnoreCase("avancar")) {
                            int challengeResult = rollDice();
                            if (challengeResult == 1 || challengeResult == 5) {
                                snakeChallenge();
                            } else if (challengeResult == 2) {
                                foundLake();
                            } else if (challengeResult == 3) {
                                banditChallenge();
                            } else if (challengeResult == 4) {
                                handleShinyObjectAlert();
                            }
                        } else if (message.equalsIgnoreCase("desistir")) {
                            output.println("Você escolheu desistir!");
                            break;
                        }

                        if (message.equalsIgnoreCase("sair")) {
                            break;
                        }
                    }
                }
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


        private void handleShinyObjectAlert() {
            output.println(playerName + " Você avistou algo brilhando no horizonte! Deseja 'investigar' ou 'continuar'?");

            if (input.hasNextLine()) {
                String response = input.nextLine().trim().toLowerCase();

                if (response.equals("continuar")) {
                    output.println("Você decidiu continuar e ignorar o objeto brilhante.");
                } else if (response.equals("investigar")) {
                    Random random = new Random();
                    int investigateResult = random.nextInt(10) + 1;

                    if (investigateResult > 5) {
                        foundTreasure();
                    } else {
                        output.println("Você investigou, mas não encontrou nada especial.");
                        extremeHeat();
                    }
                } else {
                    output.println("Opção inválida. Por favor, escolha 'investigar' ou 'continuar'.");
                }
            }
        }

        private void foundTreasure() {
            output.println("Você encontrou um objeto brilhante! Ganhou 20 de hidratacao.");
            hydrationLevel += 20;
        }

        private void extremeHeat() {
            output.println("Você encontrou um calor extremo e perdeu 40 de hidratação!");
            hydrationLevel -= 40;
            output.println("Hidratação atual: " + hydrationLevel);
        }


        private void snakeChallenge() {
            output.println(playerName + " Voce encontrou uma cobra venenosa!");

            int snakeStrength = rollSnakeDice();
            boolean success = determineSuccess(playerStrength, snakeStrength);

            if (success) {
                playerStrength += 2;
                output.println(playerName + " você sobreviveu ao encontro com a cobra e ganhou 2 de força. Força atual: " + playerStrength);
            } else {
                playerHp -= 15;
                System.out.println("perdeu");
                output.println(playerName + " você foi mordido pela cobra e perdeu 5 de HP. HP atual: " + playerHp);
                if (playerHp <= 0) {
                    output.println(playerName + " morreu pela picada da cobra.");
                    output.println("Você jogou " + roundCounter + " rodadas.");
                }
            }
        }

        private void banditChallenge() {
            output.println(playerName + " Voce encontrou uma bandido!");

            int banditStrength = rollBanditDice();
            boolean success = determineSuccess(playerStrength, banditStrength);

            if (success) {
                playerStrength += 5;
                output.println(playerName + " você sobreviveu ao encontro com o bandido e ganhou 5 de força. Força atual: " + playerStrength);
            } else {
                playerHp -= 25;
                output.println(playerName + " você foi ferido pelo bandido e perdeu 15 de HP. HP atual: " + playerHp);
                if (playerHp <= 0) {
                    output.println(playerName + " morreu pela investida do bandido.");
                    output.println("Você jogou " + roundCounter + " rodadas.");
                }
            }
        }

        private void foundLake() {
            output.println(playerName + " você encontrou um lago e ganhou 15 de hidratação!");
            hydrationLevel += 15;
            output.println("Hidratação atual: " + hydrationLevel);
        }

        private void foundKnife() {
            output.println(playerName + " você encontrou uma faca! Ganhou 5 de força.");
            playerStrength += 5;
            output.println("Força atual: " + playerStrength);
        }

        private static boolean determineSuccess(int playerStrength, int enemyStrength) {
            Random random = new Random();
            int playerRoll = random.nextInt(playerStrength + 1);
            int enemyRoll = random.nextInt(enemyStrength + 1);
            return playerRoll >= enemyRoll;
        }

        private static int rollDice() {
            Random random = new Random();
            return random.nextInt(10) + 1;
        }

        private static int rollBanditDice() {
            Random random = new Random();
            return random.nextInt(16) + 13;
        }

        private static int rollSnakeDice() {
            Random random = new Random();
            return random.nextInt(11) + 8;
        }

    }

}
