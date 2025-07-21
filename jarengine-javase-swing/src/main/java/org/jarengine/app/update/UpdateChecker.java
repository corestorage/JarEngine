package org.jarengine.app.update;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class UpdateChecker {
    private static final String UPDATE_URL = "https://raw.githubusercontent.com/corestorage/JarEngine/refs/heads/main/version.txt";
    private static final String DOWNLOAD_BASE_URL = "https://github.com/corestorage/JarEngine/releases/download/";

    public static String getLatestVersion() throws Exception {
        URL url = new URL(UPDATE_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            return content.toString().trim();
        } else {
            throw new RuntimeException("Failed to fetch update information. HTTP error code: " + responseCode);
        }
    }

    public static boolean isUpdateAvailable(String currentVersion, String latestVersion) {
        return latestVersion.compareTo(currentVersion) > 0;
    }

    public static void downloadUpdate(String version, File destinationFile) throws IOException {
        String downloadUrl = DOWNLOAD_BASE_URL + "v" + version + "/JarEngine-" + version + ".jar";
        URL url = new URL(downloadUrl);
        try (InputStream in = url.openStream()) {
            Files.copy(in, destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public static void applyUpdateAndRestart(File downloadedJar, String latestVersion) throws IOException {
        String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
        File currentJar;
        try {
            currentJar = new File(UpdateChecker.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (Exception e) {
            throw new IOException("Failed to get current JAR location: " + e.getMessage(), e);
        }

        String newJarName = "JarEngine-" + latestVersion + ".jar";
        File newJarFile = new File(currentJar.getParentFile(), newJarName);

        String os = System.getProperty("os.name").toLowerCase();
        boolean isWindows = os.contains("win");

        File tempScript;
        String scriptContent;
        String[] command;

        if (isWindows) {
            // Use a more robust Windows approach
            tempScript = File.createTempFile("updater", ".bat");
            scriptContent =
                    "@echo off\n" +
                    "echo Waiting for JarEngine to exit...\n" +
                    "timeout /t 3 /nobreak > NUL\n" +
                    "\n" +
                    "echo Attempting to replace JAR file...\n" +
                    "\n" +
                    "REM Try to delete existing file with force\n" +
                    "if exist \"" + newJarFile.getAbsolutePath() + "\" (\n" +
                    "    echo Removing existing file...\n" +
                    "    del /F /Q \"" + newJarFile.getAbsolutePath() + "\" 2>NUL\n" +
                    "    if exist \"" + newJarFile.getAbsolutePath() + "\" (\n" +
                    "        echo File still exists, trying alternative method...\n" +
                    "        timeout /t 2 /nobreak > NUL\n" +
                    "        del /F /Q \"" + newJarFile.getAbsolutePath() + "\" 2>NUL\n" +
                    "    )\n" +
                    ")\n" +
                    "\n" +
                    "REM Try to move the downloaded file\n" +
                    "echo Moving downloaded file...\n" +
                    "move /Y \"" + downloadedJar.getAbsolutePath() + "\" \"" + newJarFile.getAbsolutePath() + "\" >NUL 2>&1\n" +
                    "if errorlevel 1 (\n" +
                    "    echo Move failed, trying copy and delete...\n" +
                    "    copy /Y \"" + downloadedJar.getAbsolutePath() + "\" \"" + newJarFile.getAbsolutePath() + "\" >NUL 2>&1\n" +
                    "    if not errorlevel 1 (\n" +
                    "        del /F /Q \"" + downloadedJar.getAbsolutePath() + "\" >NUL 2>&1\n" +
                    "    )\n" +
                    ")\n" +
                    "\n" +
                    "REM Verify the new file exists\n" +
                    "if exist \"" + newJarFile.getAbsolutePath() + "\" (\n" +
                    "    echo Update successful! Starting JarEngine...\n" +
                    "    start \"\" /B \"" + javaBin + "\" -jar \"" + newJarFile.getAbsolutePath() + "\"\n" +
                    ") else (\n" +
                    "    echo Update failed! Starting original JarEngine...\n" +
                    "    start \"\" /B \"" + javaBin + "\" -jar \"" + currentJar.getAbsolutePath() + "\"\n" +
                    ")\n" +
                    "\n" +
                    "echo Cleaning up...\n" +
                    "timeout /t 1 /nobreak > NUL\n" +
                    "del \"" + tempScript.getAbsolutePath() + "\" >NUL 2>&1\n" +
                    "exit\n";
            command = new String[]{"cmd.exe", "/c", tempScript.getAbsolutePath()};
        } else {
            tempScript = File.createTempFile("updater", ".sh");
            tempScript.setExecutable(true);
            scriptContent =
                    "#!/bin/bash\n" +
                    "echo \"Waiting for JarEngine to exit...\"\n" +
                    "sleep 3\n" +
                    "\n" +
                    "echo \"Replacing JAR file...\"\n" +
                    "rm -f \"" + newJarFile.getAbsolutePath() + "\"\n" +
                    "mv \"" + downloadedJar.getAbsolutePath() + "\" \"" + newJarFile.getAbsolutePath() + "\"\n" +
                    "\n" +
                    "echo \"Starting updated JarEngine...\"\n" +
                    "java -jar \"" + newJarFile.getAbsolutePath() + "\" &\n" +
                    "\n" +
                    "echo \"Cleaning up...\"\n" +
                    "sleep 1\n" +
                    "rm \"" + tempScript.getAbsolutePath() + "\"\n";
            command = new String[]{"bash", tempScript.getAbsolutePath()};
        }

        try (PrintWriter pw = new PrintWriter(tempScript)) {
            pw.println(scriptContent);
        }

        new ProcessBuilder(command).start();
        System.exit(0);
    }
} 