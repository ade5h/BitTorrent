package downloader;

import torrent.Torrent;
import tracker.TrackerService;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.*;

import static downloader.CombiningFilesUtil.combinePiecesAndSaveToFile;

public class ParallelPieceDownloader {
    private Torrent torrent;
    private BlockingDeque<Integer> piecesQueue;
    private CountDownLatch completionLatch;
    List<String> peers;

    public ParallelPieceDownloader(Torrent torrent) {
        this.torrent = torrent;
        this.completionLatch = new CountDownLatch(torrent.getPieceHashes().size());
        this.piecesQueue = new LinkedBlockingDeque<>();
        this.peers = new TrackerService().getPeers(torrent);
    }

    public void downloadAllPieces(String outputFile) throws IOException, InterruptedException, TimeoutException {
        createTempDirectory("tmp");

        // Add all the pieces onto the queue
        for (int i = 0; i < torrent.getPieceHashes().size(); i++) {
            piecesQueue.offer(i);
        }

        // Starting all the workers
        ExecutorService workersExecutorService = Executors.newFixedThreadPool(peers.size());
        for (String peer: peers) {
            workersExecutorService.submit(new PeerWorkerService(peer, torrent, piecesQueue, completionLatch));
        }

        // Wait for all pieces to be downloaded
        boolean completed = completionLatch.await(30, TimeUnit.MINUTES);
        workersExecutorService.shutdownNow();

        if (!completed) {
            throw new TimeoutException("Download timed out after 30 minutes");
        }

        combinePiecesAndSaveToFile(outputFile);
    }

    private void createTempDirectory(String directoryName) {
        File tempDirectory = new File(directoryName);
        if (!tempDirectory.exists()){
            tempDirectory.mkdir();
        }
    }
}
