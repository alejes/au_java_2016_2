package models.responses;


import models.FtpFile;

import java.util.List;

public class ListResponse {
    List<FtpFile> items;

    public ListResponse(List<FtpFile> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder(items.size());
        if (items.size() > 0) {
            items.forEach((it) -> result.append(" " + it));
        }
        return result.toString();
    }
}
