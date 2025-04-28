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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import photos.model.Album;
import photos.model.Photo;
import photos.model.Tag;
import photos.model.User;
import photos.model.UserManager;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controller for the album view screen.
 * Handles photo management within an album.
 * 
 * @author Krish Patel, Darshan Surti
 */
public class AlbumViewController {

    @FXML
    private Label albumTitleLabel;
    
    @FXML
    private ListView<Photo> photoListView;
    
    @FXML
    private ImageView photoImageView;
    
    @FXML
    private Label photoCaptionLabel;
    
    @FXML
    private Label photoDateLabel;
    
    @FXML
    private Label photoTagsLabel;
    
    @FXML
    private Label errorMessageLabel;
    
    private User user;
    private Album album;
    private UserManager userManager;
    private Photo currentPhoto;
    
    /**
     * Initializes the controller.
     */
    @FXML
    public void initialize() {
        userManager = UserManager.getInstance();
        errorMessageLabel.setText("");
        
        // Hide photo details initially
        clearPhotoDisplay();
        
        // Configure the photo list view cell factory
        photoListView.setCellFactory(lv -> new ListCell<Photo>() {
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
        
        // Add selection listener to photo list
        photoListView.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                if (newValue != null) {
                    displayPhoto(newValue);
                } else {
                    clearPhotoDisplay();
                }
            }
        );
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
     * Sets the album for this controller and updates the UI.
     * 
     * @param album the Album object
     */
    public void setAlbum(Album album) {
        this.album = album;
        albumTitleLabel.setText("Album: " + album.getName());
        refreshPhotoList();
    }
    
    /**
     * Refreshes the list of photos in the ListView.
     */
    private void refreshPhotoList() {
        ObservableList<Photo> photos = FXCollections.observableArrayList(album.getPhotos());
        photoListView.setItems(photos);
        
        // Select the first photo if available
        if (!photos.isEmpty()) {
            photoListView.getSelectionModel().select(0);
        }
    }
    
    /**
     * Displays a photo in the image view.
     * 
     * @param photo the Photo to display
     */
    private void displayPhoto(Photo photo) {
        currentPhoto = photo;
        
        try {
            // Load the image
            File file = new File(photo.getFilePath());
            if (file.exists()) {
                Image image = new Image(file.toURI().toString());
                photoImageView.setImage(image);
                
                // Set caption and date
                photoCaptionLabel.setText(photo.getCaption().isEmpty() ? 
                                         "(No caption)" : photo.getCaption());
                
                // Format date
                photoDateLabel.setText("Date: " + String.format("%d/%d/%d", 
                                     photo.getDateTaken().get(java.util.Calendar.MONTH) + 1,
                                     photo.getDateTaken().get(java.util.Calendar.DAY_OF_MONTH),
                                     photo.getDateTaken().get(java.util.Calendar.YEAR)));
                
                // Format tags
                if (photo.getTags().isEmpty()) {
                    photoTagsLabel.setText("Tags: (None)");
                } else {
                    String tagsText = photo.getTags().stream()
                        .map(tag -> tag.getName() + ":" + tag.getValue())
                        .collect(Collectors.joining(", "));
                    photoTagsLabel.setText("Tags: " + tagsText);
                }
            } else {
                photoImageView.setImage(null);
                errorMessageLabel.setText("Photo file not found: " + file.getAbsolutePath());
            }
        } catch (Exception e) {
            photoImageView.setImage(null);
            errorMessageLabel.setText("Error displaying photo: " + e.getMessage());
        }
    }
    
    /**
     * Clears the photo display area.
     */
    private void clearPhotoDisplay() {
        photoImageView.setImage(null);
        photoCaptionLabel.setText("");
        photoDateLabel.setText("");
        photoTagsLabel.setText("");
        currentPhoto = null;
    }
    
    /**
     * Handles the add photo button action.
     * 
     * @param event the ActionEvent
     */
    @FXML
    public void handleAddPhoto(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Photo");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        );
        
        File selectedFile = fileChooser.showOpenDialog(photoListView.getScene().getWindow());
        if (selectedFile != null) {
            String filePath = selectedFile.getAbsolutePath();
            
            // Check if the photo is already in the album
            for (Photo existingPhoto : album.getPhotos()) {
                if (existingPhoto.getFilePath().equals(filePath)) {
                    errorMessageLabel.setText("This photo is already in the album");
                    return;
                }
            }
            
            Photo newPhoto = new Photo(filePath);
            album.addPhoto(newPhoto);
            userManager.saveUsers();
            
            refreshPhotoList();
            photoListView.getSelectionModel().select(newPhoto);
            errorMessageLabel.setText("");
        }
    }
    
    /**
     * Handles the remove photo button action.
     * 
     * @param event the ActionEvent
     */
    @FXML
    public void handleRemovePhoto(ActionEvent event) {
        Photo selectedPhoto = photoListView.getSelectionModel().getSelectedItem();
        
        if (selectedPhoto == null) {
            errorMessageLabel.setText("Please select a photo to remove");
            return;
        }
        
        Alert confirmAlert = new Alert(AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Remove");
        confirmAlert.setHeaderText("Remove Photo");
        confirmAlert.setContentText("Are you sure you want to remove this photo from the album?");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                album.removePhoto(selectedPhoto);
                userManager.saveUsers();
                
                refreshPhotoList();
                errorMessageLabel.setText("");
            }
        });
    }
    
    /**
     * Handles the caption photo button action.
     * 
     * @param event the ActionEvent
     */
    @FXML
    public void handleCaptionPhoto(ActionEvent event) {
        Photo selectedPhoto = photoListView.getSelectionModel().getSelectedItem();
        
        if (selectedPhoto == null) {
            errorMessageLabel.setText("Please select a photo to caption");
            return;
        }
        
        TextInputDialog dialog = new TextInputDialog(selectedPhoto.getCaption());
        dialog.setTitle("Caption Photo");
        dialog.setHeaderText("Enter Caption");
        dialog.setContentText("Caption:");
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(caption -> {
            selectedPhoto.setCaption(caption);
            userManager.saveUsers();
            
            refreshPhotoList();
            photoListView.getSelectionModel().select(selectedPhoto);
            displayPhoto(selectedPhoto);
            errorMessageLabel.setText("");
        });
    }
    
    /**
     * Handles the add tag button action.
     * 
     * @param event the ActionEvent
     */
    @FXML
    public void handleAddTag(ActionEvent event) {
        Photo selectedPhoto = photoListView.getSelectionModel().getSelectedItem();
        
        if (selectedPhoto == null) {
            errorMessageLabel.setText("Please select a photo to tag");
            return;
        }
        
        // Create a dialog for adding a tag
        Dialog<Tag> dialog = new Dialog<>();
        dialog.setTitle("Add Tag");
        dialog.setHeaderText("Add a tag to the photo");
        
        // Set up the buttons
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        // Create the tag name and value fields
        TextField tagNameField = new TextField();
        tagNameField.setPromptText("Tag name (e.g., location, person)");
        
        TextField tagValueField = new TextField();
        tagValueField.setPromptText("Tag value (e.g., New York, John)");
        
        // Create the dialog content
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setContent(createTagDialogContent(tagNameField, tagValueField));
        
        // Convert the result when the OK button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                String tagName = tagNameField.getText().trim();
                String tagValue = tagValueField.getText().trim();
                
                if (tagName.isEmpty() || tagValue.isEmpty()) {
                    return null;
                }
                
                return new Tag(tagName, tagValue);
            }
            return null;
        });
        
        // Show the dialog and process the result
        Optional<Tag> result = dialog.showAndWait();
        result.ifPresent(tag -> {
            if (selectedPhoto.addTag(tag)) {
                userManager.saveUsers();
                displayPhoto(selectedPhoto);
                errorMessageLabel.setText("");
            } else {
                errorMessageLabel.setText("This tag already exists on the photo");
            }
        });
    }
    
    /**
     * Creates the content for the tag dialog.
     * 
     * @param tagNameField the TextField for the tag name
     * @param tagValueField the TextField for the tag value
     * @return a Parent containing the dialog content
     */
    private Parent createTagDialogContent(TextField tagNameField, TextField tagValueField) {
        VBox content = new VBox(10);
        content.setPadding(new Insets(20, 10, 10, 10));
        
        HBox tagNameBox = new HBox(10);
        tagNameBox.getChildren().addAll(new Label("Tag Name:"), tagNameField);
        
        HBox tagValueBox = new HBox(10);
        tagValueBox.getChildren().addAll(new Label("Tag Value:"), tagValueField);
        
        content.getChildren().addAll(tagNameBox, tagValueBox);
        return content;
    }
    
    /**
     * Handles the remove tag button action.
     * 
     * @param event the ActionEvent
     */
    @FXML
    public void handleRemoveTag(ActionEvent event) {
        Photo selectedPhoto = photoListView.getSelectionModel().getSelectedItem();
        
        if (selectedPhoto == null) {
            errorMessageLabel.setText("Please select a photo");
            return;
        }
        
        List<Tag> tags = selectedPhoto.getTags();
        
        if (tags.isEmpty()) {
            errorMessageLabel.setText("This photo has no tags");
            return;
        }
        
        // Create a dialog for selecting a tag to remove
        ChoiceDialog<Tag> dialog = new ChoiceDialog<>(tags.get(0), tags);
        dialog.setTitle("Remove Tag");
        dialog.setHeaderText("Select a tag to remove");
        dialog.setContentText("Tag:");
        
        // Show the dialog and process the result
        Optional<Tag> result = dialog.showAndWait();
        result.ifPresent(tag -> {
            selectedPhoto.removeTag(tag);
            userManager.saveUsers();
            displayPhoto(selectedPhoto);
            errorMessageLabel.setText("");
        });
    }
    
    /**
     * Handles the copy photo to album button action.
     * 
     * @param event the ActionEvent
     */
    @FXML
    public void handleCopyPhoto(ActionEvent event) {
        Photo selectedPhoto = photoListView.getSelectionModel().getSelectedItem();
        
        if (selectedPhoto == null) {
            errorMessageLabel.setText("Please select a photo to copy");
            return;
        }
        
        List<Album> albums = user.getAlbums();
        
        // Filter out the current album
        List<Album> otherAlbums = albums.stream()
            .filter(a -> !a.getName().equals(album.getName()))
            .collect(Collectors.toList());
        
        if (otherAlbums.isEmpty()) {
            errorMessageLabel.setText("No other albums to copy to. Create an album first.");
            return;
        }
        
        // Create a dialog for selecting an album
        ChoiceDialog<Album> dialog = new ChoiceDialog<>(otherAlbums.get(0), otherAlbums);
        dialog.setTitle("Copy Photo");
        dialog.setHeaderText("Select an album to copy the photo to");
        dialog.setContentText("Album:");
        
        // Show the dialog and process the result
        Optional<Album> result = dialog.showAndWait();
        result.ifPresent(targetAlbum -> {
            // Check if the photo is already in the target album
            boolean photoExists = false;
            for (Photo photo : targetAlbum.getPhotos()) {
                if (photo.getFilePath().equals(selectedPhoto.getFilePath())) {
                    photoExists = true;
                    break;
                }
            }
            
            if (photoExists) {
                errorMessageLabel.setText("Photo already exists in the target album");
            } else {
                targetAlbum.addPhoto(selectedPhoto);
                userManager.saveUsers();
                errorMessageLabel.setText("Photo copied to " + targetAlbum.getName());
            }
        });
    }
    
    /**
     * Handles the move photo to album button action.
     * 
     * @param event the ActionEvent
     */
    @FXML
    public void handleMovePhoto(ActionEvent event) {
        Photo selectedPhoto = photoListView.getSelectionModel().getSelectedItem();
        
        if (selectedPhoto == null) {
            errorMessageLabel.setText("Please select a photo to move");
            return;
        }
        
        List<Album> albums = user.getAlbums();
        
        // Filter out the current album
        List<Album> otherAlbums = albums.stream()
            .filter(a -> !a.getName().equals(album.getName()))
            .collect(Collectors.toList());
        
        if (otherAlbums.isEmpty()) {
            errorMessageLabel.setText("No other albums to move to. Create an album first.");
            return;
        }
        
        // Create a dialog for selecting an album
        ChoiceDialog<Album> dialog = new ChoiceDialog<>(otherAlbums.get(0), otherAlbums);
        dialog.setTitle("Move Photo");
        dialog.setHeaderText("Select an album to move the photo to");
        dialog.setContentText("Album:");
        
        // Show the dialog and process the result
        Optional<Album> result = dialog.showAndWait();
        result.ifPresent(targetAlbum -> {
            // Check if the photo is already in the target album
            boolean photoExists = false;
            for (Photo photo : targetAlbum.getPhotos()) {
                if (photo.getFilePath().equals(selectedPhoto.getFilePath())) {
                    photoExists = true;
                    break;
                }
            }
            
            if (photoExists) {
                errorMessageLabel.setText("Photo already exists in the target album");
            } else {
                // Add to target album
                targetAlbum.addPhoto(selectedPhoto);
                
                // Remove from current album
                album.removePhoto(selectedPhoto);
                
                userManager.saveUsers();
                refreshPhotoList();
                errorMessageLabel.setText("Photo moved to " + targetAlbum.getName());
            }
        });
    }
    
    /**
     * Handles the previous photo button action.
     * 
     * @param event the ActionEvent
     */
    @FXML
    public void handlePreviousPhoto(ActionEvent event) {
        int currentIndex = photoListView.getSelectionModel().getSelectedIndex();
        
        if (currentIndex > 0) {
            photoListView.getSelectionModel().select(currentIndex - 1);
            photoListView.scrollTo(currentIndex - 1);
        } else {
            // Wrap around to the end if at the beginning
            photoListView.getSelectionModel().select(photoListView.getItems().size() - 1);
            photoListView.scrollTo(photoListView.getItems().size() - 1);
        }
    }
    
    /**
     * Handles the next photo button action.
     * 
     * @param event the ActionEvent
     */
    @FXML
    public void handleNextPhoto(ActionEvent event) {
        int currentIndex = photoListView.getSelectionModel().getSelectedIndex();
        int lastIndex = photoListView.getItems().size() - 1;
        
        if (currentIndex < lastIndex) {
            photoListView.getSelectionModel().select(currentIndex + 1);
            photoListView.scrollTo(currentIndex + 1);
        } else {
            // Wrap around to the beginning if at the end
            photoListView.getSelectionModel().select(0);
            photoListView.scrollTo(0);
        }
    }
    
    /**
     * Handles the back to album list button action.
     * 
     * @param event the ActionEvent
     */
    @FXML
    public void handleBackToAlbumList(ActionEvent event) {
        try {
            // Save any changes
            userManager.saveUsers();
            
            // Load the album list screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/photos/view/albumList.fxml"));
            Parent root = loader.load();
            
            AlbumListController controller = loader.getController();
            controller.setUser(user);
            
            Stage stage = (Stage) photoListView.getScene().getWindow();
            stage.setTitle("Albums - " + user.getUsername());
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            errorMessageLabel.setText("Error returning to album list");
            e.printStackTrace();
        }
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
            
            Stage stage = (Stage) photoListView.getScene().getWindow();
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
} 