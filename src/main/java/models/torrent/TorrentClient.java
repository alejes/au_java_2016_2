package models.torrent;

public interface TorrentClient {
    default short getServerPort(){
        return 8081;
    }
    void update();
}
