public class CifrarioDiCesare {
    // Dizionario utilizzato per il cifrario di Cesare
    private static final String DIZIONARIO = "abcdefghijklmnopqrstuvwxyz";

    // Metodo per criptare il testo
    public static String cripta(String testo, int shift) {
        return trasforma(testo, shift); // Richiama il metodo trasforma con il parametro di shift
    }

    // Metodo per decriptare il testo
    public static String decripta(String testo, int shift) {
        return trasforma(testo, -shift); // Richiama il metodo trasforma con il parametro di shift negativo
    }

    // Metodo che effettua la trasformazione del testo in base al parametro di shift
    public static String trasforma(String testo, int shift) {
        StringBuilder risultato = new StringBuilder(); // Creazione di un oggetto StringBuilder per costruire la stringa
                                                       // risultante

        // Iterazione attraverso ogni carattere del testo
        for (char carattere : testo.toCharArray()) {
            // Verifica se il carattere è presente nel dizionario
            if (DIZIONARIO.indexOf(carattere) != -1) {
                int posizioneOriginale = DIZIONARIO.indexOf(carattere); // Ottiene la posizione originale del carattere
                                                                        // nel dizionario
                int nuovaPosizione = (DIZIONARIO.length() + posizioneOriginale + shift) % DIZIONARIO.length(); // Calcola
                                                                                                               // la
                                                                                                               // nuova
                                                                                                               // posizione
                                                                                                               // applicando
                                                                                                               // lo
                                                                                                               // shift
                risultato.append(DIZIONARIO.charAt(nuovaPosizione)); // Aggiunge il carattere trasformato al risultato
            } else {
                risultato.append(carattere); // Se il carattere non è presente nel dizionario, lo aggiunge al risultato
                                             // senza modificarlo
            }
        }
        return risultato.toString(); // Restituisce il risultato come stringa
    }
}