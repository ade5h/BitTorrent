package downloader;

import peer.PeerMessageService;
import torrent.Torrent;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

public class PeerWorkerService implements Runnable {
    public static final String PIECE_FILE_PREFIX = "tmp/piece-";
    private BlockingQueue<Integer> piecesQueue;
    private PeerMessageService peerMessageService;
    private CountDownLatch completionLatch;
    private String peerAddress;

    public PeerWorkerService(String peerAddress, Torrent torrent, BlockingQueue<Integer> piecesQueue, CountDownLatch completionLatch) throws IOException {
        this.peerAddress = peerAddress;
        this.piecesQueue = piecesQueue;
        this.peerMessageService = new PeerMessageService(torrent, peerAddress);
        this.completionLatch = completionLatch;

        peerMessageService.establishConnection();
    }

    @Override
    public void run() {
        while(true) {
            Integer pieceNumber = null;
            try {
                pieceNumber = piecesQueue.take();

                String fileName = PIECE_FILE_PREFIX + pieceNumber;
                System.out.println("Worker - " + peerAddress + " | trying to download piece: " + pieceNumber);
                peerMessageService.downloadPiece(pieceNumber, fileName);

                completionLatch.countDown();
                System.out.println("Worker - " + peerAddress + " | finished downloading piece: " + pieceNumber);
            }
            catch (InterruptedException ie) {
                System.out.println("Worker - " + peerAddress + " | Closing this worker");
                return;
            }
            catch (Exception e) {
                System.out.println("Piece no. " + pieceNumber + "failed because of the following exception: " + e.getMessage());
                e.printStackTrace();
                assert pieceNumber != null;
                boolean pieceAdded = piecesQueue.offer(pieceNumber);

                if(!pieceAdded) throw new RuntimeException("Not able to add the piece " + pieceNumber + " onto the queue");
            }
        }
    }
}
