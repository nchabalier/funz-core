package org.funz;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.Test;
import org.funz.util.Parser;

/**
 *
 * @author richet
 */
public class ParserTest {

    public static void main(String args[]) {
        org.junit.runner.JUnitCore.main(ParserTest.class.getName());
    }

    public ParserTest() {
    }

    @Test
    public void testManyGrep() throws InterruptedException {
        System.err.println("+++++++++++++++++++++++++++++++++++++++++++++ testManyGrep");
        File src = new File("src/main/java/org/funz/util/Parser.java");
        assert src.isFile() : "Cannot find "+src;

        for (int i = 0; i < 10; i++) {
            Thread.sleep(50);
            System.err.println(Parser.grep(src, "public static (.*)"));
        }
    }

    @Test
    public void testBinaryNotBlocking() {
        System.err.println("+++++++++++++++++++++++++++++++++++++++++++++ testBinaryNotBlocking");
        File dir = new File("src/test/resources/");
        System.err.println(new Parser(new File(dir, "sheet.xlsx"), new File(dir, "Excel.vbs")).contains("(.*)vbs", "WriteLine"));
    }

    public static void zip(File f, File zip) {
        byte[] buffer = new byte[1024];

        try {

            FileOutputStream fos = new FileOutputStream(zip);
            ZipOutputStream zos = new ZipOutputStream(fos);
            FileInputStream in = new FileInputStream(f);

            ZipEntry e = new ZipEntry(f.getName());
            zos.putNextEntry(e);

            int len;
            while ((len = in.read(buffer)) > 0) {
                zos.write(buffer, 0, len);
            }

            in.close();
            zos.closeEntry();

            //remember close it
            zos.close();

            System.out.println("Zip done");

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Test
    public void testZip() {
        System.err.println("+++++++++++++++++++++++++++++++++++++++++++++ testZip");
        File src = new File("src/main/java/org/funz/util/ASCII.java");
        File out = new File("ASCII.zip");
        zip(src, out);

        assert new Parser(new File(".").listFiles()).grep("(.*)zip", "public static (.*)").size() > 0 : "No file found";
    }

    @Test
    public void testCSV() {
        System.err.println("+++++++++++++++++++++++++++++++++++++++++++++ testCSV");
        Parser p = new Parser(new File("src/test/resources/Bilan_auto_U_nat.txt"));
        List<String> l = p.lines();
        System.err.println("lines:\n" + l);
        l = p.CSV(l, "\\|", "Entr(\\S+)es Cumul(\\S+)es \\(t\\)");
        assert l != null && l.size() > 3 : "Failed to parse with CSV() " + l;
        double[] vals = p.asNumeric1DArray(Parser.getBy(l, 3, 1));
        assert vals[2] == 1000000 : "Failed to get 1000000 in " + l;
    }

    @Test
    public void testCSV_() {
        System.err.println("+++++++++++++++++++++++++++++++++++++++++++++ testCSV_");
        Parser p = new Parser(new File("src/test/resources/Bilan_auto_U_nat.txt"));
        List<String> l = p.lines();
        System.err.println("lines:\n" + l);
        l = p.CSV(l, "\\|", "   Entr(\\S+)es Cumul(\\S+)es \\(t\\)");
        assert l != null && l.size() > 3 : "Failed to parse with CSV() " + l;
        double[] vals = p.asNumeric1DArray(Parser.getBy(l, 3, 1));
        assert vals[2] == 1000000 : "Failed to get 1000000 in " + l;
    }
    
    @Test
    public void testCSV0() {
        System.err.println("+++++++++++++++++++++++++++++++++++++++++++++ testCSV0");
        Parser p = new Parser(new File("src/test/resources/Bilan_auto_U_nat.txt"));
        List<String> l = p.lines();
        System.err.println("lines:\n" + l);
        l = p.CSV(l, "\\|", "Ann");
        assert l != null && l.size() > 3 : "Failed to parse with CSV() " + l;
        double[] vals = p.asNumeric1DArray(Parser.getBy(l, 3, 1));
        assert vals[0] == 1998 : "Failed to get 1998 in " + l+" : "+vals[0];
    }    
        
    @Test
    public void testCSV0_() {
        System.err.println("+++++++++++++++++++++++++++++++++++++++++++++ testCSV0_");
        Parser p = new Parser(new File("src/test/resources/Bilan_auto_U_nat.txt"));
        List<String> l = p.lines();
        System.err.println("lines:\n" + l);
        l = p.CSV(l, "\\|", "     Ann");
        assert l != null && l.size() > 3 : "Failed to parse with CSV() " + l;
        double[] vals = p.asNumeric1DArray(Parser.getBy(l, 3, 1));
        assert vals[0] == 1998 : "Failed to get 1998 in " + l+" : "+vals[0];
    }

    @Test
    public void testCSVMap() {
        System.err.println("+++++++++++++++++++++++++++++++++++++++++++++ testCSVMap");
        Parser p = new Parser(new File("src/test/resources/Bilan_auto_U_nat.txt"));
        List<String> l = Parser.skip(p.lines(),7);
        System.err.println("lines:\n" + l);
        Map<String,double[]> m = p.CSV(l, "\\|");
        assert m != null && m.size() > 3 : "Failed to parse with CSV() " + m;
        System.err.println("m.keySet(): "+m.keySet());
        Object k0 = m.keySet().toArray()[1];
        double[] vals = m.get(k0);
        assert vals[2] == 1998.0 : "Failed to get 1998 in " + m+" : "+vals[2] ;
    }

    @Test
    public void testAfterBefore() {
        System.err.println(Parser.after("abcdefabcdef", "bc"));
        assert Parser.after("abcdefabcdef", "bc").equals("defabcdef") : "Failed after";
        assert Parser.after("abcdefabcdef", "zz").equals("abcdefabcdef") : "Failed after";
        assert Parser.before("abcdefabcdef", "bc").equals("a") : "Failed before: "+Parser.before("abcdefabcdef", "bc");
        assert Parser.before("abcdefabcdef", "zz").equals("abcdefabcdef") : "Failed before: "+Parser.before("abcdefabcdef", "zz");
    }

    @Test
    public void testUnquote() {
        assert Parser.unquote("'abcdefabcdef'").equals("abcdefabcdef") : "Failed unquote: "+Parser.unquote("'abcdefabcdef'");
        assert Parser.unquote("'abcdefabcdef").equals("'abcdefabcdef") : "Failed unquote: "+Parser.unquote("'abcdefabcdef");
        assert Parser.unquote("abcdefabcdef'").equals("abcdefabcdef'") : "Failed unquote: "+Parser.unquote("abcdefabcdef'");
        assert Parser.unquote("\"abcdefabcdef\"").equals("abcdefabcdef") : "Failed unquote: "+Parser.unquote("\"abcdefabcdef\"");
        assert Parser.unquote("\"abcdefabcdef").equals("\"abcdefabcdef") : "Failed unquote: "+Parser.unquote("\"abcdefabcdef");
        assert Parser.unquote("abcdefabcdef\"").equals("abcdefabcdef\"") : "Failed unquote: "+Parser.unquote("abcdefabcdef\"");
    }
}
