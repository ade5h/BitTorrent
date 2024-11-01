package decoder;

public class DecoderVO {
    private Object decodedObj;
    private int nextIndex;

    public DecoderVO(Object decodedObj, int nextIndex) {
        this.decodedObj = decodedObj;
        this.nextIndex = nextIndex;
    }

    public Object getDecodedObj() {
        return decodedObj;
    }

    public int getNextIndex() {
        return nextIndex;
    }
}