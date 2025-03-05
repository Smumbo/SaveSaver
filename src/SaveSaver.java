
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;


public class SaveSaver {
    private static final String USAGE_STRING = """
    usage: java -jar SaveSaver.jar <savePath> [(-u | -d) <cloudPath>] [-b <backupPath> <backupCount>...]

    arguments:
    \t<savePath>\tpath to the game save folder

    options:
    \t-u, --upload\t\tupload the save to the cloud. This will override the cloud save.
    \t-d, --download\t\tdownload the save from the cloud. This will override the local save.
    \t\t<cloudPath>\tpath to the cloud save folder

    \t-b, --backup\t\tcreate a backup of the save folder
    \t\t<backupPath>\twhere to save the backup to
    \t\t<backupNumber>\tnumber of rotating backups to keep at path
    """;
    private static final List<String> UPLOAD_OPTIONS = List.of("-u", "--upload");
    private static final List<String> DOWNLOAD_OPTIONS = List.of("-d", "--download");
    private static final List<String> BACKUP_OPTIONS = List.of("-b", "--backup");

    private static Path savePath;
    private static boolean upload = false;
    private static boolean download = false;
    private static Path cloudPath;
    private static List<Backup> backups;

    // Parses the arguments, determines the operations, gets the path arguments, and checks for errors
    private static void parse(String[] args) {
        System.out.println(Arrays.toString(args));

        if (args.length < 2) {
            System.out.println(USAGE_STRING);
            System.exit(1);
        }

        // Save path
        savePath = Paths.get(args[0]);
        if (!Files.exists(savePath)) {
            System.err.println(String.format("Invalid save path (%s), path does not exist", args[0]));
            System.exit(1);
        }
        if (!Files.isDirectory(savePath)) {
            System.err.println(String.format("Invalid save path (%s), path must be a directory", args[0]));
            System.exit(1);
        }

        for (int i = 1; i < args.length; i++) {
            String token = args[i];

            // Upload or Download
            if (UPLOAD_OPTIONS.contains(token)) {
                upload = true;
            }
            if (DOWNLOAD_OPTIONS.contains(token)) {
                download = true;
                
            }
            if (upload && download) {
                System.err.println("Cannot upload and download at the same time");
                System.exit(1);
            }
            if (upload || download) { 
                if (i + 1 >= args.length) {
                    System.err.println("Missing cloud path");
                    System.exit(1);
                }
                cloudPath = Paths.get(args[i + 1]);
                i++;
                if (download) {
                    if (!Files.exists(cloudPath)) {
                        System.err.println(String.format("Invalid cloud path (%s), path does not exist", args[i + 1]));
                        System.exit(1);
                    }
                    if (!Files.isDirectory(cloudPath)) {
                        System.err.println(String.format("Invalid cloud path (%s), path must be a directory", args[i + 1]));
                        System.exit(1);
                    }
                }
                continue;
            }

            // Backups
            if (BACKUP_OPTIONS.contains(token)) {
                if (i + 2 >= args.length) {
                    System.err.println("Missing backup arguments");
                    System.exit(1);
                }
                File backupPath = new File(args[i + 1]);
                if (!backupPath.isDirectory()) {
                    System.err.println(String.format("Invalid backup path (%s), backup path must be a directory", args[i + 1]));
                    System.exit(1);
                }
                int backupNumber = Integer.parseInt(args[i + 2]);
                backups.add(new Backup(backupPath, backupNumber));
                i += 2;
            }
        }
    }

    // Performs the operations
    private static void process() throws IOException {
        if (upload) {
            System.out.println(String.format("Uploading save from %s to %s", savePath, cloudPath));
            Files.copy(savePath, cloudPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
        }
        else if (download) {
            System.out.println(String.format("Downloading save from %s to %s", cloudPath, savePath));
            Files.copy(cloudPath, savePath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
        }

        for (Backup backup : backups) {
            System.out.println(String.format("Creating backup of %s at %s with %d backups", savePath, backup.path, backup.number));
        }
    }

    public static void main(String[] args) throws Exception {
        parse(args);
        process();
    }
}
