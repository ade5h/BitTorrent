package tracker;

import com.dampcake.bencode.Bencode;
import com.dampcake.bencode.Type;
import torrent.Torrent;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class TrackerService {

    private static final String PORT_NUMBER = "6881";
    private static final String MY_PEER_ID = "7A3F8C2E1B9D5A4F6E0C2D8B3A1F9E7C5D4B6A2C";

    public List<String> getPeers(Torrent torrent) {
        TrackerDTO trackerDTO = new TrackerDTO.Builder()
                .info_hash(UrlEncode(Torrent.byteArrayToHexaDecimal(torrent.getInfoHash())))
                .peer_id(UrlEncode(MY_PEER_ID))
                .port(PORT_NUMBER)
                .uploaded("0")
                .downloaded("0")
                .left(Long.toString(torrent.getLength()))
                .compact("1")
                .build();

        byte[] response = new TrackerProxy().getPeersFromTracker(torrent.getTrackerUrl(), trackerDTO.toString());

        Bencode bencode = new Bencode(true);
        ByteBuffer peersByteBuffer = (ByteBuffer) bencode.decode(response, Type.DICTIONARY).get("peers");

        return getPeersFromByteBuffer(peersByteBuffer);
    }

    // TODO: Use a library to URLEncode
    private String UrlEncode(String s) {
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < s.length(); i++) {
            if(i%2 == 0) {
                stringBuilder.append("%");
            }
            char c = s.charAt(i);
            stringBuilder.append(c);
        }

        return stringBuilder.toString();
    }

    private List<String> getPeersFromByteBuffer(ByteBuffer byteBuffer) {
        byte[] byteArrayOfPeers = new byte[byteBuffer.remaining()];
        byteBuffer.get(byteArrayOfPeers);

        List<String> peers = new ArrayList<>();

        int currentSize = 0;
        byte[] currentPeer = new byte[6];

        for (int index = 0; index < byteArrayOfPeers.length; index++) {
            currentPeer[index % 6] = byteArrayOfPeers[index];
            currentSize++;

            if(currentSize == 6) {
                peers.add(getPeerFromByteArray(currentPeer));
                currentSize = 0;
            }
        }

        return peers;
    }

    private String getPeerFromByteArray(byte[] peerByte) {
        StringBuilder peerBuilder = new StringBuilder();

        for (int i = 0; i < 4; i++) {
            int unsignedByte = peerByte[i] & 0xff;
            peerBuilder.append(unsignedByte);
            if(i != 3)
                peerBuilder.append(".");
        }

        peerBuilder.append(":");

        int port = ((int) peerByte[4] & 0xff)*256 + ((int) peerByte[5] & 0xff);
        peerBuilder.append(port);

        return peerBuilder.toString();
    }
}
