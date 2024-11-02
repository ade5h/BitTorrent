import downloader.ParallelPieceDownloader;
import torrent.TorrentMetaData;

public class BitTorrentClientApplication {
    public static void main(String[] args) {
        try {
            String torrentFileName = args[0];
            String downloadLocation = args[1];

            TorrentMetaData torrentMetaData = new TorrentMetaData(torrentFileName);
            System.out.println(torrentMetaData);

            ParallelPieceDownloader parallelPieceDownloader = new ParallelPieceDownloader(torrentMetaData);
            parallelPieceDownloader.downloadAllPieces(downloadLocation);
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
