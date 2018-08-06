package util;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;

/**
 * Created by chenzhi on 2018/6/20.
 */
public class StringUtil {


    public static String fileToString(String filePath) {


        String src;
        try {
            src = Files.asCharSource(new File(filePath), Charsets.UTF_8).read();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return src;
    }

}
