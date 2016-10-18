package models.utils;

import java.io.IOException;
import java.io.InputStream;


public class StreamFtpInputStream extends InputStream {
    private final InputStream is;
    private final long size;
    private long pos = 0;

    public StreamFtpInputStream(InputStream is) throws IOException {
        this.is = is;
        String readedSize = "";
        while (true) {
            int nxt = is.read();
            if ((nxt == ' ') || ((readedSize.length() == 0) && (nxt == '0'))) {
                break;
            }

            readedSize += (char) nxt;
        }
        size = Integer.valueOf((readedSize.length() == 0) ? "0" : readedSize);
    }

    @Override
    public int read() throws IOException {
        if (pos < size) {
            int res = is.read();
            ++pos;
            return res;
        } else {
            return -1;
        }
    }

    @Override
    public int available() throws IOException {
        return (size > pos) ? 1 : 0;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        is.close();
    }
}