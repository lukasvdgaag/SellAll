package net.gcnt.sellall.files.logs;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.gcnt.sellall.SellAll;
import net.gcnt.sellall.items.Item;
import net.gcnt.sellall.items.ItemType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class MySQLLog {

    private final SellAll plugin;
    private HikariDataSource ds;

    public MySQLLog(SellAll plugin) {
        this.plugin = plugin;
        setup();
    }

    public Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

    public void setup() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + plugin.getCfg().getString("mysql.host") + ":" + plugin.getCfg().getInt("mysql.port") + "/" + plugin.getCfg().getString("mysql.database"));
        config.setUsername(plugin.getCfg().getString("mysql.username"));
        config.setPassword(plugin.getCfg().getString("mysql.password"));
        config.setMinimumIdle(1);
        config.setMaximumPoolSize(50);
        config.setConnectionTimeout(4000);
        ds = new HikariDataSource(config);

        try (Connection connection = getConnection()) {
            connection.createStatement().executeUpdate("CREATE DATABASE IF NOT EXISTS " + plugin.getCfg().getString("mysql.database"));

            connection.createStatement().executeUpdate("""
                    CREATE TABLE IF NOT EXISTS `sellall_logs` (
                      `id` int(11) NOT NULL AUTO_INCREMENT,
                      `player` varchar(36) NOT NULL,
                      `material_type` varchar(20) NOT NULL,
                      `material` varchar(36) NOT NULL,
                      `amount` int(5) NOT NULL,
                      `price` DECIMAL(10,2) NOT NULL,
                      `tax` int(3) NOT NULL,
                      `date` datetime DEFAULT CURRENT_TIMESTAMP NOT NULL,
                      PRIMARY KEY (`id`)
                    );""");
        } catch (SQLException e) {
            plugin.getLogger().severe("SellAll caught an error while executing a MySQL statement.");
            e.printStackTrace();
        }
    }

    public void logSell(PlayerLog log, ItemType type, String material, int amount, double price, double tax) {
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO sellall_logs(player, material_type, material, amount, price, tax) VALUES (?, ?, ?, ?, ?, ?)");
            statement.setString(1, log.getUuid().toString());
            statement.setString(2, type.name());
            statement.setString(3, material);
            statement.setInt(4, amount);
            statement.setDouble(5, price);
            statement.setDouble(6, tax);
            statement.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("SellAll caught an error while executing a MySQL statement.");
            e.printStackTrace();
        }
    }

    /**
     * Get the amount of items sold by a player in the last 24 hours.
     *
     * @param player The player to get the sell count for.
     * @param item   The item to get the sell count for.
     * @return The amount of times the player has sold the item
     * @deprecated Use {@link PlayerLog#getSellCount(Item)} instead.
     */
    @Deprecated
    public int getSellCount(UUID player, Item item) {
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT SUM(amount) FROM sellall_logs WHERE player = ? AND material_type = ? AND material = ? AND date > now() - interval 24 hour");
            statement.setString(1, player.toString());
            statement.setString(2, item.getType().name());
            statement.setString(3, item.getType() == ItemType.BUKKIT ? item.getItem().name() : item.getExternalId());

            final ResultSet resultSet = statement.executeQuery();
            return (resultSet.next()) ? resultSet.getInt(1) : 0;
        } catch (SQLException e) {
            plugin.getLogger().severe("SellAll caught an error while executing a MySQL statement.");
            e.printStackTrace();
        }
        return 0;
    }

    public void initializeLogs(PlayerLog log) {
        log.clear();
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT SUM(amount) AS amount, material_type, material FROM sellall_logs WHERE player = ? AND date > now() - interval 24 hour GROUP BY material_type, material ");
            statement.setString(1, log.getUuid().toString());

            final ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                int amount = resultSet.getInt("amount");
                ItemType type = ItemType.valueOf(resultSet.getString("material_type"));
                String material = resultSet.getString("material");

                String id = type == ItemType.BUKKIT ? material : type.name() + "-" + material;

                Item item = Item.fromId(plugin.getItemFile(), id);
                if (item == null) {
                    plugin.getLogger().warning("SellAll could not find an item with id " + id + " in the item file.");
                    continue;
                }

                log.setSellCount(item, amount);
            }

        } catch (SQLException e) {
            plugin.getLogger().severe("SellAll caught an error while executing a MySQL statement.");
        }
    }

}
