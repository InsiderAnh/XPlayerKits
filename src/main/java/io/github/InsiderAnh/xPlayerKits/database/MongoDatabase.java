package io.github.InsiderAnh.xPlayerKits.database;

import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.UpdateOptions;
import io.github.InsiderAnh.xPlayerKits.PlayerKits;
import io.github.InsiderAnh.xPlayerKits.data.PlayerKitData;
import io.github.InsiderAnh.xPlayerKits.superclass.Database;
import io.github.InsiderAnh.xPlayerKits.utils.XPKUtils;
import org.bson.Document;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MongoDatabase extends Database {

    private final PlayerKits playerKits = PlayerKits.getInstance();
    private MongoClient mongoClient;
    private MongoCollection<Document> collection;

    @Override
    public void connect() {
        String connectionString = "mongodb://" + playerKits.getConfig().getString("databases.mongodb.user") + ":" + playerKits.getConfig().getString("databases.mongodb.password") + "@" + playerKits.getConfig().getString("databases.mongodb.host") + ":" + playerKits.getConfig().getInt("databases.mongodb.port");

        try {
            this.mongoClient = MongoClients.create(new ConnectionString(connectionString));
            this.collection = mongoClient.getDatabase(playerKits.getConfig().getString("databases.mongodb.database", "xPlayerKits")).getCollection("player_kits");
            this.collection.createIndex(new Document("uuid", 1), new IndexOptions().unique(true));
            this.collection.createIndex(new Document("name", 1));
            playerKits.getLogger().info("Connected to Mongo database correctly.");
        } catch (Exception exception) {
            exception.printStackTrace();
            playerKits.getLogger().warning("Error on connect to Mongo database.");
        }
    }

    @Override
    public void close() {
        this.mongoClient.close();
    }

    @Override
    public CompletableFuture<PlayerKitData> getPlayerDataByName(String name) {
        CompletableFuture<PlayerKitData> completableFuture = new CompletableFuture<>();
        playerKits.getExecutor().execute(() -> {
            Document documentFound = collection.find(new Document("name", name)).first();
            PlayerKitData playerKitData;
            if (documentFound != null) {
                playerKitData = XPKUtils.getGson().fromJson(documentFound.toJson(XPKUtils.getWriterSettings()), PlayerKitData.class);
                completableFuture.complete(playerKitData);
            } else {
                completableFuture.complete(null);
            }
        });
        return completableFuture;
    }

    @Override
    public CompletableFuture<PlayerKitData> getPlayerData(UUID uuid, String name) {
        CompletableFuture<PlayerKitData> completableFuture = new CompletableFuture<>();
        playerKits.getExecutor().execute(() -> completableFuture.complete(getSyncPlayerData(uuid, name)));
        return completableFuture;
    }

    @Override
    public CompletableFuture<Boolean> loadPlayerData(UUID uuid, String name) {
        CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();
        playerKits.getExecutor().execute(() -> {
            Document documentFound = collection.find(new Document("uuid", uuid.toString())).first();
            PlayerKitData playerKitData;
            boolean firstJoin;
            if (documentFound != null) {
                firstJoin = false;
                playerKitData = XPKUtils.getGson().fromJson(documentFound.toJson(XPKUtils.getWriterSettings()), PlayerKitData.class);
            } else {
                firstJoin = true;
                playerKitData = new PlayerKitData(uuid, name);
                Document document = Document.parse(XPKUtils.getGson().toJson(playerKitData, PlayerKitData.class));
                collection.insertOne(document);
            }
            cachedPlayerKits.put(uuid, playerKitData);
            completableFuture.complete(firstJoin);
        });
        return completableFuture;
    }

    @Override
    public PlayerKitData getSyncPlayerData(UUID uuid, String name) {
        Document documentFound = collection.find(new Document("uuid", uuid.toString())).first();
        PlayerKitData playerKitData;
        if (documentFound != null) {
            playerKitData = XPKUtils.getGson().fromJson(documentFound.toJson(XPKUtils.getWriterSettings()), PlayerKitData.class);
        } else {
            playerKitData = new PlayerKitData(uuid, name);
            Document document = Document.parse(XPKUtils.getGson().toJson(playerKitData, PlayerKitData.class));
            collection.insertOne(document);
        }
        cachedPlayerKits.put(uuid, playerKitData);
        return playerKitData;
    }

    @Override
    public void updatePlayerData(UUID uuid) {
        PlayerKitData playerKitData = cachedPlayerKits.get(uuid);
        if (playerKitData == null) return;

        Document document = Document.parse(XPKUtils.getGson().toJson(playerKitData, PlayerKitData.class));
        collection.updateOne(new Document("uuid", uuid.toString()), new Document("$set", document), new UpdateOptions().upsert(true));
    }

}