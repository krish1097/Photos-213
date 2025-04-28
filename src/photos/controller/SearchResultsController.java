package photos.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import photos.model.Album;
import photos.model.Photo;
import photos.model.SearchUtil;
import photos.model.User;
import photos.model.UserManager;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;

/**
 * Controller for the search results screen.
 * Displays search results and allows creating an album from them.
 * 
 * @author Krish Patel, Darshan Surti
 */
public class SearchResultsController {

    @FXML
    private Label titleLabel;
    
    @FXML
    private ListView<Photo> resultsListView;
    
    @FXML
    private TextField albumNameField;
    
    @FXML
    private Label errorMessageLabel;
    
    private User user;
    private UserManager userManager;
    private List<Photo> searchResults;
    
    /**
     * Initializes the controller.
     */
    @FXML
    public void initialize() {
        userManager = UserManager.getInstance();
        errorMessageLabel.setText("");
        
        // Configure the photo list view cell factory
        resultsListView.setCellFactory(lv -> new ListCell<Photo>() {
            @Override
            protected void updateItem(Photo photo, boolean empty) {
                super.updateItem(photo, empty);
                if (empty || photo == null) {
                    setText(null);
                } else {
                    setText(photo.getCaption().isEmpty() ? 
                          "(No caption) - " + new File(photo.getFilePath()).getName() : 
                          photo.getCaption());
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
    }
    
    /**
     * Performs a search by date range and displays the results.
     * 
     * @param startDate the start date of the range
     * @param endDate the end date of the range
     */
    public void searchByDateRange(Calendar startDate, Calendar endDate) {
        searchResults = SearchUtil.searchByDateRange(user.getAlbums(), startDate, endDate);
        displayResults("Search Results - Date Range: " + formatDate(startDate) + " to " + formatDate(endDate));
    }
    
    /**
     * Performs a search by a single tag and displays the results.
     * 
     * @param tagName the tag name to search for
     * @param tagValue the tag value to search for
     */
    public void searchByTag(String tagName, String tagValue) {
        searchResults = SearchUtil.searchByTag(user.getAlbums(), tagName, tagValue);
        displayResults("Search Results - Tag: " + tagName + ":" + tagValue);
    }
    
    /**
     * Performs a search by two tags with AND operator and displays the results.
     * 
     * @param tag1Name the first tag name
     * @param tag1Value the first tag value
     * @param tag2Name the second tag name
     * @param tag2Value the second tag value
     */
    public void searchByTagsAnd(String tag1Name, String tag1Value, String tag2Name, String tag2Value) {
        searchResults = SearchUtil.searchByTagsAnd(user.getAlbums(), tag1Name, tag1Value, tag2Name, tag2Value);
        displayResults("Search Results - Tags: " + tag1Name + ":" + tag1Value + " AND " + tag2Name + ":" + tag2Value);
    }
    
    /**
     * Performs a search by two tags with OR operator and displays the results.
     * 
     * @param tag1Name the first tag name
     * @param tag1Value the first tag value
     * @param tag2Name the second tag name
     * @param tag2Value the second tag value
     */
    public void searchByTagsOr(String tag1Name, String tag1Value, String tag2Name, String tag2Value) {
        searchResults = SearchUtil.searchByTagsOr(user.getAlbums(), tag1Name, tag1Value, tag2Name, tag2Value);
        displayResults("Search Results - Tags: " + tag1Name + ":" + tag1Value + " OR " + tag2Name + ":" + tag2Value);
    }
    
    /**
     * Displays the search results in the list view.
     * 
     * @param title the title to display for the search results
     */
    private void displayResults(String title) {
        titleLabel.setText(title);
        
        ObservableList<Photo> photos = FXCollections.observableArrayList(searchResults);
        resultsListView.setItems(photos);
        
        if (searchResults.isEmpty()) {
            errorMessageLabel.setText("No photos found matching the search criteria");
        } else {
            errorMessageLabel.setText("");
        }
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
     * Creates a new album from the search results.
     * 
     * @param event the ActionEvent
     */
    @FXML
    public void handleCreateAlbum(ActionEvent event) {
        if (searchResults.isEmpty()) {
            errorMessageLabel.setText("No photos to create an album from");
            return;
        }
        
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
        
        // Add all search results to the new album
        for (Photo photo : searchResults) {
            newAlbum.addPhoto(photo);
        }
        
        user.addAlbum(newAlbum);
        userManager.saveUsers();
        
        errorMessageLabel.setText("Album \"" + albumName + "\" created with " + searchResults.size() + " photos");
        albumNameField.clear();
    }
    
    /**
     * Handles the back button action.
     * Returns to the album list screen.
     * 
     * @param event the ActionEvent
     */
    @FXML
    public void handleBack(ActionEvent event) {
        try {
            // Save any changes
            userManager.saveUsers();
            
            // Load the album list screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/photos/view/albumList.fxml"));
            Parent root = loader.load();
            
            AlbumListController controller = loader.getController();
            controller.setUser(user);
            
            Stage stage = (Stage) resultsListView.getScene().getWindow();
            stage.setTitle("Albums - " + user.getUsername());
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            errorMessageLabel.setText("Error returning to album list");
            e.printStackTrace();
        }
    }
} 