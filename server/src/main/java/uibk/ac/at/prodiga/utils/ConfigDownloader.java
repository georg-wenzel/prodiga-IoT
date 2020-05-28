package uibk.ac.at.prodiga.utils;

import com.google.common.io.Files;
import org.springframework.util.StringUtils;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ConfigDownloader {

    public static File downloadConfig(String password, String raspiInternalId) throws Exception {
        if(StringUtils.isEmpty(password)) {
            throw new ProdigaGeneralExpectedException("Cannot download config with empty password", MessageType.ERROR);
        }

        File scriptFolder = new File("./../client/script/");

        if(!scriptFolder.exists()) {
            throw new ProdigaGeneralExpectedException("Script folder does not exist on path "
                    + scriptFolder.getAbsolutePath(), MessageType.ERROR);
        }

        File tempPath = new File(Files.createTempDir() + File.separator + raspiInternalId + ".zip");

        if(tempPath.exists()) {
            tempPath.delete();
        }

        String ipAddress = Constants.getIpAddress();

        FileOutputStream fos = null;
        ZipOutputStream zos = null;
        try {
            fos = new FileOutputStream(tempPath.getAbsolutePath());
            zos = new ZipOutputStream(fos);
            byte[] buffer = new byte[1024];

            FileInputStream in = null;

            for (File file: scriptFolder.listFiles()) {
                String fileName = file.getName();

                if(fileName.equals("start_client.sh")) {
                    String content = getFileContent(file);
                    if(content != null) {
                        content = content.replaceAll("@ipAddress", ipAddress);
                        content = content.replaceAll("@password", password);
                    }

                    file = new File(tempPath.getParentFile().getAbsolutePath() + File.separator + "start_client.sh");

                    if(!file.exists()) {
                        file.createNewFile();
                    }

                    writeFileContent(file, content);
                }

                ZipEntry ze = new ZipEntry(fileName);
                zos.putNextEntry(ze);

                try {
                    in = new FileInputStream(file.getAbsolutePath());
                    int len;
                    while ((len = in.read(buffer)) > 0) {
                        zos.write(buffer, 0, len);
                    }
                } finally {
                    in.close();
                }
            }

            zos.closeEntry();
        } catch (IOException ex) {
            throw new ProdigaGeneralExpectedException("Error while creating zip File\n" + ex.toString(), MessageType.ERROR);
        } finally {
            try {
                zos.close();
            } catch (IOException e) {
                // Ignore
            }
        }

        return tempPath;
    }

    private static String getFileContent(File f) throws Exception {
        if(f == null) {
            return null;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(f.getAbsolutePath()))) {
            String line = null;
            StringBuilder stringBuilder = new StringBuilder();
            String ls = System.getProperty("line.separator");
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append(ls);
            }

            return stringBuilder.toString();
        }
    }

    private static void writeFileContent(File f, String content) throws Exception {
        if(content == null || f == null) {
            return;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(f))) {
            writer.write(content);
        }

    }
}
