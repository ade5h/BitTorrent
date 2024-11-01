import downloader.ParallelPieceDownloader;
import torrent.TorrentMetaData;

public class BitTorrentClientApplication {
    public static void main(String[] args) throws Exception {
        String torrentFileName = args[0];
        String downloadLocation = args[1];

        TorrentMetaData torrentMetaData = new TorrentMetaData(torrentFileName);

        ParallelPieceDownloader parallelPieceDownloader = new ParallelPieceDownloader(torrentMetaData);
        parallelPieceDownloader.downloadAllPieces(downloadLocation);
    }
}
