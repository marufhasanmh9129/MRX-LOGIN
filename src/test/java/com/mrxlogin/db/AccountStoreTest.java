package com.mrxlogin.db;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class AccountStoreTest {
    @TempDir Path temp;

    @Test void createsAndFindsAccountAndIpCount() throws Exception {
        UUID id=UUID.randomUUID();
        try(AccountStore store=new AccountStore(temp.resolve("accounts.db"),5000)) {
            assertFalse(store.exists(id));
            store.create(id,"TestPlayer","pbkdf2-sha256$100000$c2FsdA$ZGlnZXN0","127.0.0.1");
            assertTrue(store.exists(id));
            assertTrue(store.passwordHash(id).isPresent());
            assertEquals(1,store.countByIp("127.0.0.1"));
        }
    }

    @Test void updatesPasswordAndDeletesAccountByName() throws Exception {
        UUID id=UUID.randomUUID();
        try(AccountStore store=new AccountStore(temp.resolve("accounts.db"),5000)) {
            store.create(id,"TestPlayer","old-hash","127.0.0.2");
            store.updatePassword(id,"new-hash");
            assertEquals("new-hash",store.passwordHash(id).orElseThrow());
            assertTrue(store.deleteByName("testplayer"));
            assertFalse(store.exists(id));
        }
    }
}
