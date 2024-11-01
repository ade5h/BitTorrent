package decoder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BencodedDecorder {
    public static DecoderVO decodeBencode(String bencodedString, int startIndex) {
        if (Character.isDigit(bencodedString.charAt(startIndex))) {
            return decodeString(bencodedString, startIndex);
        }
        else if(bencodedString.charAt(startIndex) == 'i') {
            return decodeInteger(bencodedString, startIndex);
        }
        else if(bencodedString.charAt(startIndex) == 'l') {
            return decodeList(bencodedString, startIndex);
        }
        else if(bencodedString.charAt(startIndex) == 'd') {
            return decodeDictionary(bencodedString, startIndex);
        }
        else {
            throw new RuntimeException("Not a valid bencoded string");
        }
    }

    private static DecoderVO decodeString(String bencodedString, int startIndex) {
        int firstColonIndex = startIndex;
        for(int i = startIndex; i < bencodedString.length(); i++) {
            if(bencodedString.charAt(i) == ':') {
                firstColonIndex = i;
                break;
            }
        }
        int length = Integer.parseInt(bencodedString.substring(startIndex, firstColonIndex));
        int lastIndex = firstColonIndex+length+1;

        String decodedString = bencodedString.substring(firstColonIndex+1, lastIndex);

        return new DecoderVO(
                decodedString,
                lastIndex
        );
    }

    private static DecoderVO decodeInteger(String bencodedString, int startIndex) {
        int endIndex = startIndex;
        while(bencodedString.charAt(endIndex) != 'e') {
            endIndex++;
        }

        Long decodedLong = Long.parseLong(bencodedString.substring(startIndex+1, endIndex));

        return new DecoderVO(
                decodedLong,
                endIndex+1
        );
    }

    private static DecoderVO decodeList(String bencodedString, int startIndex) {
        List<Object> decodedList = new ArrayList<>();

        int currentIndex = startIndex+1;

        while(bencodedString.charAt(currentIndex) != 'e') {
            DecoderVO decoderVO = decodeBencode(bencodedString, currentIndex);
            decodedList.add(decoderVO.getDecodedObj());
            currentIndex = decoderVO.getNextIndex();
        }

        return new DecoderVO(
                decodedList,
                currentIndex+1
        );
    }

    private static DecoderVO decodeDictionary(String bencodedString, int startIndex) {
        Map<String, Object> decodedDict = new HashMap<>();

        int currentIndex = startIndex+1;

        while(bencodedString.charAt(currentIndex) != 'e') {
            DecoderVO keyDecoderVO = decodeBencode(bencodedString, currentIndex);
            DecoderVO valueDecoderVO = decodeBencode(bencodedString, keyDecoderVO.getNextIndex());

            decodedDict.put(keyDecoderVO.getDecodedObj().toString(), valueDecoderVO.getDecodedObj());
            currentIndex = valueDecoderVO.getNextIndex();
        }

        return new DecoderVO(
                decodedDict,
                currentIndex+1
        );
    }
}
