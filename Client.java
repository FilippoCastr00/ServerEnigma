
// Importazione delle classi necessarie per I/O e networking.
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Properties;
import java.util.Scanner;

// Definizione della classe Client.
public class Client {
    private static boolean isEnigmaOn=false;
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
                    while (in.hasNextLine()) {
                        String message = in.nextLine();
                        try { // Prova a decodificare il messaggio
                            String decryptedMessage = message; // Assume che il messaggio sia già decriptato
                            if (isAESOn) { // Se è attivo l'AES
                                decryptedMessage = cryptoAes.decrypt(message, sharedSecret); // Decifra il messaggio utilizzando AES
                            } else if (isEnigmaOn) { // Altrimenti, se è attivo Enigma
                                decryptedMessage = messaggio.cifraDecifra(message, false); // Decifra il messaggio utilizzando Enigma
                            }
                            System.out.println(decryptedMessage); // Stampa il messaggio decodificato
                        } catch (Exception e) { // Gestisce eventuali eccezioni
                            System.out.println("Ricevuto messaggio non decriptabile"); // Stampa un messaggio di errore
                        }
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
                    continue; // quando la modalità è attiva si chiede all'utente di scrivere i
                                                    // messaggi da criptare
                }
                if (message.equalsIgnoreCase("/enigma off")) {// metodo che disattiva la modalità enigma
                    isAESOn=false;
                    isEnigmaOn = false;
                    System.out.println("modalità enigma disattivata");
                    continue; // quando la modalità è attiva si chiede all'utente di scrivere i
                                                    // messaggi normali
                }
                
                if (message.equalsIgnoreCase("/aes on")) {
                    isAESOn= true;
                    isEnigmaOn=false;
                    System.out.println("modalità aes attiva");
                    continue;
                       
                }else if(message.equalsIgnoreCase("/aes off")){
                    isAESOn = false;
                    isEnigmaOn=false;
                    System.out.println("modalità aes disattivata");
                    continue;
                }
                String messageToSend = username + ": " + message;
                if(isAESOn){
                    try{
                        messageToSend = cryptoAes.encrypt(messageToSend, sharedSecret);
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
                if (isEnigmaOn) {
                    messageToSend = messaggio.cifraDecifra(messageToSend, true);
                }
                if (message.equalsIgnoreCase("exit")) { // Se il messaggio è "exit", interrompe il ciclo.
                    break;
                }
                out.println(messageToSend);
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
