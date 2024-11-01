import downloader.ParallelPieceDownloader;
import torrent.Torrent;

import java.io.File;
import java.nio.file.Files;

public class BitTorrentClientApplication {
    public static void main(String[] args) throws Exception {
        String torrentFileName = args[0];
        File torrentFile = new File(torrentFileName);
        byte[] bytes = Files.readAllBytes(torrentFile.toPath());
        Torrent torrent = new Torrent(bytes);

        String downloadLocation = args[1];

        ParallelPieceDownloader parallelPieceDownloader = new ParallelPieceDownloader(torrent);
        parallelPieceDownloader.downloadAllPieces(downloadLocation);
    }
}
