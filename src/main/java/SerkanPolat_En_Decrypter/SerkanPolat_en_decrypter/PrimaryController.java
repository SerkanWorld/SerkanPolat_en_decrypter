package SerkanPolat_En_Decrypter.SerkanPolat_en_decrypter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

public class PrimaryController {
	
	
	
	@FXML
	private TextField pathInput;
	
	@FXML
	private StackPane root;

	@FXML
	private Rectangle dragndrop;
	    
	@FXML
    private TextField passwordInput;
    
    @FXML
    private Label regEx;
    
    @FXML
    private Button start;
    
    @FXML
    private Label pathOutput;
 
    
    
    public void initialize() {
        // Set up drag and drop handling for the Rectangle
        dragndrop.setOnDragOver(event -> {
            if (event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });

        dragndrop.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            // Wenn Drag-and-Drop-Objekt Dateien enthält, wird die erste Datei in das Pfad-Eingabefeld eingefügt
            if (db.hasFiles()) {
                File file = db.getFiles().get(0);
                pathInput.setText(file.getAbsolutePath());
                success = true;
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }
    
   
    
    public void fileDecrypt() throws Exception {
    	try {
    		// Holt den Salz-Wert aus den ersten 16 Bytes des verschlüsselten Inhalts
            byte[] encryptedContent = Files.readAllBytes(Paths.get(path));

            byte[] salt = Arrays.copyOfRange(encryptedContent, 0, 16);

            // Legt die Anzahl der Iterationen und die Schlüssellänge fest, um den geheimen Schlüssel aus dem Passwort zu generieren
            int iterations = 10000;
            int keyLength = 128;
            KeySpec spec = new PBEKeySpec(passwordInput.getText().toCharArray(), salt, iterations, keyLength);
            
            // Erstellt einen geheimen Schlüssel anhand des Passworts mit Hilfe des PBKDF2-Algorithmus
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] keyBytes = factory.generateSecret(spec).getEncoded();
            secretKey = new SecretKeySpec(keyBytes, "AES");

            
            // Initialisiert den AES-Cipher im Entschlüsselungsmodus
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            
            // Entschlüsselt den Inhalt der Datei, beginnend ab Byte 16
            byte[] decryptedContent = cipher.doFinal(encryptedContent, 16, encryptedContent.length - 16);

            
            // Schreibt den entschlüsselten Inhalt in die Datei
            FileOutputStream outputStream = new FileOutputStream(path);
            outputStream.write(decryptedContent);
            outputStream.close();

            // Setzt die Erfolgsmeldung und den Ausgabepfad
            regEx.setText("Passwort is right");
            pathOutput.setText("save encrypted data here " + path);
    	}catch(Exception e){
    		
    		// Setzt die Fehlermeldung bei einem falschen Passwort
    		regEx.setText("Password wrong");
    	}
        
    }
    
    String path;
    SecretKey secretKey;
    String passwordMuster = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$";

    @FXML
    private void confirm() throws IOException {
    	// Speichere den Dateipfad des Eingabefeldes
    	path = pathInput.getText();
    	// Eingabefeld für das Passwort wird editierbar
        passwordInput.setEditable(true);
    }
    
    public void fileEncrypt() throws Exception {
    	
        String password = passwordInput.getText();
        
        // Überprüfe, ob das Passwort den Anforderungen entspricht
        boolean isStrongPassword = password.matches(passwordMuster);
        
        // Zeige die Passwortstärke im Label an
        regEx.setText(isStrongPassword ? "Password is strong enough." : "Password is not strong enough.");
        
        if (isStrongPassword) {
	        SecureRandom random = new SecureRandom();
	        byte[] salt = new byte[16];
	        random.nextBytes(salt);
	
	        int iterations = 10000;
	        int keyLength = 128;
	        
	        // Erstelle einen Schlüssel aus dem Passwort
	        KeySpec spec = new PBEKeySpec(passwordInput.getText().toCharArray(), salt, iterations, keyLength);
	        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
	        byte[] keyBytes = factory.generateSecret(spec).getEncoded();
	        secretKey = new SecretKeySpec(keyBytes, "AES");
	
	        // Lies den Inhalt der Datei
	        byte[] fileContent = Files.readAllBytes(Paths.get(path));
	
	        Cipher cipher = Cipher.getInstance("AES");
	        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
	
	        // Verschlüssel den Dateiinhalt
	        byte[] encryptedContent = cipher.doFinal(fileContent);
	
	        FileOutputStream outputStream = new FileOutputStream(path);
	        
	        // Schreibe den Salt und den verschlüsselten Inhalt in die Datei
	        outputStream.write(salt);
	        outputStream.write(encryptedContent);
	        outputStream.close();
	
	        // Zeige den verschlüsselten Dateipfad im Label an
	        pathOutput.setText("save encrypted data " + path);
	    }
    }
}
