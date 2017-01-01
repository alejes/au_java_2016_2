import client.TorrentClientImpl;
import models.torrent.TorrentClient;
import org.junit.Test;
import server.TorrentServerCmd;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

public class TorrentServerClientTest {
    private static final String SERVER_HOST = "127.0.0.1";

    @Test
    public void simpleStart() throws Exception {
        TorrentServerCmd.ServerManager serverManager = new TorrentServerCmd.ServerManager(true);

        serverManager.start();

        TorrentClient tc = new TorrentClientImpl(SERVER_HOST, 0, true);
        serverManager.close();
        tc.close();
        System.gc();
    }

    @Test
    public void initFilesCount() throws Exception {
        TorrentServerCmd.ServerManager serverManager = new TorrentServerCmd.ServerManager(true);
        serverManager.start();

        TorrentClient tc1 = new TorrentClientImpl(SERVER_HOST, 0, true);
        TorrentClient tc2 = new TorrentClientImpl(SERVER_HOST, 1, true);
        Thread.sleep(1000);

        assertEquals(0, tc1.distributedFiles().size());
        assertEquals(0, tc2.distributedFiles().size());

        assertEquals(0, tc1.listFiles().size());
        assertEquals(0, tc2.listFiles().size());

        serverManager.close();
        tc2.close();
        tc1.close();
        System.gc();
    }

    @Test
    public void fileSend() throws Exception {
        System.gc();
        TorrentServerCmd.ServerManager serverManager = new TorrentServerCmd.ServerManager(true);
        serverManager.start();

        TorrentClient tc1 = new TorrentClientImpl(SERVER_HOST, 0, true);
        TorrentClient tc2 = new TorrentClientImpl(SERVER_HOST, 1, true);

        assertEquals(0, tc1.distributedFiles().size());
        assertEquals(0, tc2.distributedFiles().size());

        File flGradlewBat = new File("gradlew.bat");
        tc1.registerFile(flGradlewBat);
        assertEquals(1, tc1.distributedFiles().size());
        assertEquals(0, tc2.distributedFiles().size());
        tc1.forceUpdate();
        assertEquals(1, tc1.listFiles().size());
        assertEquals(1, tc2.listFiles().size());
        assertEquals("0\tgradlew.bat\t2404\t[OK]", tc1.distributedFiles().get(0).toString());
        assertEquals("0\tgradlew.bat\t2404\t[0/241]", tc2.listFiles().get(0).toString());

        tc2.addGetTask(0, "gradlewDownload.bat");
        Thread.sleep(5000);
        assertEquals(1, tc2.distributedFiles().size());
        assertEquals("0\tgradlew.bat\t2404\t[OK]", tc1.distributedFiles().get(0).toString());
        assertEquals(Files.readAllLines(Paths.get("gradlew.bat")).stream().reduce(String::concat),
                Files.readAllLines(Paths.get("gradlewDownload.bat")).stream().reduce(String::concat));

        File flGradlew = new File("gradlew");
        tc2.registerFile(flGradlew);
        tc2.forceUpdate();
        assertEquals(2, tc1.listFiles().size());
        assertEquals(2, tc2.listFiles().size());
        tc1.addGetTask(1, "gradlewPureDownload.bat");
        Thread.sleep(5000);
        assertEquals(Files.readAllLines(Paths.get("gradlew")).stream().reduce(String::concat),
                Files.readAllLines(Paths.get("gradlewPureDownload.bat")).stream().reduce(String::concat));


        serverManager.close();
        tc2.close();
        tc1.close();
        System.gc();
    }
}