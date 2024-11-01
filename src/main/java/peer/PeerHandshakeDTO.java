package peer;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class PeerHandshakeDTO {
    byte[] infoHash;
    byte[] peerId;

    public PeerHandshakeDTO(byte[] peerId, byte[] infoHash) {
        this.peerId = peerId;
        this.infoHash = infoHash;
    }

    public PeerHandshakeDTO(byte[] byteArray) {
        this.infoHash = Arrays.copyOfRange(byteArray, 28, 48);
        this.peerId = Arrays.copyOfRange(byteArray, 48, 68);
    }

    public byte[] getBytes() {
        ByteBuffer handshakeByteBuffer = ByteBuffer.allocate(68);
        handshakeByteBuffer.put((byte) 19);
        handshakeByteBuffer.put("BitTorrent protocol".getBytes(StandardCharsets.UTF_8));
        handshakeByteBuffer.put(new byte[8]);
        handshakeByteBuffer.put(infoHash);
        handshakeByteBuffer.put(peerId);

        handshakeByteBuffer.flip();

        byte[] handshakeBytes = new byte[68];
        handshakeByteBuffer.get(handshakeBytes);

        return handshakeBytes;
    }

    public byte[] getInfoHash() {
        return infoHash;
    }

    public byte[] getPeerId() {
        return peerId;
    }
}
