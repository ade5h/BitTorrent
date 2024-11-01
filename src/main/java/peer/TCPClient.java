package peer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;

public class TCPClient {
    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;

    public TCPClient(String host, int port) throws IOException {
        this.socket = new Socket(host, port);

        this.out = new DataOutputStream(socket.getOutputStream());
        this.in = new DataInputStream(socket.getInputStream());
    }

    public void sendBytes(byte[] message) throws IOException {
        this.out.write(message);
    }

    public void sendByteBuffer(ByteBuffer messageByteBuffer) throws IOException {
        byte[] message = new byte[messageByteBuffer.limit()];
        messageByteBuffer.get(message);

        this.out.write(message);
    }

    public void receiveAllBytes(byte[] response) throws IOException {
        this.in.readFully(response);
    }

    public ByteBuffer receiveMessage() throws IOException {
        int messageSize = this.in.readInt();

        byte[] response = new byte[messageSize];
        this.in.readFully(response);

        return ByteBuffer.wrap(response);
    }

    void closeSocket() throws IOException {
        if (out != null) out.close();
        if (in != null) in.close();
        if (socket != null) socket.close();
    }
}
