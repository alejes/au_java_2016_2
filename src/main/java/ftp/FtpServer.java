package ftp;

import java.io.IOException;

public interface FtpServer {
    void serverStart() throws IOException;

    void serverStop() throws IOException;
}
