package models.responses;


import exceptions.FTPException;
import models.FtpFile;
import models.Networkable;

import java.util.ArrayList;
import java.util.List;

public class ListResponse implements Networkable {
    List<FtpFile> items;

    public ListResponse(List<FtpFile> items) {
        this.items = items;
    }

    public ListResponse(String stringResponse) {
        String[] parts = stringResponse.split(" ");
        if ((parts.length < 1) || (parts.length % 2 != 1)) {
            throw new FTPException("Cannot parse list response");
        }
        this.items = new ArrayList<>();
        for (int i = 1; i < parts.length; i += 2) {
            boolean isDir = parts[i + 1].equals("true");
            items.add(new FtpFile(isDir, parts[i].trim()));
        }
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder(items.size());
        if (items.size() > 0) {
            items.forEach((it) -> result.append(it.toString() + '\n'));
        }
        return result.toString();
    }

    @Override
    public String toNetworkResponse() {
        StringBuilder result = new StringBuilder(items.size());
        if (items.size() > 0) {
            items.forEach((it) -> result.append(it.toString() + '\n'));
        }
        return result.toString();
    }
}
