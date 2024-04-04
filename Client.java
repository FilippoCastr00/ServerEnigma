
// Importazione delle classi necessarie per I/O e networking.
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Properties;
import java.util.Scanner;

// Definizione della classe Client.
public class Client {
    private static boolean isEnigmaOn;
    private static boolean isAESOn=false;
    private static String sharedSecret = getPSK("app.properties");

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
            CrittografiaAES cryptoAes = new CrittografiaAES();

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
                try{// gestisce l'eccezione nel caso in cui l'utente non scrive
                    message = userInput.nextLine(); // Legge un messaggio da console.
                    if (message.isEmpty())throw new NullPointerException();
                }catch(NullPointerException e){
                    System.err.println("devi inserire qualcosa, non può essere nullo ");
                }
                if (message.equalsIgnoreCase("/enigma on")) {// metodo che attiva la modalità enigma
                    isAESOn=false;
                    isEnigmaOn = true;
                    System.out.println("modalità enigma attiva");
                    message = userInput.nextLine(); // quando la modalità è attiva si chiede all'utente di scrivere i
                                                    // messaggi da criptare
                }
                if (message.equalsIgnoreCase("/enigma off")) {// metodo che disattiva la modalità enigma
                    isAESOn=false;
                    isEnigmaOn = false;
                    System.out.println("modalità enigma disattivata");
                    message = userInput.nextLine(); // quando la modalità è attiva si chiede all'utente di scrivere i
                                                    // messaggi normali
                }
                if (isEnigmaOn) {
                    String messaggioCriptato = messaggio.cifraDecifra(message, true);
                    out.println(username + ": " + messaggioCriptato);
                }
                if (message.equalsIgnoreCase("/aes on")) {
                    isAESOn= true;
                    isEnigmaOn=false;
                    System.out.println("modalità aes attiva");
                    message = userInput.nextLine();
                       
                }else if(message.equalsIgnoreCase("/aes off")){
                    isAESOn = false;
                    isEnigmaOn=false;
                    System.out.println("modalità aes disattivata");
                    message = userInput.nextLine();
                }
                if(isAESOn){
                    try{
                        String messaggioAes = cryptoAes.encrypt(message, sharedSecret);
                        out.println(username + ": " + messaggioAes);
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
                if(!isAESOn && !isEnigmaOn) {
                    out.println(username + ": " + message);
                }
                if (message.equalsIgnoreCase("exit")) { // Se il messaggio è "exit", interrompe il ciclo.
                    break;
                }
            }

        } catch (IOException e) { // Cattura eccezioni di I/O.
            e.printStackTrace(); // Stampa lo stack trace dell'eccezione.
        }
    }
    private static String getPSK(String filename) { // Metodo privato per ottenere la chiave condivisa dal file di configurazione
        Properties prop = new Properties(); // Crea un nuovo oggetto Properties per gestire le proprietà
        try (FileInputStream fis = new FileInputStream(filename)) { // Apre un file di input stream per leggere le proprietà
            prop.load(fis); // Carica le proprietà dal file
            return prop.getProperty("sharedSecret"); // Restituisce il valore della chiave condivisa dal file di configurazione
        } catch (IOException e) { // Gestisce eventuali eccezioni di IO
            e.printStackTrace(); // Stampa lo stack trace dell'eccezione
            return null; // Restituisce null in caso di errore
        }
    }
}
