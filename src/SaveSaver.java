import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class SaveSaver {
    private static final String USAGE_STRING = """
    usage: java -jar SaveSaver.jar <savePath> [(-u | -d) <cloudPath>] [-b <backupPath> [<backupCount>]...]

    arguments:
    \t<savePath>\tpath to the game save folder

    options:
    \t-u, --upload\t\tupload the save to the cloud. This will override the cloud save.
    \t-d, --download\t\tdownload the save from the cloud. This will override the local save.
    \t\t<cloudPath>\tpath to the cloud save folder

    \t-b, --backup\t\tcreate a backup of the save folder
    \t\t<backupPath>\twhere to save the backup to
    \t\t<backupNumber>\tnumber of rotating backups to keep at path. Setting to 0 or omitting will keep all backups.
    """;
    private static final List<String> UPLOAD_OPTIONS = List.of("-u", "--upload");
    private static final List<String> DOWNLOAD_OPTIONS = List.of("-d", "--download");
    private static final List<String> BACKUP_OPTIONS = List.of("-b", "--backup");

    private static Path savePath;
    private static boolean upload = false;
    private static boolean download = false;
    private static Path cloudPath;
    private static List<Backup> backups = new ArrayList<Backup>();

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
                if (i + 1 >= args.length) {
                    System.err.println("Missing backup path argument");
                    System.exit(1);
                }
                Path backupPath = Paths.get(args[i + 1]);
                int maxBackups = 0; // Default value
                if (i + 2 < args.length && args[i + 2].matches("\\d+")) {
                    maxBackups = Integer.parseInt(args[i + 2]);
                    i++;
                }
                backups.add(new Backup(backupPath, maxBackups));
                i++;
            }
        }
    }

    // Performs the operations
    private static void process() throws IOException {
        if (upload || download) {
            if (upload) {
                System.out.println(String.format("Uploading save from %s to %s", savePath, cloudPath));
                if (!Files.exists(cloudPath)) {
                    Files.createDirectories(cloudPath);
                }
                copyDirectory(savePath, cloudPath);
            }
            else if (download) {
                System.out.println(String.format("Downloading save from %s to %s", cloudPath, savePath));
                copyDirectory(cloudPath, savePath);
            }
        }
        
        if (!backups.isEmpty()) {
            String backupName = String.format("Backup_%s.zip", new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date()));
            Path backupPath = Paths.get(backupName);
            zipDirectory(savePath, backupPath);
            
            // Copy the zip file to each backup location and delete oldest backups if necessary
            for (Backup backup : backups) {
                System.out.println(String.format("Creating backup of %s at %s with %d backups", savePath, backup.path, backup.max));
                if (!Files.exists(backup.path)) {
                    Files.createDirectories(backup.path);
                }
                Path backupDestination = backup.path.resolve(backupName);
                Files.copy(backupPath, backupDestination);

                // Delete oldest backups if necessary
                if (backup.max > 0) {
                    try {
                        // Get all backup files with matching name pattern, sorted by last modified time
                        List<Path> backupFiles = Files.list(backup.path)
                            .filter(path -> path.getFileName().toString().matches("Backup_\\d{4}-\\d{2}-\\d{2}_\\d{2}-\\d{2}-\\d{2}\\.zip"))
                            .sorted((p1, p2) -> {
                                try {
                                    return Files.getLastModifiedTime(p1).compareTo(Files.getLastModifiedTime(p2));
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            })
                            .toList();
                        
                        // Delete oldest backups until the number of backups is less than the maximum
                        while (backupFiles.size() > backup.max) {
                            Files.delete(backupFiles.get(0));
                            backupFiles.remove(0);
                        }
                    } catch (IOException e) {
                        System.err.println("Error managing backup files: " + e.getMessage());
                    }
                }
            }

            Files.delete(backupPath);
        }
    }

    // Copies a directory and its contents to another directory
    private static void copyDirectory(Path sourceFolder, Path destinationFolder) throws IOException {
        Files.walk(sourceFolder).forEach(source -> {
            // Combines the destinationFolder path with the relative path extracted from the source path to create the full destination path
            Path destination = Paths.get(destinationFolder.toString(), source.toString().substring(sourceFolder.toString().length()));
            try {
                Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    // Creates a backup of a directory and its contents
    private static void zipDirectory(Path sourceDirPath, Path zipFilePath) {
        try(ZipOutputStream zipOut = new ZipOutputStream(Files.newOutputStream(zipFilePath))) {
            // Walks through the source directory and adds each file to the zip file
            Files.walk(sourceDirPath)
                .filter(path -> !Files.isDirectory(path))
                .forEach(path -> {
                ZipEntry zipEntry = new ZipEntry(sourceDirPath.relativize(path).toString());
                try {
                    zipOut.putNextEntry(zipEntry);
                    Files.copy(path, zipOut);
                    zipOut.closeEntry();
                } catch (IOException e) {
                    System.err.println(e);
                }
            });
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    public static void main(String[] args) throws Exception {
        parse(args);
        process();
    }
}
