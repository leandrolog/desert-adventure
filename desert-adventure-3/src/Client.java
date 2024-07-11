import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    private Socket socket;
    private Scanner consoleInput;
    private Scanner serverInput;
    private PrintWriter output;
    private BufferedReader input;

    public Client(String host) {
        try {
            socket = new Socket(InetAddress.getByName(host), 9090);
            consoleInput = new Scanner(System.in);
            serverInput = new Scanner(socket.getInputStream());
            output = new PrintWriter(socket.getOutputStream(), true);
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            System.out.println("Conectado ao servidor.");
            startInteraction();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            consoleInput.close();
            serverInput.close();
            output.close();
        }
    }

    public void startInteraction() {
        try {
            while (true) {
                if (serverInput.hasNextLine()) {
                    String serverResponse = serverInput.nextLine();
                    System.out.println("Servidor: " + serverResponse);

                    if (serverResponse.contains("morreu de desidratacao.")) {
                        System.out.println(serverResponse);
                        break;
                    }

                    String[] palavras = serverResponse.split("\\s+");
                    String lastWord = palavras[palavras.length - 1];
                    System.out.println(lastWord);

                    if (serverResponse.contains("lago")) {
                        foundLakeResponse();
                    } else if (lastWord.endsWith("venenosa!")) {
                        handleSnakeResponse();
                    } else if (lastWord.endsWith("bandido!")) {
                        handleBanditResponse();
                    } else if (serverResponse.contains("'continuar'?")) {
                        handleShinyObjectAlertResponse();
                    }
                }

                if (consoleInput.hasNextLine()) {
                    String message = consoleInput.nextLine();
                    output.println(message);


                    if (message.equalsIgnoreCase("sair")) {
                        break;
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleShinyObjectAlertResponse() throws IOException {
        System.out.println("Você avistou algo brilhando no horizonte! Deseja 'investigar' ou 'continuar'?");

        String userInput = consoleInput.nextLine();
        output.println(userInput);

        if (userInput.equalsIgnoreCase("continuar")) {
            System.out.println("Você decidiu continuar e ignorar o objeto brilhante.");
        } else if (userInput.equalsIgnoreCase("investigar")) {
            waitForServerResponse();
        } else {
            System.out.println("Opção inválida. Por favor, escolha 'investigar' ou 'continuar'.");
        }
    }

    private void waitForServerResponse() throws IOException {
        String serverResponse = input.readLine();

        if (serverResponse.contains("encontrou nada especial.")) {
            String hydrationResult = input.readLine();
            System.out.println("Servidor: " + hydrationResult);
        } else if (serverResponse.contains("Ganhou 20 de hidratação.")) {
            String heatResult = input.readLine();
            System.out.println("Servidor: " + heatResult);
        }
    }


    private void foundLakeResponse() {
        if (serverInput.hasNextLine()) {
            String lakeResult = serverInput.nextLine();
            System.out.println("Servidor: " + lakeResult);

            if (lakeResult.contains("Hidratação")) {
                if (serverInput.hasNextLine()) {
                    String hydrationResult = serverInput.nextLine();
                    System.out.println("Servidor: " + hydrationResult);
                }
            }
        }
    }


    private void handleSnakeResponse() {
        if (serverInput.hasNextLine()) {
            String snakeResult = serverInput.nextLine();
            System.out.println("Servidor: " + snakeResult);

            if (snakeResult.contains("HP.")) {
                if (serverInput.hasNextLine()) {
                    String hpResult = serverInput.nextLine();
                    System.out.println("Servidor: " + hpResult);
                }
            }
        }
    }

    private void handleBanditResponse() {
        if (serverInput.hasNextLine()) {
            String banditResult = serverInput.nextLine();
            System.out.println("Servidor: " + banditResult);

            if (banditResult.contains("HP.")) {
                if (serverInput.hasNextLine()) {
                    String hpResult = serverInput.nextLine();
                    System.out.println("Servidor: " + hpResult);
                }
            }
        }
    }
}
