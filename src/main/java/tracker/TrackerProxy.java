package tracker;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class TrackerProxy {
    public byte[] getPeersFromTracker(String trackerUrl, String queryParams) {
        try {
            StringBuilder uriBuilder = new StringBuilder();
            uriBuilder.append(trackerUrl)
                    .append("?")
                    .append(queryParams);

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(new URI(uriBuilder.toString()))
                    .GET()
                    .build();

            HttpResponse<byte[]> response = HttpClient.newHttpClient()
                    .send(httpRequest, HttpResponse.BodyHandlers.ofByteArray());

            return response.body();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
