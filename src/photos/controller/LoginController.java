package photos.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import photos.model.User;
import photos.model.UserManager;

import java.io.IOException;

/**
 * Controller for the login screen.
 * Handles user authentication and navigation to the appropriate screens.
 * 
 * @author Krish Patel, Darshan Surti
 */
public class LoginController {

    @FXML
    private TextField usernameField;
    
    @FXML
    private Label errorMessageLabel;
    
    private UserManager userManager;
    
    /**
     * Initializes the controller.
     */
    @FXML
    public void initialize() {
        userManager = UserManager.getInstance();
        errorMessageLabel.setText("");
    }
    
    /**
     * Handles the login button action.
     * Authenticates the user and navigates to the appropriate screen.
     * 
     * @param event the ActionEvent
     */
    @FXML
    public void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        
        if (username.isEmpty()) {
            errorMessageLabel.setText("Please enter a username");
            return;
        }
        
        if (userManager.userExists(username)) {
            User user = userManager.getUser(username);
            
            if (username.equals("admin")) {
                openAdminScreen();
            } else {
                openAlbumListScreen(user);
            }
        } else {
            errorMessageLabel.setText("User does not exist");
        }
    }
    
    /**
     * Opens the admin screen.
     */
    private void openAdminScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/photos/view/admin.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setTitle("Admin");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            errorMessageLabel.setText("Error loading admin screen");
            e.printStackTrace();
        }
    }
    
    /**
     * Opens the album list screen for a regular user.
     * 
     * @param user the User object
     */
    private void openAlbumListScreen(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/photos/view/albumList.fxml"));
            Parent root = loader.load();
            
            AlbumListController controller = loader.getController();
            controller.setUser(user);
            
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setTitle("Albums - " + user.getUsername());
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            errorMessageLabel.setText("Error loading album list screen");
            e.printStackTrace();
        }
    }
    
    /**
     * Handles the quit button action.
     * Saves all user data and exits the application.
     * 
     * @param event the ActionEvent
     */
    @FXML
    public void handleQuit(ActionEvent event) {
        userManager.saveUsers();
        Platform.exit();
    }
} 