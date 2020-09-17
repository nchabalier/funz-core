/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.funz;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import org.apache.commons.exec.OS;
import org.junit.Test;
import org.funz.util.Disk;
import org.funz.util.ParserUtils;

/**
 *
 * @author richet
 */
public class ProcessTest {

    //System.out.println(cat(",", splitSpacesNoQuote("C:\\\"Program Files\"\\totot")));
    //System.out.println(cat(",", splitSpacesNoQuote("\"C:\\Program Files\"\\totot")));
    //System.out.println(new Process("/bin/bash -c ls.sh", null, null).runCommand());
    //System.out.println(new Process("./ls.sh", null, null).runCommand());
    //System.out.println(new Process("echo 2", null, null).runCommand());
    //System.out.println(new Process("echo 3", null, null).runCommand(new FileOutputStream("out3.txt"), new FileOutputStream("err3.txt"), new FileOutputStream("log3.txt")));
    //System.out.println(new Process("./ls.sh", null, null).runCommand(new FileOutputStream("out4.txt"), new FileOutputStream("err4.txt"), new FileOutputStream("log4.txt")));
    //System.out.println(new Process("dir", null, null).runCommand());
    //System.out.println(new Process("dir.bat", null, null).runCommand());
    public static void main(String args[]) {
        org.junit.runner.JUnitCore.main(ProcessTest.class.getName());
    }

    @Test
    public void testBadCommand() {
        System.err.println("+++++++++++++++++++++++++++++++ testBadCommand");
        org.funz.util.Process fail = new org.funz.util.Process("ThisIsABadCommand", new File("tmp"), null);
        try {
            System.err.println(fail.runCommand(System.out, System.err, System.err));
        } catch (Exception ex) {
            assert false : ex.getMessage();
        }

        assert fail.getFailReason().contains("ThisIsABadCommand") : "Failed to get error:" + fail.getFailReason();
    }

    @Test
    public void testOk() {
        System.err.println("+++++++++++++++++++++++++++++++ testOk");
        org.funz.util.Process ok = new org.funz.util.Process("R CMD BATCH ok.R", new File("tmp"), null);
        try {
            Disk.copyFile(new File("src/test/resources/ok.R"), new File("tmp", "ok.R"));
        } catch (IOException ex) {
            assert false : ex;
        }
        try {
            int r = ok.runCommand(System.out, System.err, System.err);
            assert r == 0 : "Bad return status !=0 " + r;
        } catch (Exception ex) {
            assert false : ex.getMessage();
        }
    }

    @Test
    public void testUsrBinEnv() {
        System.err.println("+++++++++++++++++++++++++++++++ testUsrBinEnv");
        org.funz.util.Process ok = new org.funz.util.Process("./src/test/resources/ok.py", new File("tmp"), null);
        try {
            int r = ok.runCommand(System.out, System.err, System.err);
            assert r == 0 : "Bad return status "+r+" !=0 ";
        } catch (Exception ex) {
            assert false : ex.getMessage();
        }
    }

    @Test
    public void testUsrBinEnv_Args() {
        System.err.println("+++++++++++++++++++++++++++++++ testUsrBinEnv_Args");
        org.funz.util.Process ok = new org.funz.util.Process("./src/test/resources/ok.py toto", new File("tmp"), null);
        try {
            int r = ok.runCommand(System.out, System.err, System.err);
            assert r == 0 : "Bad return status !=0 " + r;
        } catch (Exception ex) {
            assert false : ex.getMessage();
        }
    }

    @Test
    public void testExit1() {
        System.err.println("+++++++++++++++++++++++++++++++ testExit1");
        org.funz.util.Process fail = null;
        if (OS.isFamilyWindows()) {
            fail = new org.funz.util.Process("src\\test\\resources\\exit1.bat", new File("tmp"), null);
        } else {
            fail = new org.funz.util.Process("./src/test/resources/exit1.sh", new File("tmp"), null);
        }
        try {
            int ret = fail.runCommand(System.out, System.err, System.err);
            assert ret == 1 : "Bad return status "+ret+" !=1";
        } catch (Exception ex) {
            assert false : ex.getMessage();
        }
    }

    @Test
    public void testCrash() {
        System.err.println("+++++++++++++++++++++++++++++++ testCrash");
        org.funz.util.Process fail = null;
        if (OS.isFamilyWindows()) {
            fail = new org.funz.util.Process("src\\test\\resources\\crash.bat", new File("tmp"), null);
        } else {
            fail = new org.funz.util.Process("./src/test/resources/crash.sh", new File("tmp"), null);
        }
        try {
            int ret = fail.runCommand(System.out, System.err, System.err);
            assert ret != 0 : "Bad return status == 0";
        } catch (Exception ex) {
            assert false : ex.getMessage();
        }
    }

    @Test
    public void testEcho1() {
        System.err.println("+++++++++++++++++++++++++++++++ testEcho1");
        org.funz.util.Process p = new org.funz.util.Process("echo 1", new File("tmp"), null);
        try {
            FileOutputStream o = new FileOutputStream("out1.txt");
            FileOutputStream e = new FileOutputStream("err1.txt");
            FileOutputStream l = new FileOutputStream("log1.txt");

            p.runCommand(o, e, l);
        } catch (Exception ex) {
            assert false : ex.getMessage();
        }

        assert ParserUtils.getASCIIFileContent(new File("out1.txt")).contains("1") : "Bad out stream:" + ParserUtils.getASCIIFileContent(new File("out1.txt"));
    }

    @Test
    public void testBashWithArgs() {
        System.err.println("+++++++++++++++++++++++++++++++ testCrash");
        if (OS.isFamilyWindows()) {
            return; // Do not test bash for windows
        }
        org.funz.util.Process bash = new org.funz.util.Process("./src/test/resources/mult.sh -0.24995 0.25000000000000006", new File("tmp"), null);
        try {
            int r = bash.runCommand(System.out, System.err, System.err);
            assert r == 0 : "Bad return status !=0 " + r;
        } catch (Exception ex) {
            assert false : ex.getMessage();
        }
    }
}
