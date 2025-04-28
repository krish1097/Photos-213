package photos.model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to handle stock photos for the application.
 * 
 * @author Krish Patel, Darshan Surti
 */
public class StockPhotoManager {
    
    private static final String STOCK_PHOTOS_DIR = "data" + File.separator + "stock";
    
    /**
     * Initializes the stock photos in the stock user's album.
     * This should be called when the application starts.
     */
    public static void initializeStockPhotos() {
        // Create the stock photos directory if it doesn't exist
        File stockDir = new File(STOCK_PHOTOS_DIR);
        if (!stockDir.exists()) {
            stockDir.mkdirs();
        }
        
        // Check if we have the stock user
        UserManager userManager = UserManager.getInstance();
        User stockUser = userManager.getUser("stock");
        
        if (stockUser == null) {
            return;
        }
        
        // Find the stock album
        Album stockAlbum = stockUser.findAlbumByName("stock");
        if (stockAlbum == null) {
            stockAlbum = new Album("stock");
            stockUser.addAlbum(stockAlbum);
        }
        
        // Get stock photo files
        File[] stockPhotoFiles = stockDir.listFiles((dir, name) -> {
            String lowerName = name.toLowerCase();
            return lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg") || 
                   lowerName.endsWith(".png") || lowerName.endsWith(".gif") || 
                   lowerName.endsWith(".bmp");
        });
        
        // If we don't have any stock photos, we'll add placeholders
        if (stockPhotoFiles == null || stockPhotoFiles.length < 5) {
            try {
                createPlaceholderPhotos(stockDir, 5);
                
                // Reload the list
                stockPhotoFiles = stockDir.listFiles((dir, name) -> {
                    String lowerName = name.toLowerCase();
                    return lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg") || 
                           lowerName.endsWith(".png") || lowerName.endsWith(".gif") || 
                           lowerName.endsWith(".bmp");
                });
            } catch (IOException e) {
                System.err.println("Failed to create placeholder photos: " + e.getMessage());
                return;
            }
        }
        
        // Add stock photos to the stock album
        if (stockPhotoFiles != null) {
            for (File photoFile : stockPhotoFiles) {
                // Check if the photo is already in the album
                boolean alreadyExists = false;
                for (Photo existingPhoto : stockAlbum.getPhotos()) {
                    if (existingPhoto.getFilePath().equals(photoFile.getAbsolutePath())) {
                        alreadyExists = true;
                        break;
                    }
                }
                
                if (!alreadyExists) {
                    Photo photo = new Photo(photoFile.getAbsolutePath());
                    photo.setCaption("Stock photo: " + photoFile.getName());
                    
                    // Add some sample tags
                    photo.addTag(new Tag("type", "stock"));
                    photo.addTag(new Tag("filename", photoFile.getName()));
                    
                    stockAlbum.addPhoto(photo);
                }
            }
        }
        
        // Save changes
        userManager.saveUsers();
    }
    
    /**
     * Creates placeholder image files in the stock photos directory.
     * 
     * @param stockDir the stock photos directory
     * @param count the number of placeholder photos to create
     * @throws IOException if there's an error creating the files
     */
    private static void createPlaceholderPhotos(File stockDir, int count) throws IOException {
        List<String> fileNames = new ArrayList<>();
        fileNames.add("beach.jpg");
        fileNames.add("mountains.jpg");
        fileNames.add("city.jpg");
        fileNames.add("forest.jpg");
        fileNames.add("sunset.jpg");
        
        // Create basic text files as placeholder for images
        // In a real application, you would include actual image files
        for (int i = 0; i < Math.min(count, fileNames.size()); i++) {
            Path filePath = Paths.get(stockDir.getAbsolutePath(), fileNames.get(i));
            if (!Files.exists(filePath)) {
                String content = "This is a placeholder for a stock photo: " + fileNames.get(i);
                Files.write(filePath, content.getBytes());
            }
        }
    }
} 