package ftp;

import exceptions.FTPException;
import models.requests.GetRequest;
import models.requests.ListRequest;
import models.responses.GetResponse;
import models.responses.ListResponse;

public interface FtpClient {
    void connect() throws FTPException;

    void disconnect() throws FTPException;

    ListResponse executeList(ListRequest request) throws FTPException;

    GetResponse executeGet(GetRequest request) throws FTPException;
}
