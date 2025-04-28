package photos.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import photos.model.Album;
import photos.model.User;
import photos.model.UserManager;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

/**
 * Controller for the album list screen.
 * Handles album management (listing, creating, renaming, deleting, and opening).
 * 
 * @author Krish Patel, Darshan Surti
 */
public class AlbumListController {

    @FXML
    private Label welcomeLabel;
    
    @FXML
    private ListView<Album> albumListView;
    
    @FXML
    private TextField albumNameField;
    
    @FXML
    private Label errorMessageLabel;
    
    private User user;
    private UserManager userManager;
    
    /**
     * Initializes the controller.
     */
    @FXML
    public void initialize() {
        userManager = UserManager.getInstance();
        errorMessageLabel.setText("");
        
        // Configure the album list view to show album details
        albumListView.setCellFactory(lv -> new ListCell<Album>() {
            @Override
            protected void updateItem(Album album, boolean empty) {
                super.updateItem(album, empty);
                if (empty || album == null) {
                    setText(null);
                } else {
                    StringBuilder sb = new StringBuilder();
                    sb.append(album.getName());
                    sb.append(" (").append(album.getPhotoCount()).append(" photos");
                    
                    // Add date range if the album has photos
                    Calendar[] dateRange = album.getDateRange();
                    if (dateRange != null) {
                        sb.append(", ");
                        sb.append(formatDate(dateRange[0])).append(" - ").append(formatDate(dateRange[1]));
                    }
                    
                    sb.append(")");
                    setText(sb.toString());
                }
            }
        });
    }
    
    /**
     * Sets the user for this controller.
     * 
     * @param user the User object
     */
    public void setUser(User user) {
        this.user = user;
        welcomeLabel.setText("Albums - " + user.getUsername());
        refreshAlbumList();
    }
    
    /**
     * Refreshes the list of albums in the ListView.
     */
    private void refreshAlbumList() {
        ObservableList<Album> albums = FXCollections.observableArrayList(user.getAlbums());
        albumListView.setItems(albums);
    }
    
    /**
     * Formats a Calendar object as a string for display.
     * 
     * @param calendar the Calendar to format
     * @return a string representation of the date
     */
    private String formatDate(Calendar calendar) {
        return String.format("%d/%d/%d", 
                calendar.get(Calendar.MONTH) + 1, 
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.YEAR));
    }
    
    /**
     * Handles the create album button action.
     * 
     * @param event the ActionEvent
     */
    @FXML
    public void handleCreateAlbum(ActionEvent event) {
        String albumName = albumNameField.getText().trim();
        
        if (albumName.isEmpty()) {
            errorMessageLabel.setText("Please enter an album name");
            return;
        }
        
        // Check if an album with this name already exists
        if (user.findAlbumByName(albumName) != null) {
            errorMessageLabel.setText("An album with this name already exists");
            return;
        }
        
        Album newAlbum = new Album(albumName);
        user.addAlbum(newAlbum);
        userManager.saveUsers();
        
        albumNameField.clear();
        errorMessageLabel.setText("");
        refreshAlbumList();
    }
    
    /**
     * Handles the rename album button action.
     * 
     * @param event the ActionEvent
     */
    @FXML
    public void handleRenameAlbum(ActionEvent event) {
        Album selectedAlbum = albumListView.getSelectionModel().getSelectedItem();
        
        if (selectedAlbum == null) {
            errorMessageLabel.setText("Please select an album to rename");
            return;
        }
        
        String newName = albumNameField.getText().trim();
        
        if (newName.isEmpty()) {
            errorMessageLabel.setText("Please enter a new album name");
            return;
        }
        
        // Check if an album with this name already exists
        if (user.findAlbumByName(newName) != null) {
            errorMessageLabel.setText("An album with this name already exists");
            return;
        }
        
        selectedAlbum.setName(newName);
        userManager.saveUsers();
        
        albumNameField.clear();
        errorMessageLabel.setText("");
        refreshAlbumList();
    }
    
    /**
     * Handles the delete album button action.
     * 
     * @param event the ActionEvent
     */
    @FXML
    public void handleDeleteAlbum(ActionEvent event) {
        Album selectedAlbum = albumListView.getSelectionModel().getSelectedItem();
        
        if (selectedAlbum == null) {
            errorMessageLabel.setText("Please select an album to delete");
            return;
        }
        
        Alert confirmAlert = new Alert(AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setHeaderText("Delete Album");
        confirmAlert.setContentText("Are you sure you want to delete the album '" + selectedAlbum.getName() + "'?");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                user.removeAlbum(selectedAlbum);
                userManager.saveUsers();
                refreshAlbumList();
                errorMessageLabel.setText("");
            }
        });
    }
    
    /**
     * Handles the open album button action.
     * 
     * @param event the ActionEvent
     */
    @FXML
    public void handleOpenAlbum(ActionEvent event) {
        Album selectedAlbum = albumListView.getSelectionModel().getSelectedItem();
        
        if (selectedAlbum == null) {
            errorMessageLabel.setText("Please select an album to open");
            return;
        }
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/photos/view/albumView.fxml"));
            Parent root = loader.load();
            
            AlbumViewController controller = loader.getController();
            controller.setUser(user);
            controller.setAlbum(selectedAlbum);
            
            Stage stage = (Stage) albumListView.getScene().getWindow();
            stage.setTitle("Album: " + selectedAlbum.getName());
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            errorMessageLabel.setText("Error opening album");
            e.printStackTrace();
        }
    }
    
    /**
     * Handles the search by date menu item action.
     * 
     * @param event the ActionEvent
     */
    @FXML
    public void handleSearchByDate(ActionEvent event) {
        // Create a dialog to get the date range
        Dialog<Calendar[]> dialog = new Dialog<>();
        dialog.setTitle("Search by Date Range");
        dialog.setHeaderText("Enter date range to search for photos");
        
        // Set up the dialog buttons
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        // Create the date pickers for start and end dates
        DatePicker startDatePicker = new DatePicker();
        DatePicker endDatePicker = new DatePicker();
        
        // Set to current date by default
        LocalDate now = LocalDate.now();
        startDatePicker.setValue(now);
        endDatePicker.setValue(now);
        
        // Create the dialog content
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setContent(createDateRangeContent(startDatePicker, endDatePicker));
        
        // Convert the result to Calendar[] when the OK button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                Calendar startDate = Calendar.getInstance();
                startDate.setTime(Date.from(startDatePicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant()));
                
                Calendar endDate = Calendar.getInstance();
                endDate.setTime(Date.from(endDatePicker.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant()));
                // Set to end of day
                endDate.set(Calendar.HOUR_OF_DAY, 23);
                endDate.set(Calendar.MINUTE, 59);
                endDate.set(Calendar.SECOND, 59);
                
                return new Calendar[] { startDate, endDate };
            }
            return null;
        });
        
        // Show the dialog and process the result
        Optional<Calendar[]> result = dialog.showAndWait();
        result.ifPresent(dateRange -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/photos/view/searchResults.fxml"));
                Parent root = loader.load();
                
                SearchResultsController controller = loader.getController();
                controller.setUser(user);
                controller.searchByDateRange(dateRange[0], dateRange[1]);
                
                Stage stage = (Stage) albumListView.getScene().getWindow();
                stage.setTitle("Search Results - Date Range");
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                errorMessageLabel.setText("Error showing search results");
                e.printStackTrace();
            }
        });
    }
    
    /**
     * Creates the content for the date range dialog.
     * 
     * @param startDatePicker the DatePicker for the start date
     * @param endDatePicker the DatePicker for the end date
     * @return a Parent containing the dialog content
     */
    private Parent createDateRangeContent(DatePicker startDatePicker, DatePicker endDatePicker) {
        VBox content = new VBox(10);
        content.setPadding(new Insets(20, 10, 10, 10));
        
        HBox startDateBox = new HBox(10);
        startDateBox.getChildren().addAll(new Label("Start Date:"), startDatePicker);
        
        HBox endDateBox = new HBox(10);
        endDateBox.getChildren().addAll(new Label("End Date:"), endDatePicker);
        
        content.getChildren().addAll(startDateBox, endDateBox);
        return content;
    }
    
    /**
     * Handles the search by tag menu item action.
     * 
     * @param event the ActionEvent
     */
    @FXML
    public void handleSearchByTag(ActionEvent event) {
        // Create a dialog for tag search
        Dialog<TagSearchCriteria> dialog = new Dialog<>();
        dialog.setTitle("Search by Tags");
        dialog.setHeaderText("Enter tag criteria to search for photos");
        
        // Set up the dialog buttons
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        // Create the tag input fields
        TextField tag1NameField = new TextField();
        tag1NameField.setPromptText("Tag name (e.g., location)");
        
        TextField tag1ValueField = new TextField();
        tag1ValueField.setPromptText("Tag value (e.g., New York)");
        
        // Add combo box for AND/OR search
        ComboBox<String> operatorCombo = new ComboBox<>();
        operatorCombo.getItems().addAll("Single Tag", "AND", "OR");
        operatorCombo.setValue("Single Tag");
        
        // Fields for second tag (initially disabled)
        TextField tag2NameField = new TextField();
        tag2NameField.setPromptText("Tag name (e.g., person)");
        tag2NameField.setDisable(true);
        
        TextField tag2ValueField = new TextField();
        tag2ValueField.setPromptText("Tag value (e.g., John)");
        tag2ValueField.setDisable(true);
        
        // Enable/disable second tag fields based on combo box selection
        operatorCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            boolean enableSecondTag = !newVal.equals("Single Tag");
            tag2NameField.setDisable(!enableSecondTag);
            tag2ValueField.setDisable(!enableSecondTag);
        });
        
        // Create the dialog content
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setContent(createTagSearchContent(tag1NameField, tag1ValueField, 
                                                  operatorCombo, 
                                                  tag2NameField, tag2ValueField));
        
        // Convert the result when the OK button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                String tag1Name = tag1NameField.getText().trim();
                String tag1Value = tag1ValueField.getText().trim();
                String operator = operatorCombo.getValue();
                String tag2Name = tag2NameField.getText().trim();
                String tag2Value = tag2ValueField.getText().trim();
                
                return new TagSearchCriteria(tag1Name, tag1Value, operator, tag2Name, tag2Value);
            }
            return null;
        });
        
        // Show the dialog and process the result
        Optional<TagSearchCriteria> result = dialog.showAndWait();
        result.ifPresent(criteria -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/photos/view/searchResults.fxml"));
                Parent root = loader.load();
                
                SearchResultsController controller = loader.getController();
                controller.setUser(user);
                
                String operator = criteria.getOperator();
                if (operator.equals("Single Tag")) {
                    controller.searchByTag(criteria.getTag1Name(), criteria.getTag1Value());
                } else if (operator.equals("AND")) {
                    controller.searchByTagsAnd(criteria.getTag1Name(), criteria.getTag1Value(),
                                               criteria.getTag2Name(), criteria.getTag2Value());
                } else if (operator.equals("OR")) {
                    controller.searchByTagsOr(criteria.getTag1Name(), criteria.getTag1Value(),
                                              criteria.getTag2Name(), criteria.getTag2Value());
                }
                
                Stage stage = (Stage) albumListView.getScene().getWindow();
                stage.setTitle("Search Results - Tags");
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                errorMessageLabel.setText("Error showing search results");
                e.printStackTrace();
            }
        });
    }
    
    /**
     * Creates the content for the tag search dialog.
     * 
     * @param tag1NameField the TextField for the first tag name
     * @param tag1ValueField the TextField for the first tag value
     * @param operatorCombo the ComboBox for selecting the operator
     * @param tag2NameField the TextField for the second tag name
     * @param tag2ValueField the TextField for the second tag value
     * @return a Parent containing the dialog content
     */
    private Parent createTagSearchContent(TextField tag1NameField, TextField tag1ValueField,
                                         ComboBox<String> operatorCombo,
                                         TextField tag2NameField, TextField tag2ValueField) {
        VBox content = new VBox(10);
        content.setPadding(new Insets(20, 10, 10, 10));
        
        HBox tag1Box = new HBox(10);
        tag1Box.getChildren().addAll(new Label("Tag 1:"), tag1NameField, new Label("Value:"), tag1ValueField);
        
        HBox operatorBox = new HBox(10);
        operatorBox.getChildren().addAll(new Label("Operator:"), operatorCombo);
        
        HBox tag2Box = new HBox(10);
        tag2Box.getChildren().addAll(new Label("Tag 2:"), tag2NameField, new Label("Value:"), tag2ValueField);
        
        content.getChildren().addAll(tag1Box, operatorBox, tag2Box);
        return content;
    }
    
    /**
     * Handles the logout menu item action.
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
            
            Stage stage = (Stage) albumListView.getScene().getWindow();
            stage.setTitle("Photos Application");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            errorMessageLabel.setText("Error returning to login screen");
            e.printStackTrace();
        }
    }
    
    /**
     * Handles the quit menu item action.
     * 
     * @param event the ActionEvent
     */
    @FXML
    public void handleQuit(ActionEvent event) {
        userManager.saveUsers();
        Platform.exit();
    }
    
    /**
     * Helper class to store tag search criteria.
     */
    private static class TagSearchCriteria {
        private String tag1Name;
        private String tag1Value;
        private String operator;
        private String tag2Name;
        private String tag2Value;
        
        public TagSearchCriteria(String tag1Name, String tag1Value, String operator, String tag2Name, String tag2Value) {
            this.tag1Name = tag1Name;
            this.tag1Value = tag1Value;
            this.operator = operator;
            this.tag2Name = tag2Name;
            this.tag2Value = tag2Value;
        }
        
        public String getTag1Name() { return tag1Name; }
        public String getTag1Value() { return tag1Value; }
        public String getOperator() { return operator; }
        public String getTag2Name() { return tag2Name; }
        public String getTag2Value() { return tag2Value; }
    }
} 