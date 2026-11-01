package com.mrxlogin.db;

import java.nio.file.Path;
import java.sql.*;
import java.util.Optional;
import java.util.UUID;

public final class AccountStore implements AutoCloseable {
    private final Connection connection;
    public AccountStore(Path file, int busyTimeoutMillis) throws SQLException {
        this.connection = DriverManager.getConnection("jdbc:sqlite:" + file.toAbsolutePath());
        try (Statement s = connection.createStatement()) {
            s.execute("PRAGMA busy_timeout=" + Math.max(0, Math.min(busyTimeoutMillis, 60000)));
            s.execute("PRAGMA foreign_keys=ON");
            s.execute("PRAGMA journal_mode=WAL");
            s.executeUpdate("CREATE TABLE IF NOT EXISTS accounts (uuid TEXT PRIMARY KEY, name TEXT NOT NULL, password_hash TEXT NOT NULL, email TEXT, email_verified INTEGER NOT NULL DEFAULT 0, last_ip TEXT, last_world TEXT, last_x REAL, last_y REAL, last_z REAL)");
        }
    }
    public synchronized boolean exists(UUID uuid) throws SQLException {
        try (PreparedStatement p=connection.prepareStatement("SELECT 1 FROM accounts WHERE uuid=?")) { p.setString(1,uuid.toString()); try(ResultSet r=p.executeQuery()){return r.next();} }
    }
    public synchronized Optional<String> passwordHash(UUID uuid) throws SQLException {
        try (PreparedStatement p=connection.prepareStatement("SELECT password_hash FROM accounts WHERE uuid=?")) { p.setString(1,uuid.toString()); try(ResultSet r=p.executeQuery()){return r.next()?Optional.of(r.getString(1)):Optional.empty();} }
    }
    public synchronized void create(UUID uuid,String name,String hash,String ip) throws SQLException {
        try(PreparedStatement p=connection.prepareStatement("INSERT INTO accounts(uuid,name,password_hash,last_ip) VALUES(?,?,?,?)")){p.setString(1,uuid.toString());p.setString(2,name);p.setString(3,hash);p.setString(4,ip);p.executeUpdate();}
    }
    public synchronized int countByIp(String ip) throws SQLException {
        try (PreparedStatement p=connection.prepareStatement("SELECT COUNT(*) FROM accounts WHERE last_ip=?")) { p.setString(1, ip); try (ResultSet r=p.executeQuery()) { return r.next() ? r.getInt(1) : 0; } }
    }
    public synchronized boolean deleteByName(String name) throws SQLException {
        try (PreparedStatement p=connection.prepareStatement("DELETE FROM accounts WHERE name=? COLLATE NOCASE")) { p.setString(1, name); return p.executeUpdate() > 0; }
    }
    public synchronized void updateLastLocation(UUID uuid, String world, double x, double y, double z) throws SQLException {
        try (PreparedStatement p=connection.prepareStatement("UPDATE accounts SET last_world=?,last_x=?,last_y=?,last_z=? WHERE uuid=?")) { p.setString(1,world); p.setDouble(2,x); p.setDouble(3,y); p.setDouble(4,z); p.setString(5,uuid.toString()); p.executeUpdate(); }
    }
    public synchronized void updatePassword(UUID uuid,String hash) throws SQLException {
        try(PreparedStatement p=connection.prepareStatement("UPDATE accounts SET password_hash=? WHERE uuid=?")){p.setString(1,hash);p.setString(2,uuid.toString());p.executeUpdate();}
    }
    public synchronized void setEmail(UUID uuid,String email) throws SQLException {
        try(PreparedStatement p=connection.prepareStatement("UPDATE accounts SET email=?,email_verified=0 WHERE uuid=?")){p.setString(1,email);p.setString(2,uuid.toString());p.executeUpdate();}
    }
    public synchronized Optional<String> email(UUID uuid) throws SQLException {
        try(PreparedStatement p=connection.prepareStatement("SELECT email FROM accounts WHERE uuid=?")){p.setString(1,uuid.toString());try(ResultSet r=p.executeQuery()){return r.next()?Optional.ofNullable(r.getString(1)):Optional.empty();}}
    }
    @Override public synchronized void close() throws SQLException { connection.close(); }
}
