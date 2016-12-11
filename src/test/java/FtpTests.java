import models.requests.GetRequest;
import models.requests.ListRequest;
import models.responses.GetResponse;
import models.responses.ListResponse;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class FtpTests {
    private static final int portNumberMod = 30000;
    private static final Random rnd = new Random();
    private static final int MAX_CLIENT_COUNT = 100;

    @Test
    public void connect() throws Exception {
        int serverPort = portNumberMod + (rnd.nextInt() % portNumberMod);
        FtpServerImpl ftpServer = new FtpServerImpl(serverPort);
        FtpClientImpl ftpClient = new FtpClientImpl(serverPort);
        try {
            ftpServer.serverStart();
            ftpClient.connect();
        } finally {
            try {
                ftpClient.disconnect();
            } finally {
                ftpServer.serverStop();
            }
        }
    }

    @Test
    public void multiConnect() throws Exception {
        int serverPort = portNumberMod + (rnd.nextInt() % portNumberMod);
        FtpServerImpl ftpServer = new FtpServerImpl(serverPort);
        FtpClientImpl[] ftpClient = new FtpClientImpl[MAX_CLIENT_COUNT];
        try {
            ftpServer.serverStart();
            for (int i = 0; i < MAX_CLIENT_COUNT; ++i) {
                ftpClient[i] = new FtpClientImpl(serverPort);
                ftpClient[i].connect();
            }
        } finally {
            try {
                for (int i = 0; i < MAX_CLIENT_COUNT; ++i) {
                    if (ftpClient[i] != null) {
                        ftpClient[i].disconnect();
                    }
                }
            } finally {
                ftpServer.serverStop();
            }
        }
    }

    @Test
    public void executeList() throws Exception {
        int serverPort = portNumberMod + (rnd.nextInt() % portNumberMod);
        FtpServerImpl ftpServer = new FtpServerImpl(serverPort);
        FtpClientImpl ftpClient = new FtpClientImpl(serverPort);
        try {
            ftpServer.serverStart();
            ftpClient.connect();
            ListResponse get = ftpClient.executeList(new ListRequest("./src/test/resources"));
            assertEquals(">\\src\\test\\resources\\listDir", get.toString());
            get = ftpClient.executeList(new ListRequest("./src/test/resources/listDir"));
            assertEquals("\\src\\test\\resources\\listDir\\emptyFile.txt\n" +
                    "\\src\\test\\resources\\listDir\\file1.txt\n" +
                    "\\src\\test\\resources\\listDir\\file2.txt" +
                    "", get.toString());
        } finally {
            ftpClient.disconnect();
            ftpServer.serverStop();
        }
    }

    @Test
    public void executeGet() throws Exception {
        int serverPort = portNumberMod + (rnd.nextInt() % portNumberMod);
        FtpServerImpl ftpServer = new FtpServerImpl(serverPort);
        FtpClientImpl ftpClient = new FtpClientImpl(serverPort);
        try {
            ftpServer.serverStart();
            ftpClient.connect();
            GetResponse get = ftpClient.executeGet(new GetRequest("."));
            get.toFile("testFile.txt");
            List<String> allLines = Files.readAllLines(Paths.get("testFile.txt"));
            assertTrue(allLines.isEmpty());


            get = ftpClient.executeGet(new GetRequest("./src/test/resources/listDir/file1.txt"));
            get.toFile("testFile.txt");
            allLines = Files.readAllLines(Paths.get("testFile.txt"));
            assertFalse(allLines.isEmpty());
            assertThat(Arrays.asList("frefre",
                    "frefref",
                    "refrefre",
                    "frefre"), is(allLines));
            get = ftpClient.executeGet(new GetRequest("./src/test/resources/listDir/file2.txt"));
            get.toFile("testFile.txt");
            allLines = Files.readAllLines(Paths.get("testFile.txt"));
            assertFalse(allLines.isEmpty());
            assertThat(Collections.singletonList("9"), is(allLines));

            get = ftpClient.executeGet(new GetRequest("./src/test/resources/listDir/emptyFile.txt"));
            get.toFile("testFile.txt");
            allLines = Files.readAllLines(Paths.get("testFile.txt"));
            assertTrue(allLines.isEmpty());


        } finally {
            ftpClient.disconnect();
            ftpServer.serverStop();
        }
    }

    @Test
    public void executeGetLazy() throws Exception {
        int serverPort = portNumberMod + (rnd.nextInt() % portNumberMod);
        FtpServerImpl ftpServer = new FtpServerImpl(serverPort);
        FtpClientImpl ftpClient = new FtpClientImpl(serverPort);
        try {
            ftpServer.serverStart();
            ftpClient.connect();
            String assertPath = "./src/test/resources/listDir/assert.txt";
            GetResponse get = ftpClient.executeGet(new GetRequest("./src/test/resources/listDir/file1.txt"));
            get.toFile(assertPath);

            assertEquals(Files.readAllLines(Paths.get("./src/test/resources/listDir/file1.txt")), Files.readAllLines(Paths.get(assertPath)));
            new File(assertPath).delete();
        } finally {
            ftpClient.disconnect();
            ftpServer.serverStop();
        }
    }
}