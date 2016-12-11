package models.responses;

import exceptions.FTPException;

import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class GetResponse {
    private final DataInputStream is;

    public GetResponse(DataInputStream is) {
        this.is = is;
    }

    public void toFile(String filePath) throws FTPException {
        try (FileOutputStream fileStream = new FileOutputStream(filePath)) {
            long size = is.readLong();
            if (size > 0) {
                byte[] byteBuffer = new byte[4096];
                long readed = 0;
                while (readed < size) {
                    int cnt = is.read(byteBuffer);
                    if (cnt < 0) {
                        break;
                    }
                    readed += cnt;
                    fileStream.write(byteBuffer, 0, cnt);
                }
            }
        } catch (FileNotFoundException e) {
            throw new FTPException("Cant found target file to save", e);
        } catch (IOException e) {
            throw new FTPException("IOException", e);
        }
    }

}
