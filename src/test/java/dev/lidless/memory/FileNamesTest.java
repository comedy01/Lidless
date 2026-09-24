package dev.lidless.memory;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class FileNamesTest {
    @Test
    void addressesBecomeSafeFileNames() {
        assertEquals("play.example.org_25565", FileNames.safe("Play.Example.org:25565"));
        assertEquals("___1_", FileNames.safe("[::1]"));
    }
}
