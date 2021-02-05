package org.funz;

import java.io.File;
import org.apache.commons.exec.OS;
import org.junit.Test;
import org.funz.util.Disk;

/**
 *
 * @author richet
 */
public class DiskTest {

    public static void main(String args[]) {
        org.junit.runner.JUnitCore.main(DiskTest.class.getName());
    }

    @Test
    public void testlistRecursiveFiles() {
        File src_main_java = new File("src/main/java");
        File[] lrs = Disk.listRecursiveFiles(src_main_java);
        boolean found = false;
        for (int i = 0; i < lrs.length; i++) {
            System.out.println(lrs[i]);
            if (lrs[i].getName().equals("Disk.java")) {
                found = true;
            }
        }
        assert found : "Not all files found interface recursive list";
    }

    @Test
    public void testIsBinary() {
        File java_src = new File("src/main/java/org/funz/util/Disk.java");
        System.err.println(java_src);
        assert !Disk.isBinary(java_src) : "bad binary inference: "+java_src;
        File java_class = new File("build/org/funz/util/Disk.class");
        System.err.println(java_class);
        assert Disk.isBinary(java_class) : "bad binary inference: "+java_class;
        File R_src = new File("src/test/resources/branin.R");
        System.err.println(R_src);
        assert !Disk.isBinary(R_src) : "bad binary inference: "+R_src;
        File castem_src = new File("src/test/resources/out.txt");
        System.err.println(castem_src);
        assert !Disk.isBinary(castem_src) : "bad binary inference: "+castem_src;

        if (OS.isFamilyWindows()) {
            File cmd = new File("C:\\Windows\\system32\\cmd.exe");
            System.err.println(cmd);
            assert Disk.isBinary(cmd) : "bad binary inference: "+cmd;
        } else {
            File bash = new File("/bin/bash");
            System.err.println(bash);
            assert Disk.isBinary(bash) : "bad binary inference: "+bash;
        }
    }
}
