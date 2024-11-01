package tracker;

import java.lang.reflect.Field;

public class TrackerDTO {
    // The 20 byte sha1 hash of the bencoded form of the info value from the metainfo file. This value will almost certainly have to be escaped.
    private String info_hash;

    // A string of length 20 which this downloader uses as its id.
    // Each downloader generates its own id at random at the start of a new download. This value will also almost certainly have to be escaped.
    private String peer_id;

    // The port number this peer is listening on.
    // Common behavior is for a downloader to try to listen on port 6881 and if that port is taken try 6882, then 6883, etc. and give up after 6889.
    private String port;

    // The total amount uploaded so far, encoded in base ten ascii.
    private String uploaded;

    // The total amount downloaded so far, encoded in base ten ascii.
    private String downloaded;

    // The number of bytes this peer still has to download, encoded in base ten ascii.
    // Note that this can't be computed from downloaded and the file length since it might be a resume,
    // and there's a chance that some of the downloaded data failed an integrity check and had to be re-downloaded.
    private String left;

    // Whether the peer list should use the compact representation
    private String compact;

    public TrackerDTO(String info_hash, String peer_id, String port, String uploaded, String downloaded, String left, String compact) {
        this.info_hash = info_hash;
        this.peer_id = peer_id;
        this.port = port;
        this.uploaded = uploaded;
        this.downloaded = downloaded;
        this.left = left;
        this.compact = compact;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();

        for(Field field: this.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try {
                result.append(field.getName())
                        .append("=")
                        .append(field.get(this))
                        .append("&");
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return result.toString();
    }

    public static class Builder {
        private String info_hash;
        private String peer_id;
        private String port;
        private String uploaded;
        private String downloaded;
        private String left;
        private String compact;

        Builder info_hash(String info_hash) {
            this.info_hash = info_hash;
            return this;
        }

        Builder peer_id(String peer_id) {
            this.peer_id = peer_id;
            return this;
        }

        Builder port(String port) {
            this.port = port;
            return this;
        }

        Builder uploaded(String uploaded) {
            this.uploaded = uploaded;
            return this;
        }

        Builder downloaded(String downloaded) {
            this.downloaded = downloaded;
            return this;
        }

        Builder left(String left) {
            this.left = left;
            return this;
        }

        Builder compact(String compact) {
            this.compact = compact;
            return this;
        }

        TrackerDTO build() {
            return new TrackerDTO(
                    info_hash,
                    peer_id,
                    port,
                    uploaded,
                    downloaded,
                    left,
                    compact
            );
        }
    }
}
