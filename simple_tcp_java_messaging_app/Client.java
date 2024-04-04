
// Importazione delle classi necessarie per I/O e networking.
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

// Definizione della classe Client.
public class Client {
    private static boolean isEnigmaOn;
    private static boolean isCesarOn;

    // Metodo main, punto di ingresso dell'applicazione client.
    public static void main(String[] args) {
        // Controlla se sono stati passati esattamente tre argomenti (IP server, porta,
        // username).
        if (args.length != 3) {
            // Se non sono presenti esattamente tre argomenti, stampa un messaggio di errore
            // e termina.
            System.err.println("Usage: java Client <server-ip> <port> <username>");
            System.exit(1);
        }

        // Estrazione degli argomenti: IP del server, porta e username.
        String serverIp = args[0]; // IP del server.
        int port = Integer.parseInt(args[1]); // Porta di connessione al server, convertita in un intero.
        String username = args[2]; // Username dell'utente.
        // Tentativo di stabilire una connessione al server e di configurare gli stream
        // di input/output.

        try (Socket socket = new Socket(serverIp, port); // Crea un socket per connettersi al server.
                Scanner userInput = new Scanner(System.in); // Scanner per leggere l'input dell'utente da console.
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) { // PrintWriter per inviare messaggi
                                                                                     // al server, con auto-flush
                                                                                     // attivato.

            // Messaggio che indica la connessione riuscita al server.
            System.out.println("Connected to server. Start typing messages (type 'exit' to quit).");
            Enigma messaggio = new Enigma();
            CifrarioDiCesare messaggioCesar = new CifrarioDiCesare();

            // Creazione e avvio di un nuovo thread per ascoltare i messaggi dal server.
            Thread serverListener = new Thread(() -> {
                try (Scanner in = new Scanner(socket.getInputStream())) { // Scanner per leggere i messaggi in entrata
                                                                          // dal server.
                    while (in.hasNextLine()) { // Continua a leggere finché ci sono messaggi.
                        System.out.println(in.nextLine()); // Stampa i messaggi ricevuti dal server.
                    }
                } catch (IOException e) { // Cattura eccezioni di I/O.
                    e.printStackTrace(); // Stampa lo stack trace dell'eccezione.
                }
            });
            serverListener.start(); // Avvia il thread che ascolta i messaggi dal server.
            String message = "";
            // Ciclo principale per l'invio di messaggi al server.
            while (true) {
                try {// gestisce l'eccezione nel caso in cui l'utente non scrive
                    message = userInput.nextLine(); // Legge un messaggio da console.
                    if (message.isEmpty())
                        throw new NullPointerException();
                } catch (NullPointerException e) {
                    System.err.println("devi inserire qualcosa, non può essere nullo ");
                }
                if (message.equalsIgnoreCase("/cesar on")) {
                    isCesarOn = true;
                    System.out.println("modalità cesar attiva");
                    message = userInput.nextLine();
                }
                if (message.equalsIgnoreCase("/enigma on")) {// metodo che attiva la modalità enigma
                    isEnigmaOn = true;
                    System.out.println("modalità enigma attiva");
                    message = userInput.nextLine(); // quando la modalità è attiva si chiede all'utente di scrivere i
                                                    // messaggi da criptare
                }
                if (message.equalsIgnoreCase("/cesar off")) {
                    isCesarOn = false;
                    System.out.println("modalità cesar disattivata");
                    message = userInput.nextLine();
                }
                if (message.equalsIgnoreCase("/enigma off")) {// metodo che disattiva la modalità enigma
                    isEnigmaOn = false;
                    System.out.println("modalità enigma disattivata");
                    message = userInput.nextLine(); // quando la modalità è attiva si chiede all'utente di scrivere i
                                                    // messaggi normali
                }
                if (isCesarOn) {
                    String messaggioCriptato1 = messaggioCesar.cripta(message, 3);
                    out.println(username + ": " + messaggioCriptato1);
                } else if (isEnigmaOn) {
                    String messaggioCriptato = messaggio.cifraDecifra(message, true);
                    out.println(username + ": " + messaggioCriptato);
                } else if (!isEnigmaOn && !isEnigmaOn) {
                    out.println(username + ": " + message);
                } else if (message.equalsIgnoreCase("exit")) { // Se il messaggio è "exit", interrompe il ciclo.
                    break;
                }
            }

        } catch (IOException e) { // Cattura eccezioni di I/O.
            e.printStackTrace(); // Stampa lo stack trace dell'eccezione.
        }
    }
}
