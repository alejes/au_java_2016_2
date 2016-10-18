package ftp;

import models.requests.GetRequest;
import models.requests.ListRequest;
import models.responses.GetResponse;
import models.responses.ListResponse;

import java.io.File;

public interface FtpClient {
    void connect();

    void disconnect();

    ListResponse executeList(ListRequest request);

    GetResponse executeGet(GetRequest request);

    GetResponse executeGetLazy(GetRequest request);

}
