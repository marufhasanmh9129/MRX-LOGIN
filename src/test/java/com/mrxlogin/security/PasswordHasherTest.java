package com.mrxlogin.security;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PasswordHasherTest {
    @Test void hashVerifiesCorrectPassword() throws Exception {
        char[] password="correct horse battery staple".toCharArray();
        String encoded=PasswordHasher.hash(password,100000);
        assertTrue(PasswordHasher.verify(password,encoded));
    }
    @Test void rejectsWrongPassword() throws Exception {
        String encoded=PasswordHasher.hash("right-password".toCharArray(),100000);
        assertFalse(PasswordHasher.verify("wrong-password".toCharArray(),encoded));
    }
    @Test void usesDifferentSaltForSamePassword() throws Exception {
        String first=PasswordHasher.hash("same-password".toCharArray(),100000);
        String second=PasswordHasher.hash("same-password".toCharArray(),100000);
        assertNotEquals(first,second);
    }
    @Test void rejectsMalformedHash() throws Exception {
        assertFalse(PasswordHasher.verify("anything".toCharArray(),"not-a-valid-hash"));
    }
}
