package photos.model;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages users in the Photos application and handles persistence of user data.
 * 
 * @author Krish Patel, Darshan Surti
 */
public class UserManager implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final String DATA_DIR = "data";
    private static final String USERS_FILE = DATA_DIR + File.separator + "users.dat";
    
    private Map<String, User> users;
    private static UserManager instance;
    
    /**
     * Private constructor for singleton pattern.
     * Initializes the users map.
     */
    private UserManager() {
        users = new HashMap<>();
    }
    
    /**
     * Gets the singleton instance of UserManager.
     * 
     * @return the UserManager instance
     */
    public static UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
            instance.loadUsers();
            
            // If no users exist, create the admin and stock users
            if (instance.users.isEmpty()) {
                instance.createAdminUser();
                instance.createStockUser();
            }
        }
        return instance;
    }
    
    /**
     * Creates the admin user if it doesn't exist.
     */
    private void createAdminUser() {
        User adminUser = new User("admin");
        users.put("admin", adminUser);
    }
    
    /**
     * Creates the stock user with a stock album if it doesn't exist.
     */
    private void createStockUser() {
        User stockUser = new User("stock");
        Album stockAlbum = new Album("stock");
        stockUser.addAlbum(stockAlbum);
        users.put("stock", stockUser);
    }
    
    /**
     * Gets a user by username.
     * 
     * @param username the username to look up
     * @return the User object, or null if not found
     */
    public User getUser(String username) {
        return users.get(username);
    }
    
    /**
     * Checks if a user with the given username exists.
     * 
     * @param username the username to check
     * @return true if the user exists, false otherwise
     */
    public boolean userExists(String username) {
        return users.containsKey(username);
    }
    
    /**
     * Adds a new user.
     * 
     * @param username the username for the new user
     * @return the newly created User object, or null if a user with that username already exists
     */
    public User addUser(String username) {
        if (userExists(username)) {
            return null;
        }
        
        User newUser = new User(username);
        users.put(username, newUser);
        saveUsers();
        
        return newUser;
    }
    
    /**
     * Removes a user.
     * 
     * @param username the username of the user to remove
     * @return true if the user was removed, false if the user doesn't exist or is the admin user
     */
    public boolean removeUser(String username) {
        if (!userExists(username) || username.equals("admin")) {
            return false;
        }
        
        users.remove(username);
        saveUsers();
        
        return true;
    }
    
    /**
     * Gets a list of all usernames.
     * 
     * @return a list of all usernames
     */
    public List<String> getAllUsernames() {
        return new ArrayList<>(users.keySet());
    }
    
    /**
     * Loads users from the persistent storage.
     */
    @SuppressWarnings("unchecked")
    private void loadUsers() {
        File file = new File(USERS_FILE);
        if (!file.exists()) {
            return;
        }
        
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            users = (Map<String, User>) ois.readObject();
        } catch (Exception e) {
            System.err.println("Error loading users: " + e.getMessage());
            users = new HashMap<>();
        }
    }
    
    /**
     * Saves users to persistent storage.
     */
    public void saveUsers() {
        File dir = new File(DATA_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(USERS_FILE))) {
            oos.writeObject(users);
        } catch (IOException e) {
            System.err.println("Error saving users: " + e.getMessage());
        }
    }
} 