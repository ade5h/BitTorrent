package downloader;

import torrent.TorrentMetaData;
import tracker.TrackerService;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.*;

import static downloader.CombiningFilesUtil.combinePiecesAndSaveToFile;

public class ParallelPieceDownloader {
    private TorrentMetaData torrentMetaData;
    private BlockingDeque<Integer> piecesQueue;
    private CountDownLatch completionLatch;
    List<String> peers;

    public ParallelPieceDownloader(TorrentMetaData torrentMetaData) {
        this.torrentMetaData = torrentMetaData;
        this.completionLatch = new CountDownLatch(torrentMetaData.getPieceHashes().size());
        this.piecesQueue = new LinkedBlockingDeque<>();
        this.peers = new TrackerService().getPeers(torrentMetaData);
    }

    public void downloadAllPieces(String outputFile) throws IOException, InterruptedException, TimeoutException {
        createTempDirectory("tmp");

        // Add all the pieces onto the queue
        for (int i = 0; i < torrentMetaData.getPieceHashes().size(); i++) {
            piecesQueue.offer(i);
        }

        // Starting all the workers
        ExecutorService workersExecutorService = Executors.newFixedThreadPool(peers.size());
        for (String peer: peers) {
            workersExecutorService.submit(new PeerWorkerService(peer, torrentMetaData, piecesQueue, completionLatch));
        }

        // Wait for all pieces to be downloaded onto the /tmp directory
        boolean completed = completionLatch.await(30, TimeUnit.MINUTES);
        workersExecutorService.shutdownNow();

        System.out.println("All the pieces downloaded to /tmp directory");

        if (!completed) {
            throw new TimeoutException("Download timed out after 30 minutes");
        }

        // Combine all the pieces /tmp directory and finally save them in outputFile location
        combinePiecesAndSaveToFile(outputFile);

        System.out.println("Download complete. File saved in the following location: " + outputFile);
    }

    private void createTempDirectory(String directoryName) {
        File tempDirectory = new File(directoryName);
        if (!tempDirectory.exists()){
            tempDirectory.mkdir();
        }
    }
}
