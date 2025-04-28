package photos.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents a tag for a photo in the Photos application.
 * Tags consist of a name-value pair, such as "location-New York" or "person-John".
 * 
 * @author Krish Patel, Darshan Surti
 */
public class Tag implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String name;
    private String value;
    
    /**
     * Creates a new tag with the given name and value.
     * 
     * @param name the tag name (e.g., "location", "person")
     * @param value the tag value (e.g., "New York", "John")
     */
    public Tag(String name, String value) {
        this.name = name;
        this.value = value;
    }
    
    /**
     * Gets the name of this tag.
     * 
     * @return the tag name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Gets the value of this tag.
     * 
     * @return the tag value
     */
    public String getValue() {
        return value;
    }
    
    /**
     * Returns a string representation of this tag.
     * 
     * @return a string in the format "name:value"
     */
    @Override
    public String toString() {
        return name + ":" + value;
    }
    
    /**
     * Overrides equals to compare tags based on their name and value.
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
        Tag other = (Tag) obj;
        return Objects.equals(name, other.name) && Objects.equals(value, other.value);
    }
    
    /**
     * Overrides hashCode based on the name and value.
     * 
     * @return the hash code for this object
     */
    @Override
    public int hashCode() {
        return Objects.hash(name, value);
    }
} 