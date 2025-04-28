package photos.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import photos.model.StockPhotoManager;
import photos.model.UserManager;

/**
 * Main entry point for the Photos application.
 * This class launches the JavaFX application and loads the login screen.
 * 
 * @author Krish Patel, Darshan Surti
 */
public class Photos extends Application {
    
    /**
     * JavaFX start method that sets up the primary stage
     * 
     * @param primaryStage The primary stage for this application
     * @throws Exception if there's an error loading FXML
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Initialize the user manager and stock photos
        UserManager.getInstance();
        StockPhotoManager.initializeStockPhotos();
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/photos/view/login.fxml"));
        BorderPane root = loader.load();
        
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Photos Application");
        primaryStage.setResizable(true);
        primaryStage.show();
    }
    
    /**
     * Main method that launches the JavaFX application
     * 
     * @param args command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
} 