package peer;

import torrent.Torrent;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;

public class PeerMessageService {
    private static final int BLOCK_LENGTH = 16 * 1024;
    private final Torrent torrent;
    private final TCPClient tcpClient;

    public PeerMessageService(Torrent torrent, String ipAddressAndPort) throws IOException {
        this.torrent = torrent;

        String[] iPAddressParts = ipAddressAndPort.split(":");
        String peerIp = iPAddressParts[0];
        int peerPort = Integer.parseInt(iPAddressParts[1]);
        this.tcpClient = new TCPClient(peerIp, peerPort);
    }

    public PeerHandshakeDTO connectWithPeer() throws IOException {
        PeerHandshakeDTO peerHandshakeDTO = new PeerHandshakeDTO(new byte[20], torrent.getInfoHash());

        tcpClient.sendBytes(peerHandshakeDTO.getBytes());
        byte[] response = new byte[68];
        tcpClient.receiveAllBytes(response);

        tcpClient.closeSocket();

        return new PeerHandshakeDTO(response);
    }

    public void establishConnection() throws IOException {
        // Step 1: Perform Handshake
        performHandshake();

        // Step 2: Send interested message and receive unchoked message
        readyToReceivePieces();
    }

    public void downloadPiece(int pieceIndex, String downloadLocation) throws IOException, NoSuchAlgorithmException {
        // Step 3: Receive piece
        byte[] piece = receivePiece(pieceIndex);

        if(!torrent.getPieceHashes().get(pieceIndex).equals(Torrent.byteArrayToHexaDecimal(Torrent.hashTheByteArray(piece)))) {
            throw new IllegalStateException("Piece hash does not match");
        }

        // Step 4: Save the piece onto downloadLocation
        saveBytes(piece, downloadLocation);

//        tcpClient.closeSocket();
    }

    private byte[] receivePiece(int pieceIndex) throws IOException {
        int lenOfCurrentPiece = (int) Math.min(torrent.getPieceLength(), torrent.getLength() - (pieceIndex*torrent.getPieceLength()));
        byte[] piece = new byte[lenOfCurrentPiece];
        ByteBuffer pieceByteBuffer = ByteBuffer.wrap(piece);

        for(int i=0; i<lenOfCurrentPiece; i+=BLOCK_LENGTH) {
            int length = Math.min(BLOCK_LENGTH, lenOfCurrentPiece - i);
            byte[] block = getBlock(pieceIndex, i, length);

            pieceByteBuffer.put(block);
        }

        pieceByteBuffer.flip();
        pieceByteBuffer.get(piece);

        return piece;
    }

    private byte[] getBlock(int index, int begin, int length) throws IOException {
        byte[] block = new byte[length];

        ByteBuffer getBlockMessage = ByteBuffer.allocate(17);
        getBlockMessage.putInt(13);
        getBlockMessage.put((byte) 6);
        getBlockMessage.putInt(index);
        getBlockMessage.putInt(begin);
        getBlockMessage.putInt(length);
        getBlockMessage.flip();

        tcpClient.sendByteBuffer(getBlockMessage);

        ByteBuffer blockMessage = tcpClient.receiveMessage();

        int messageId = blockMessage.get();
        assert messageId == 7;

        int receivedIndex = blockMessage.getInt();
        assert receivedIndex == index;

        int receivedBegin = blockMessage.getInt();
        assert receivedBegin == begin;

        blockMessage.get(block);

        return block;
    }

    private void readyToReceivePieces() throws IOException {
        // Receive bitfield message
        ByteBuffer bitfieldResponseByteBuffer = tcpClient.receiveMessage();
        assert bitfieldResponseByteBuffer.get() == 5;

        // Send interested message
        ByteBuffer interestedMessageBuffer = ByteBuffer.allocate(5);
        interestedMessageBuffer.putInt(1);
        interestedMessageBuffer.put((byte) 2);
        interestedMessageBuffer.flip();
        tcpClient.sendByteBuffer(interestedMessageBuffer);

        // Receive unchoked message
        ByteBuffer unchokeResponseByteBuffer = tcpClient.receiveMessage();
        while(unchokeResponseByteBuffer.limit() == 0 || unchokeResponseByteBuffer.get() != 1) {
            unchokeResponseByteBuffer = tcpClient.receiveMessage();
        }
    }

    private void performHandshake() throws IOException {
        PeerHandshakeDTO peerHandshakeDTO = new PeerHandshakeDTO(new byte[20], torrent.getInfoHash());

        tcpClient.sendBytes(peerHandshakeDTO.getBytes());
        byte[] response = new byte[68];
        tcpClient.receiveAllBytes(response);
    }

    private void saveBytes(byte[] bytes, String filePath) throws IOException {
        try (final var fileOutputStream = new FileOutputStream(filePath)) {
            fileOutputStream.write(bytes);
        }
    }
}
