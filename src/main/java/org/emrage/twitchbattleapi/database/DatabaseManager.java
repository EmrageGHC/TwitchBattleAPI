package org.emrage.twitchbattleapi.database;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.emrage.twitchbattleapi.config.DatabaseConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages MongoDB database connections and operations
 */
public class DatabaseManager {
    private String connectionString;
    private MongoClient mongoClient;
    private MongoDatabase database;
    private final Logger logger = Logger.getLogger("TwitchBattleAPI");

    /**
     * Create a new database manager with default connection string
     */
    public DatabaseManager() {
        this.connectionString = DatabaseConfig.getConnectionString();
    }

    /**
     * Create a new database manager
     * @param connectionString MongoDB connection string
     */
    public DatabaseManager(String connectionString) {
        this.connectionString = connectionString;
    }

    /**
     * Set a custom connection string
     * @param connectionString MongoDB connection string
     */
    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }

    /**
     * Connect to the database
     */
    public void connect() {
        try {
            // Set up MongoDB connection
            ConnectionString connString = new ConnectionString(connectionString);
            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(connString)
                    .build();

            mongoClient = MongoClients.create(settings);
            database = mongoClient.getDatabase(DatabaseConfig.getDatabaseName());

            logger.info("Successfully connected to MongoDB database");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to connect to MongoDB database", e);
        }
    }

    /**
     * Disconnect from the database
     */
    public void disconnect() {
        try {
            if (mongoClient != null) {
                mongoClient.close();
                logger.info("Successfully disconnected from MongoDB database");
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to disconnect from MongoDB database", e);
        }
    }

    /**
     * Create collections needed for this API
     */
    public void createTables() {
        try {
            // Check if collections exist, create them if not
            boolean hasTeams = false;
            boolean hasPlayers = false;
            boolean hasPoints = false;

            for (String name : database.listCollectionNames()) {
                if (name.equals("teams")) hasTeams = true;
                if (name.equals("players")) hasPlayers = true;
                if (name.equals("points")) hasPoints = true;
            }

            if (!hasTeams) database.createCollection("teams");
            if (!hasPlayers) database.createCollection("players");
            if (!hasPoints) database.createCollection("points");

            // Create indexes for better performance
            database.getCollection("teams").createIndex(new Document("name", 1));
            database.getCollection("players").createIndex(new Document("uuid", 1));
            database.getCollection("players").createIndex(new Document("team_id", 1));
            database.getCollection("points").createIndex(new Document("team_id", 1));
            database.getCollection("points").createIndex(new Document("player_uuid", 1));

            logger.info("Successfully initialized MongoDB collections and indices");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to initialize MongoDB collections", e);
        }
    }

    /**
     * Find documents in a collection
     * @param collection Collection name
     * @param filter Filter document
     * @return List of matching documents
     */
    public List<Document> find(String collection, Document filter) {
        try {
            List<Document> results = new ArrayList<>();
            MongoCollection<Document> coll = database.getCollection(collection);
            coll.find(filter).into(results);
            return results;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error executing find query", e);
            return new ArrayList<>();
        }
    }

    /**
     * Find a single document in a collection
     * @param collection Collection name
     * @param filter Filter document
     * @return Matching document or null
     */
    public Document findOne(String collection, Document filter) {
        try {
            return database.getCollection(collection).find(filter).first();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error executing findOne query", e);
            return null;
        }
    }

    /**
     * Insert a document into a collection
     * @param collection Collection name
     * @param document Document to insert
     * @return True if successful, false otherwise
     */
    public boolean insertOne(String collection, Document document) {
        try {
            database.getCollection(collection).insertOne(document);
            return true;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error inserting document", e);
            return false;
        }
    }

    /**
     * Update a document in a collection
     * @param collection Collection name
     * @param filter Filter to find the document
     * @param update Update operations
     * @return True if successful, false otherwise
     */
    public boolean updateOne(String collection, Document filter, Document update) {
        try {
            database.getCollection(collection).updateOne(filter, new Document("$set", update));
            return true;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error updating document", e);
            return false;
        }
    }

    /**
     * Delete a document from a collection
     * @param collection Collection name
     * @param filter Filter to find the document
     * @return True if successful, false otherwise
     */
    public boolean deleteOne(String collection, Document filter) {
        try {
            database.getCollection(collection).deleteOne(filter);
            return true;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error deleting document", e);
            return false;
        }
    }

    /**
     * Execute SQL-style update (compatibility with old code)
     * @param sql The SQL statement with placeholders (ignored)
     * @param params The parameters to replace the placeholders (ignored)
     * @return Always false as SQL operations are not supported
     * @deprecated Use MongoDB native methods instead
     */
    @Deprecated
    public boolean executeUpdate(String sql, Object... params) {
        logger.warning("SQL operations are not supported with MongoDB. Use MongoDB native methods instead.");
        return false;
    }

    /**
     * Execute SQL-style query (compatibility with old code)
     * @param sql The SQL statement with placeholders (ignored)
     * @param params The parameters to replace the placeholders (ignored)
     * @return An empty list as SQL operations are not supported
     * @deprecated Use MongoDB native methods instead
     */
    @Deprecated
    public List<Document> executeQuery(String sql, Object... params) {
        logger.warning("SQL operations are not supported with MongoDB. Use MongoDB native methods instead.");
        return new ArrayList<>();
    }

    /**
     * Execute SQL-style update (compatibility with old code)
     * @param sql The SQL statement (ignored)
     * @return Always false as SQL operations are not supported
     * @deprecated Use MongoDB native methods instead
     */
    @Deprecated
    public boolean executeUpdate(String sql) {
        logger.warning("SQL operations are not supported with MongoDB. Use MongoDB native methods instead.");
        return false;
    }

    /**
     * Execute SQL-style query (compatibility with old code)
     * @param sql The SQL statement (ignored)
     * @return An empty list as SQL operations are not supported
     * @deprecated Use MongoDB native methods instead
     */
    @Deprecated
    public List<Document> executeQuery(String sql) {
        logger.warning("SQL operations are not supported with MongoDB. Use MongoDB native methods instead.");
        return new ArrayList<>();
    }

    /**
     * Get the MongoDB client
     * @return The MongoDB client
     */
    public MongoClient getMongoClient() {
        return mongoClient;
    }

    /**
     * Get the MongoDB database
     * @return The MongoDB database
     */
    public MongoDatabase getDatabase() {
        return database;
    }

    /**
     * Get MongoDB connection as generic Object (for compatibility)
     * @return The MongoDB client as Object
     */
    public Object getConnection() {
        return mongoClient;
    }
}