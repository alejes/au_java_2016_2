import models.torrent.TorrentClient;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;

public class TorrentServerClientTest {
    private static String SERVER_HOST = "127.0.0.1";

    @Test
    public void simpleStart() throws Exception {
        TorrentServerCmd.ServerManager serverManager = new TorrentServerCmd.ServerManager(true);

        serverManager.start();

        TorrentClient tc = new TorrentClientImpl(SERVER_HOST, 0, true);
        serverManager.shutdown();
        tc.shutdown();
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

        serverManager.shutdown();
        tc2.shutdown();
        tc1.shutdown();
        System.gc();
    }

    @Test
    public void fileSend() throws Exception {
        System.gc();
        TorrentServerCmd.ServerManager serverManager = new TorrentServerCmd.ServerManager(true);
        serverManager.start();

        TorrentClient tc1 = new TorrentClientImpl(SERVER_HOST, 0, true);
        TorrentClient tc2 = new TorrentClientImpl(SERVER_HOST, 1, true);
        Thread.sleep(2000);

        assertEquals(0, tc1.distributedFiles().size());
        assertEquals(0, tc2.distributedFiles().size());

        File flGradlew = new File("gradlew.bat");
        tc1.registerFile(flGradlew);
        assertEquals(1, tc1.distributedFiles().size());
        assertEquals(0, tc2.distributedFiles().size());
        tc1.forceUpdate();
        assertEquals(1, tc1.listFiles().size());
        assertEquals(1, tc2.listFiles().size());


        serverManager.shutdown();
        tc2.shutdown();
        tc1.shutdown();
        System.gc();
    }
}