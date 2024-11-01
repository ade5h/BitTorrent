package downloader;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CombiningFilesUtil {
    public static void combinePiecesAndSaveToFile(String outputFile) {
        try {
            String pieceDirectory = "tmp";

            List<String> pieces = findPieces(pieceDirectory);
            System.out.println("Files will be combined in this order:");
            for (String piece : pieces) {
                System.out.println(piece);
            }

            combineFiles(outputFile, pieces);
            System.out.println("Files combined successfully!");

        } catch (IOException e) {
            System.err.println("Error combining files: " + e.getMessage());
        }
    }

    private static void combineFiles(String outputPath, List<String> inputPaths) throws IOException {
        // Create output file
        try (FileOutputStream fos = new FileOutputStream(outputPath);
             FileChannel outputChannel = fos.getChannel()) {

            // Process each input file
            for (String inputPath : inputPaths) {
                try (FileInputStream fis = new FileInputStream(inputPath);
                     FileChannel inputChannel = fis.getChannel()) {

                    // Get the size of the current input file
                    long size = inputChannel.size();

                    // Transfer from input to output using a buffer size of 8MB
                    long position = 0;
                    while (position < size) {
                        position += inputChannel.transferTo(
                                position,    // position in input file
                                8 << 20,     // 8MB chunks
                                outputChannel
                        );
                    }
                }
            }

            deleteFilePieces(inputPaths);
        }
    }

    // Utility method to find all pieces in a directory with a specific pattern
    private static List<String> findPieces(String directory) throws IOException {
        List<String> pieces = new ArrayList<>();
        Pattern piecePattern = Pattern.compile("piece-(\\d+)");

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(
                Paths.get(directory),
                path -> piecePattern.matcher(path.getFileName().toString()).matches())) {

            for (Path path : stream) {
                pieces.add(path.toString());
            }
        }

        // Custom sorting based on the numeric value after "piece-"
        Collections.sort(pieces, (a, b) -> {
            Matcher matcherA = piecePattern.matcher(Paths.get(a).getFileName().toString());
            Matcher matcherB = piecePattern.matcher(Paths.get(b).getFileName().toString());

            if (matcherA.find() && matcherB.find()) {
                int numA = Integer.parseInt(matcherA.group(1));
                int numB = Integer.parseInt(matcherB.group(1));
                return Integer.compare(numA, numB);
            }
            return a.compareTo(b);  // fallback to string comparison
        });

        return pieces;
    }

    // New method to handle piece deletion
    private static void deleteFilePieces(List<String> paths) {
        List<String> failedDeletions = new ArrayList<>();

        for (String path : paths) {
            try {
                Files.delete(Paths.get(path));
                System.out.println("Deleted: " + path);
            } catch (IOException e) {
                failedDeletions.add(path);
                System.err.println("Failed to delete: " + path + " - " + e.getMessage());
            }
        }

        // Report any failures
        if (!failedDeletions.isEmpty()) {
            System.err.println("\nWarning: Failed to delete the following pieces:");
            failedDeletions.forEach(path -> System.err.println("  - " + path));
        }
    }


}
