package models.responses;

import exceptions.FTPException;
import models.Networkable;
import models.utils.StreamFtpInputStream;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class GetResponse implements Networkable {
    private final byte[] content;
    private final StreamFtpInputStream is;

    public GetResponse(byte[] content) {
        this.content = content;
        this.is = null;
    }

    public GetResponse(StreamFtpInputStream is) {
        this.content = null;
        this.is = is;
    }

    public GetResponse(String stringResponse) {
        content = stringResponse.getBytes();
        this.is = null;
    }

    public byte[] getBytes() {
        return content;
    }

    public void toFile(String filePath) {
        try {
            FileOutputStream fileStream = new FileOutputStream(filePath);

            if (is == null) {
                fileStream.write(content);
            } else {
                byte[] byteBuffer = new byte[4096];
                while (is.available() > 0) {
                    int cnt = is.read(byteBuffer);
                    if (cnt < 0) {
                        break;
                    }
                    fileStream.write(new String(byteBuffer).substring(0, cnt).getBytes());
                }
            }
            fileStream.close();
        } catch (FileNotFoundException e) {
            throw new FTPException("Cant found target file to save", e);
        } catch (IOException e) {
            throw new FTPException("IOException", e);
        }
    }

    @Override
    public String toString() {
        return new String(content);
    }

    @Override
    public String toNetworkResponse() {
        return new String(content);
    }
}
