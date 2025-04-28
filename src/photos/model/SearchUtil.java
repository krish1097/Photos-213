package photos.model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Utility class for searching photos by date range and tags.
 * 
 * @author Krish Patel, Darshan Surti
 */
public class SearchUtil {
    
    /**
     * Search for photos within a date range.
     * 
     * @param albums the list of albums to search in
     * @param startDate the start date of the range (inclusive)
     * @param endDate the end date of the range (inclusive)
     * @return a list of photos that were taken within the date range
     */
    public static List<Photo> searchByDateRange(List<Album> albums, Calendar startDate, Calendar endDate) {
        List<Photo> results = new ArrayList<>();
        
        // Clear milliseconds for accurate comparisons
        startDate.set(Calendar.MILLISECOND, 0);
        endDate.set(Calendar.MILLISECOND, 0);
        
        for (Album album : albums) {
            for (Photo photo : album.getPhotos()) {
                Calendar photoDate = photo.getDateTaken();
                
                // Check if photo date is within range (inclusive)
                if ((photoDate.equals(startDate) || photoDate.after(startDate)) && 
                    (photoDate.equals(endDate) || photoDate.before(endDate))) {
                    
                    // Add the photo if it's not already in the results
                    if (!results.contains(photo)) {
                        results.add(photo);
                    }
                }
            }
        }
        
        return results;
    }
    
    /**
     * Search for photos with a specific tag.
     * 
     * @param albums the list of albums to search in
     * @param tagName the tag name to search for
     * @param tagValue the tag value to search for
     * @return a list of photos that have the specified tag
     */
    public static List<Photo> searchByTag(List<Album> albums, String tagName, String tagValue) {
        List<Photo> results = new ArrayList<>();
        
        for (Album album : albums) {
            for (Photo photo : album.getPhotos()) {
                if (photo.hasTag(tagName, tagValue)) {
                    // Add the photo if it's not already in the results
                    if (!results.contains(photo)) {
                        results.add(photo);
                    }
                }
            }
        }
        
        return results;
    }
    
    /**
     * Search for photos that match both tag criteria (AND operation).
     * 
     * @param albums the list of albums to search in
     * @param tag1Name the first tag name
     * @param tag1Value the first tag value
     * @param tag2Name the second tag name
     * @param tag2Value the second tag value
     * @return a list of photos that have both specified tags
     */
    public static List<Photo> searchByTagsAnd(List<Album> albums, 
                                               String tag1Name, String tag1Value,
                                               String tag2Name, String tag2Value) {
        List<Photo> results = new ArrayList<>();
        
        for (Album album : albums) {
            for (Photo photo : album.getPhotos()) {
                if (photo.hasTag(tag1Name, tag1Value) && photo.hasTag(tag2Name, tag2Value)) {
                    // Add the photo if it's not already in the results
                    if (!results.contains(photo)) {
                        results.add(photo);
                    }
                }
            }
        }
        
        return results;
    }
    
    /**
     * Search for photos that match either tag criteria (OR operation).
     * 
     * @param albums the list of albums to search in
     * @param tag1Name the first tag name
     * @param tag1Value the first tag value
     * @param tag2Name the second tag name
     * @param tag2Value the second tag value
     * @return a list of photos that have either of the specified tags
     */
    public static List<Photo> searchByTagsOr(List<Album> albums, 
                                              String tag1Name, String tag1Value,
                                              String tag2Name, String tag2Value) {
        List<Photo> results = new ArrayList<>();
        
        for (Album album : albums) {
            for (Photo photo : album.getPhotos()) {
                if (photo.hasTag(tag1Name, tag1Value) || photo.hasTag(tag2Name, tag2Value)) {
                    // Add the photo if it's not already in the results
                    if (!results.contains(photo)) {
                        results.add(photo);
                    }
                }
            }
        }
        
        return results;
    }
} 