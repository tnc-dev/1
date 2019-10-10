package my.mkt;

import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;


public class ParseData {


        public static void parseData(){
        try {
             SevenZFile sevenZFile;
            File apath = new File(Environment.getDataDirectory()+"/NewTextFile.7z");

            // Проверка существования.
            System.out.println("Path exists? " + apath.exists());

            sevenZFile = new SevenZFile(apath);
            SevenZArchiveEntry entry = sevenZFile.getNextEntry();
            while(entry!=null){
                System.out.println(entry.getName());
                FileOutputStream out = new FileOutputStream(entry.getName());
                byte[] content = new byte[(int) entry.getSize()];
                sevenZFile.read(content, 0, content.length);
                out.write(content);
                out.close();
                entry = sevenZFile.getNextEntry();
            }
            sevenZFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}

