package torrent;

import com.dampcake.bencode.Bencode;
import com.dampcake.bencode.Type;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TorrentMetaData {
    private String trackerUrl;
    private long length;
    private byte[] infoHash;

    private long pieceLength;
    private List<String> pieceHashes;

    public TorrentMetaData(String torrentMetadataFileName) throws NoSuchAlgorithmException, IOException {
        File torrentFile = new File(torrentMetadataFileName);
        byte[] torrentMetaInfoBytes = Files.readAllBytes(torrentFile.toPath());

        Bencode bencode = new Bencode(true);

        Map<String, Object> metaInfo = bencode.decode(torrentMetaInfoBytes, Type.DICTIONARY);
        Map<String, Object> infoDict = (Map<String, Object>) metaInfo.get("info");

        byte[] bencodedByteArray = bencode.encode(infoDict);

        this.infoHash = hashTheByteArray(bencodedByteArray);
        this.trackerUrl = byteBufferToString((ByteBuffer) metaInfo.get("announce"));
        this.length = (long) infoDict.get("length");

        this.pieceLength = (long) infoDict.get("piece length");
        this.pieceHashes =  getPieceHashesFromPiecesByteBuffer((ByteBuffer) infoDict.get("pieces"));
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("Tracker URL: ").append(this.trackerUrl).append(System.lineSeparator())
                .append("Length: ").append(this.length).append(System.lineSeparator())
                .append("Info Hash: ").append(byteArrayToHexaDecimal(this.infoHash)).append(System.lineSeparator())
                .append("Piece Length: ").append(this.pieceLength).append(System.lineSeparator())
                .append("Piece Hashes:").append(System.lineSeparator());

        for (String pieceHash: pieceHashes) {
            stringBuilder.append(pieceHash).append(System.lineSeparator());
        }

        return stringBuilder.toString();
    }

    public static byte[] hashTheByteArray(byte[] bytes) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        md.update(bytes);
        return md.digest();
    }

    private String byteBufferToString(ByteBuffer byteBuffer) {
        return StandardCharsets.UTF_8.decode(byteBuffer).toString();
    }

    private List<String> getPieceHashesFromPiecesByteBuffer(ByteBuffer pieces) throws NoSuchAlgorithmException {
        byte[] byteArrayOfPieceHashes = new byte[pieces.remaining()];
        pieces.get(byteArrayOfPieceHashes);

        List<String> pieceHashes = new ArrayList<>();

        int currentSize = 0;
        byte[] currentHash = new byte[20];

        for (int index = 0; index < byteArrayOfPieceHashes.length; index++) {
            currentHash[index % 20] = byteArrayOfPieceHashes[index];
            currentSize++;

            if(currentSize == 20) {
                pieceHashes.add(byteArrayToHexaDecimal(currentHash));
                currentSize = 0;
            }
        }

        return pieceHashes;
    }

    public static String byteArrayToHexaDecimal(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();

        for (byte b : bytes) {
            hexString.append(String.format("%02x", b));
        }

        return hexString.toString();
    }

    public String getTrackerUrl() {
        return trackerUrl;
    }

    public long getLength() {
        return length;
    }

    public byte[] getInfoHash() {
        return infoHash;
    }

    public long getPieceLength() {
        return pieceLength;
    }

    public List<String> getPieceHashes() {
        return pieceHashes;
    }
}
