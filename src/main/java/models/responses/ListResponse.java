package models.responses;


import models.FtpFile;

import java.util.List;
import java.util.stream.Collectors;

public class ListResponse {
    private final List<FtpFile> items;

    public ListResponse(List<FtpFile> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return items.stream().map(FtpFile::toString).collect(Collectors.joining("\n"));
    }
}
