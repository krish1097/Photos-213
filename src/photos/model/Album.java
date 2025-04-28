package photos.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Represents an album in the Photos application.
 * Albums have a name and contain a list of photos.
 * 
 * @author Krish Patel, Darshan Surti
 */
public class Album implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String name;
    private List<Photo> photos;
    
    /**
     * Creates a new album with the given name and an empty list of photos.
     * 
     * @param name the name of the album
     */
    public Album(String name) {
        this.name = name;
        this.photos = new ArrayList<>();
    }
    
    /**
     * Gets the name of this album.
     * 
     * @return the album name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Sets the name of this album.
     * 
     * @param name the new album name
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Gets the list of photos in this album.
     * 
     * @return the list of photos
     */
    public List<Photo> getPhotos() {
        return photos;
    }
    
    /**
     * Adds a photo to this album.
     * 
     * @param photo the photo to add
     * @return true if the photo was added, false if it was already in the album
     */
    public boolean addPhoto(Photo photo) {
        // Check if the photo is already in the album
        if (photos.contains(photo)) {
            return false;
        }
        
        photos.add(photo);
        return true;
    }
    
    /**
     * Removes a photo from this album.
     * 
     * @param photo the photo to remove
     * @return true if the photo was removed, false if it wasn't in the album
     */
    public boolean removePhoto(Photo photo) {
        return photos.remove(photo);
    }
    
    /**
     * Gets the number of photos in this album.
     * 
     * @return the number of photos
     */
    public int getPhotoCount() {
        return photos.size();
    }
    
    /**
     * Gets the date range of photos in this album.
     * 
     * @return an array of two Calendar objects representing the earliest and latest dates,
     *         or null if the album is empty
     */
    public Calendar[] getDateRange() {
        if (photos.isEmpty()) {
            return null;
        }
        
        Calendar earliest = null;
        Calendar latest = null;
        
        for (Photo photo : photos) {
            Calendar photoDate = photo.getDateTaken();
            
            if (earliest == null || photoDate.before(earliest)) {
                earliest = (Calendar) photoDate.clone();
            }
            
            if (latest == null || photoDate.after(latest)) {
                latest = (Calendar) photoDate.clone();
            }
        }
        
        return new Calendar[] { earliest, latest };
    }
    
    /**
     * Returns a string representation of this album.
     * 
     * @return a string representing this album
     */
    @Override
    public String toString() {
        return name + " (" + photos.size() + " photos)";
    }
} 