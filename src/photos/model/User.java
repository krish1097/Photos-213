package photos.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a user in the Photos application.
 * Users have a username and a list of albums.
 * 
 * @author Krish Patel, Darshan Surti
 */
public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String username;
    private List<Album> albums;
    
    /**
     * Creates a new user with the given username and an empty list of albums.
     * 
     * @param username the username for this user
     */
    public User(String username) {
        this.username = username;
        this.albums = new ArrayList<>();
    }
    
    /**
     * Gets the username of this user.
     * 
     * @return the username
     */
    public String getUsername() {
        return username;
    }
    
    /**
     * Gets the list of albums for this user.
     * 
     * @return the list of albums
     */
    public List<Album> getAlbums() {
        return albums;
    }
    
    /**
     * Adds an album to this user's list of albums.
     * 
     * @param album the album to add
     * @return true if the album was added successfully, false if an album with the same name already exists
     */
    public boolean addAlbum(Album album) {
        // Check if an album with this name already exists
        for (Album existingAlbum : albums) {
            if (existingAlbum.getName().equals(album.getName())) {
                return false;
            }
        }
        
        albums.add(album);
        return true;
    }
    
    /**
     * Removes an album from this user's list of albums.
     * 
     * @param album the album to remove
     * @return true if the album was removed, false if it wasn't found
     */
    public boolean removeAlbum(Album album) {
        return albums.remove(album);
    }
    
    /**
     * Finds an album by name.
     * 
     * @param name the name of the album to find
     * @return the album with the given name, or null if not found
     */
    public Album findAlbumByName(String name) {
        for (Album album : albums) {
            if (album.getName().equals(name)) {
                return album;
            }
        }
        return null;
    }
} 