package photos.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import photos.model.UserManager;

import java.io.IOException;
import java.util.List;

/**
 * Controller for the admin screen.
 * Handles user management (listing, adding, and deleting users).
 * 
 * @author Krish Patel, Darshan Surti
 */
public class AdminController {

    @FXML
    private ListView<String> userListView;
    
    @FXML
    private TextField newUserField;
    
    @FXML
    private Label errorMessageLabel;
    
    private UserManager userManager;
    private ObservableList<String> userList;
    
    /**
     * Initializes the controller.
     */
    @FXML
    public void initialize() {
        userManager = UserManager.getInstance();
        errorMessageLabel.setText("");
        
        // Load the list of users
        refreshUserList();
    }
    
    /**
     * Refreshes the list of users in the ListView.
     */
    private void refreshUserList() {
        List<String> usernames = userManager.getAllUsernames();
        userList = FXCollections.observableArrayList(usernames);
        userListView.setItems(userList);
    }
    
    /**
     * Handles the add user button action.
     * 
     * @param event the ActionEvent
     */
    @FXML
    public void handleAddUser(ActionEvent event) {
        String username = newUserField.getText().trim();
        
        if (username.isEmpty()) {
            errorMessageLabel.setText("Please enter a username");
            return;
        }
        
        if (userManager.userExists(username)) {
            errorMessageLabel.setText("User already exists");
            return;
        }
        
        userManager.addUser(username);
        newUserField.clear();
        errorMessageLabel.setText("");
        refreshUserList();
    }
    
    /**
     * Handles the delete user button action.
     * 
     * @param event the ActionEvent
     */
    @FXML
    public void handleDeleteUser(ActionEvent event) {
        String selectedUsername = userListView.getSelectionModel().getSelectedItem();
        
        if (selectedUsername == null) {
            errorMessageLabel.setText("Please select a user to delete");
            return;
        }
        
        if (selectedUsername.equals("admin")) {
            errorMessageLabel.setText("Cannot delete admin user");
            return;
        }
        
        Alert confirmAlert = new Alert(AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setHeaderText("Delete User");
        confirmAlert.setContentText("Are you sure you want to delete the user '" + selectedUsername + "'?");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                userManager.removeUser(selectedUsername);
                refreshUserList();
                errorMessageLabel.setText("");
            }
        });
    }
    
    /**
     * Handles the logout button action.
     * Returns to the login screen.
     * 
     * @param event the ActionEvent
     */
    @FXML
    public void handleLogout(ActionEvent event) {
        try {
            // Save any changes
            userManager.saveUsers();
            
            // Load the login screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/photos/view/login.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) userListView.getScene().getWindow();
            stage.setTitle("Photos Application");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            errorMessageLabel.setText("Error returning to login screen");
            e.printStackTrace();
        }
    }
} 