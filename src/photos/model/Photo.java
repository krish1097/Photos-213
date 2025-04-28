package photos.model;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

/**
 * Represents a photo in the Photos application.
 * Photos have a file path, caption, date taken, and tags.
 * 
 * @author Krish Patel, Darshan Surti
 */
public class Photo implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String filePath;
    private String caption;
    private Calendar dateTaken;
    private List<Tag> tags;
    
    /**
     * Creates a new photo with the given file path.
     * The date taken is set to the last modified date of the file.
     * 
     * @param filePath the path to the photo file
     */
    public Photo(String filePath) {
        this.filePath = filePath;
        this.caption = "";
        this.tags = new ArrayList<>();
        
        // Set date taken to the last modified date of the file
        File file = new File(filePath);
        if (file.exists()) {
            this.dateTaken = Calendar.getInstance();
            this.dateTaken.setTimeInMillis(file.lastModified());
            this.dateTaken.set(Calendar.MILLISECOND, 0); // Clear milliseconds for accurate comparisons
        } else {
            this.dateTaken = Calendar.getInstance();
        }
    }
    
    /**
     * Gets the file path of this photo.
     * 
     * @return the file path
     */
    public String getFilePath() {
        return filePath;
    }
    
    /**
     * Gets the caption of this photo.
     * 
     * @return the caption
     */
    public String getCaption() {
        return caption;
    }
    
    /**
     * Sets the caption of this photo.
     * 
     * @param caption the new caption
     */
    public void setCaption(String caption) {
        this.caption = caption;
    }
    
    /**
     * Gets the date this photo was taken.
     * 
     * @return the date taken
     */
    public Calendar getDateTaken() {
        return dateTaken;
    }
    
    /**
     * Gets the list of tags for this photo.
     * 
     * @return the list of tags
     */
    public List<Tag> getTags() {
        return tags;
    }
    
    /**
     * Adds a tag to this photo.
     * 
     * @param tag the tag to add
     * @return true if the tag was added, false if a tag with the same name and value already exists
     */
    public boolean addTag(Tag tag) {
        // Check for duplicate tags
        for (Tag existingTag : tags) {
            if (existingTag.getName().equals(tag.getName()) && 
                existingTag.getValue().equals(tag.getValue())) {
                return false;
            }
        }
        
        tags.add(tag);
        return true;
    }
    
    /**
     * Removes a tag from this photo.
     * 
     * @param tag the tag to remove
     * @return true if the tag was removed, false if it wasn't found
     */
    public boolean removeTag(Tag tag) {
        return tags.remove(tag);
    }
    
    /**
     * Determines if this photo has a tag with the given name and value.
     * 
     * @param tagName the tag name
     * @param tagValue the tag value
     * @return true if the photo has the tag, false otherwise
     */
    public boolean hasTag(String tagName, String tagValue) {
        for (Tag tag : tags) {
            if (tag.getName().equals(tagName) && tag.getValue().equals(tagValue)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Finds all tags with the given name.
     * 
     * @param tagName the tag name to search for
     * @return a list of tags with the given name
     */
    public List<Tag> findTagsByName(String tagName) {
        List<Tag> matchingTags = new ArrayList<>();
        for (Tag tag : tags) {
            if (tag.getName().equals(tagName)) {
                matchingTags.add(tag);
            }
        }
        return matchingTags;
    }
    
    /**
     * Overrides equals to compare photos based on their file path.
     * 
     * @param obj the object to compare with
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Photo other = (Photo) obj;
        return Objects.equals(filePath, other.filePath);
    }
    
    /**
     * Overrides hashCode based on the file path.
     * 
     * @return the hash code for this object
     */
    @Override
    public int hashCode() {
        return Objects.hash(filePath);
    }
} 