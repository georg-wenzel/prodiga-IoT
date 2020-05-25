package uibk.ac.at.prodiga.utils;

import com.google.common.io.Files;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ConfigDownloader {

    public static void downloadConfig(String password, String raspiInternalId) throws ProdigaGeneralExpectedException {
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

        FileOutputStream fos = null;
        ZipOutputStream zos = null;
        try {
            fos = new FileOutputStream(tempPath.getAbsolutePath());
            zos = new ZipOutputStream(fos);
            byte[] buffer = new byte[1024];

            FileInputStream in = null;

            for (File file: scriptFolder.listFiles()) {
                ZipEntry ze = new ZipEntry(file.getName());
                zos.putNextEntry(ze);

                try {
                    in = new FileInputStream(file.getAbsolutePath());
                    int len;
                    while ((len = in .read(buffer)) > 0) {
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
    }

}
