package org.funz.util;

import com.jayway.jsonpath.JsonPath;
import org.math.io.parser.ArrayString;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.funz.util.ASCII.*;

/**
 * Conveninence methods to emulate ParserUtils parsing functions like grep, cut,
 * split, ...
 */
public class Parser {

    public static final String ANY = "(.*)";
    public File[] files = new File[0];
    public String filefilter = ANY;

    public Parser(File... _files) {
        files = _files;
    }

    public void setFilenameFilter(String regexp) {
        filefilter = regexp;
    }

    public void removeFilenameFilter() {
        filefilter = ANY;
    }

    /**
     * find matching filename regexp files
     *
     * @param filefilter filename fileter regexp
     * (http://java.sun.com/j2se/1.4.2/docs/api/java/util/regex/Pattern.html)
     * @return LinkedList<File> list of files
     */
    public List<File> find(String filefilter) {
        LinkedList<File> gf = new LinkedList<File>();
        for (File f : files) {
            if (f.getName().matches(filefilter)) {
                if (f.getName().endsWith(".zip")) {
                    gf.addAll(unzip(f));
                } else {
                    gf.add(f);
                }
            }
        }
        return gf;
    }

    public static List<File> unzip(File z) {
        List<File> files = new LinkedList<File>();
        try {
            final ZipFile zipFile = new ZipFile(z);
            final Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                final ZipEntry zipEntry = entries.nextElement();
                if (!zipEntry.isDirectory()) {
                    InputStream input = zipFile.getInputStream(zipEntry);
                    File out = new File(zipEntry.getName());
                    saveFile(out, InputStreamToString(input));
                    input.close();
                    files.add(out);
                }
            }
            zipFile.close();
        } catch (final IOException ioe) {
            ioe.printStackTrace();
        }
        return files;
    }

    /*public List<NodeList> xml(String filefilter) {
     List<NodeList> gf = new LinkedList<NodeList>();
     for (File f : files) {
     if (f.getName().matches(filefilter)) {
     DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
     try{
     DocumentBuilder builder = factory.newDocumentBuilder();
     Document document = builder.parse(f.getAbsolutePath());
     Element racine = document.getDocumentElement();
     NodeList racineNoeuds = racine.getChildNodes();
     gf.add(racineNoeuds);}catch(Exception e){e.printStackTrace();}
     }
     }
     return gf;
     }
     public List<NodeList> nodes(List<NodeList> nodes,String name,String... attributes){
     List<NodeList> next_nodes = new LinkedList<NodeList>();
     for (int i = 0; i < nodes.size(); i++) {
     next_nodes.add(nodes(nodes.get(i),name,attributes));
     }
     return next_nodes;
     }
     public NodeList nodes(NodeList nodes,String name,String... attributes){
     NodeList next_nodes=  new
     for (int i = 0; i < nodes.getLength(); i++) {
     Node n = nodes.item(i);
     if (n.getNodeName().equals(name)){
     }
     }
     }*/
    public static List<String> CSV(List<String> lines, String coldelim, String column) {
        LinkedList<String> p = new LinkedList<String>();
        int i = -1;
        for (String line : lines) {
            if (i == -1) {
                if (line.matches("(.*)"+column+"(.*)")) {
                    String bef = line.split(column)[0]; //substring(0, line.indexOf(title));
                    i = bef.split(coldelim).length - 1;
                    if (bef.matches(".*" + coldelim + "$")) { //ends with delim, so count one more
                        i = i + 1;
                        if (bef.matches(coldelim + "$")) { // starts with delim and first pos, so add 1 also
                            i = i + 1;
                        }
                    }
                }
            } else {
                try {
                    p.add(line.split(coldelim)[i]);
                } catch (Exception e) {
                    //Nothing to do
                }
            }
        }
        return p;
    }
    public List<String> CSV(String filefilter, String coldelim, String column) {
        return CSV( lines(filefilter),coldelim, column);
    }

    public static Map<String,double[]> CSV(List<String> lines, String coldelim) {
        String[] titles = lines.get(0).split(coldelim);
        double[][] values = new double[titles.length][lines.size()-1];
        for (int j = 0; j < lines.size()-1; j++) {
            String[] line_vals = lines.get(j+1).split(coldelim);
            if (line_vals!=null && line_vals.length==titles.length)
            for (int i = 0; i < titles.length; i++) {
                values[i][j] = asNumeric(line_vals[i]);
            }
        }
        Map m = new HashMap<>(titles.length);
        for (int i = 0; i < titles.length; i++) {
            m.put(titles[i].trim(), values[i]);
        }
        return m;
    }
    public Map<String,double[]> CSV(String filefilter, String coldelim) {
        return CSV( lines(filefilter),coldelim);
    }

    public List<String> JSONPath(String filefilter, String path) {
        LinkedList<String> p = new LinkedList<String>();
        List<File> fs = find(filefilter);
        if (fs.size() == 0) {
            return p;
        }
        for (File f : fs) {
            String value = JSONPath(f, path);
            if (value != null) {
                p.add(value);
            }
        }
        return p;
    }

    public static String JSONPath(File file, String path) {
        try {
            return asString(JsonPath.read(file, path));
        } catch (IOException ex) {
            return ex.getMessage();
        }
    }

    public List<String> XPath(String filefilter, String path) {
        LinkedList<String> p = new LinkedList<String>();
        List<File> fs = find(filefilter);
        if (fs.size() == 0) {
            return p;
        }
        for (File f : fs) {
            String value = XPath(f, path);
            if (value != null) {
                p.add(value);
            }
        }
        return p;
    }

    public static String XPath(File file, String path) {
        try {
            Document xmlDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
            XPath xPath = XPathFactory.newInstance().newXPath();
            XPathExpression xPathExpression = xPath.compile(path);
            NodeList nodes = (NodeList) xPathExpression.evaluate(xmlDocument, XPathConstants.NODESET);
            Document newXmlDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                Node copyNode = newXmlDocument.importNode(node, true);
                newXmlDocument.appendChild(copyNode);
            }
            return toString(newXmlDocument);
        } catch (Exception ex) {
            return ex.getMessage();
        }
    }

    public static String toString(Document xml) {
        DOMImplementationLS domImplementationLS = (DOMImplementationLS) xml.getImplementation();
        LSSerializer lsSerializer = domImplementationLS.createLSSerializer();
        return lsSerializer.writeToString(xml);
    }

    public static List<String> toStrings(List<Document> xml) {
        LinkedList<String> ss = new LinkedList<String>();
        for (int i = 0; i < xml.size(); i++) {
            ss.add(toString(xml.get(i)));
        }
        return ss;
    }

    /**
     * find matching files and concatenante content
     *
     * @param filefilter filename fileter regexp
     * (http://java.sun.com/j2se/1.4.2/docs/api/java/util/regex/Pattern.html)
     * @return String concatenation of ParserUtils file content
     */
    public String filecat(String filefilter) {
        StringBuilder sb = new StringBuilder();
        for (File f : find(filefilter)) {
            sb.append(filecat(f)).append("\n");
        }
        return sb.toString();
    }

    public String cat(String filefilter) {
        return filecat(filefilter);
    }

    /**
     * read ParserUtils content of a file
     *
     * @param f File
     * @return String ParserUtils file content
     */
    public static String filecat(File f) {
        return ParserUtils.getASCIIFileContent(f);
    }

    public String cat(File f) {
        return filecat(f);
    }

    /**
     * concatenante content of files
     *
     * @return String concatenation of ParserUtils file content
     */
    public String filecat() {
        return filecat(filefilter);
    }

    public String cat() {
        return filecat();
    }

    /**
     * list files with matching name given in regexp
     *
     * @param filefilter filename fileter regexp
     * (http://java.sun.com/j2se/1.4.2/docs/api/java/util/regex/Pattern.html)
     * @return String list of filenames (one per line)
     */
    public String ls(String filefilter) {
        String l = "";
        for (File f : files) {
            if (f.getName().matches(filefilter)) {
                l += f.getName() + "\n";
            }
        }
        return l;
    }

    /**
     * list files
     *
     * @return String list of filenames (one per line)
     */
    public String ls() {
        return ls(filefilter);
    }

    /**
     * reads a property in properties files matching given file name regexp
     *
     * @param filefilter filename filter regexp
     * (http://java.sun.com/j2se/1.4.2/docs/api/java/util/regex/Pattern.html)
     * @param key property key to read
     * @return list of values taken in selected files
     */
    public List<String> property(String filefilter, String key) throws MalformedURLException, IOException {
        LinkedList<String> p = new LinkedList<String>();
        List<File> fs = find(filefilter);
        if (fs.size() == 0) {
            return p;
        }
        for (File f : fs) {
            String value = property(f, key);
            if (value != null) {
                p.add(value);
            }
        }
        return p;
    }

    /**
     * reads a property in a properties file
     *
     * @param f properties file to read
     * @param key property key to read
     * @return value taken in file
     */
    public static String property(File f, String key) throws MalformedURLException, IOException {
        if (!f.isFile()) {
            return null;
        }
        URL url = f.toURI().toURL();
        Properties _properties = new Properties();
        _properties.load(url.openConnection().getInputStream());
        if (_properties.containsKey(key)) {
            return _properties.getProperty(key);
        } else {
            return null;
        }
    }

    /**
     * reads a property in properties files
     *
     * @param key property key to read
     * @return list of values taken in selected files
     */
    public LinkedList<String> property(String key) throws MalformedURLException, IOException {
        return property(filefilter);
    }

    /**
     * find String in ParserUtils files matching given file name regexp
     *
     * @param filefilter filename filter regexp
     * (http://java.sun.com/j2se/1.4.2/docs/api/java/util/regex/Pattern.html)
     * @param keyfilter String to find
     * @return list of lines taken from selected files
     */
    public List<String> grep(String filefilter, String keyfilter) {
        LinkedList<String> p = new LinkedList<String>();
        List<File> fs = find(filefilter);
        if (fs.size() == 0) {
            return p;
        }
        for (File f : fs) {
            p.addAll(grep(f, keyfilter));
        }
        return p;
    }

    /**
     * find String in ParserUtils files
     *
     * @param keyfilter String to find
     * @return list of lines taken from selected files
     */
    public List<String> grep(String keyfilter) {
        return grep(filefilter, keyfilter);
    }

    /**
     * find String in ParserUtils file
     *
     * @param file ParserUtils file to read
     * @param keyfilter String to find
     * @return lines taken in file
     */
    public static List<String> grep(File file, String keyfilter) {
        if (!file.isFile()) {
            return null;
        }
        java.io.BufferedReader inn = null;
        InputStreamReader isr = null;
        FileInputStream fis = null;
        List<String> ret = null;
        try {
            fis = new FileInputStream(file);
            isr = new InputStreamReader(fis, CHARSET);
            inn = new BufferedReader(isr);
            ret = grep(inn, keyfilter);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fis.close();
                isr.close();
                inn.close();
            } catch (Exception ee) {
                ee.printStackTrace();
            }
        }
        return ret;
    }

    /**
     * find String in ParserUtils file
     *
     * @param input ParserUtils content to read
     * @param keyfilter String to find
     * @return lines taken in file
     */
    public static List<String> grepIn(String input, String keyfilter) {
        if (input == null) {
            return null;
        }
        java.io.BufferedReader inn = null;
        InputStreamReader isr = null;
        InputStream fis = null;
        List<String> ret = null;
        try {
            fis = new ByteArrayInputStream(input.getBytes(CHARSET));
            isr = new InputStreamReader(fis, CHARSET);
            inn = new BufferedReader(isr);
            ret = grep(inn, keyfilter);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fis.close();
                isr.close();
                inn.close();
            } catch (Exception ee) {
                ee.printStackTrace();
            }
        }
        return ret;
    }

    /**
     * find String in buffer
     *
     * @param inn buffer to read
     * @param keyfilter String to find
     * @return lines taken in file
     */
    public static List<String> grep(BufferedReader inn, String keyfilter) {
        if (keyfilter.contains("(") || keyfilter.contains(")") || keyfilter.contains("{") || keyfilter.contains("}") || keyfilter.contains("+") || keyfilter.contains("?") || keyfilter.contains("*") || keyfilter.contains("$") || keyfilter.contains("^") || keyfilter.contains("|") || keyfilter.contains(".") || keyfilter.contains("\\")) {
            LinkedList<String> lines = new LinkedList<String>();
            String tmp;
            Matcher m;
            Pattern p = Pattern.compile(ANY + keyfilter + ANY);
            try {
                while ((tmp = inn.readLine()) != null) {
                    m = p.matcher(tmp);
                    if (m.find()) {
                        lines.add(tmp);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return lines;
        } else {
            return grep_basic(inn, keyfilter);
        }
    }

    /**
     * find String in buffer
     *
     * @param inn buffer to read
     * @param keyfilter String to find, not a regexp
     * @return lines taken in file
     */
    public static List<String> grep_basic(BufferedReader inn, String keyfilter) {
        LinkedList<String> lines = new LinkedList<String>();
        String tmp;
        try {
            while ((tmp = inn.readLine()) != null) {
                if (tmp.contains(keyfilter)) {
                    lines.add(tmp);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }

    /**
     * find String in ParserUtils files matching given file name regexp
     *
     * @param filefilter filename filter regexp
     * (http://java.sun.com/j2se/1.4.2/docs/api/java/util/regex/Pattern.html)
     * @param keyfilter String to find
     * @return list of lines taken from selected files
     */
    public List<String> grep_basic(String filefilter, String keyfilter) {
        LinkedList<String> p = new LinkedList<String>();
        List<File> fs = find(filefilter);
        if (fs.size() == 0) {
            return p;
        }
        for (File f : fs) {
            p.addAll(grep_basic(f, keyfilter));
        }
        return p;
    }

    /**
     * find String in ParserUtils file
     *
     * @param file ParserUtils file to read
     * @param keyfilter String to find
     * @return lines taken in file
     */
    public static List<String> grep_basic(File file, String keyfilter) {
        if (!file.isFile()) {
            return null;
        }
        java.io.BufferedReader inn = null;
        InputStreamReader isr = null;
        FileInputStream fis = null;
        List<String> ret = null;
        try {
            fis = new FileInputStream(file);
            isr = new InputStreamReader(fis, CHARSET);
            inn = new BufferedReader(isr);
            ret = grep_basic(inn, keyfilter);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fis.close();
                isr.close();
                inn.close();
            } catch (Exception ee) {
                ee.printStackTrace();
            }
        }
        return ret;
    }

    /**
     * find String in buffer
     *
     * @param inn buffer to read
     * @param keyfilter String to find
     * @param linesAfter selecting the line 'number of lines after the regex match'
     * @return lines taken in file
     */
    public static List<String> grep_after(BufferedReader inn, String keyfilter, int linesAfter) {
        if (keyfilter.contains("(") || keyfilter.contains(")") || keyfilter.contains("{") || keyfilter.contains("}") || keyfilter.contains("+") || keyfilter.contains("?") || keyfilter.contains("*") || keyfilter.contains("$") || keyfilter.contains("^") || keyfilter.contains("|") || keyfilter.contains(".") || keyfilter.contains("\\")) {
            LinkedList<String> lines = new LinkedList<String>();
            String tmp;
            Matcher m;
            Pattern p = Pattern.compile(ANY + keyfilter + ANY);
            try {
                int line = 0;
                Set<Integer> linesIdxToAdd = new HashSet<>();
                while ((tmp = inn.readLine()) != null) {
                    m = p.matcher(tmp);
                    if (m.find()) {
                        linesIdxToAdd.add(line+linesAfter);
                    }
                    if(linesIdxToAdd.contains(line)) {
                        lines.add(tmp);
                        linesIdxToAdd.remove(line);
                    }
                    line++;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return lines;
        } else {
            return grep_basic_after(inn, keyfilter, linesAfter);
        }
    }

    /**
     * find String in ParserUtils files matching given file name regexp
     *
     * @param filefilter filename filter regexp
     * (http://java.sun.com/j2se/1.4.2/docs/api/java/util/regex/Pattern.html)
     * @param keyfilter String to find
     * @param linesAfter selecting the line 'number of lines after the regex match'
     * @return list of lines taken from selected files
     */
    public List<String> grep_after(String filefilter, String keyfilter, int linesAfter) {
        LinkedList<String> p = new LinkedList<String>();
        List<File> fs = find(filefilter);
        if (fs.size() == 0) {
            return p;
        }
        for (File f : fs) {
            p.addAll(grep_after(f, keyfilter, linesAfter));
        }
        return p;
    }

    /**
     * find String in ParserUtils file
     *
     * @param file ParserUtils file to read
     * @param keyfilter String to find
     * @param linesAfter selecting the line 'number of lines after the regex match'
     * @return lines taken in file
     */
    public static List<String> grep_after(File file, String keyfilter, int linesAfter) {
        if (!file.isFile()) {
            return null;
        }
        java.io.BufferedReader inn = null;
        InputStreamReader isr = null;
        FileInputStream fis = null;
        List<String> ret = null;
        try {
            fis = new FileInputStream(file);
            isr = new InputStreamReader(fis, CHARSET);
            inn = new BufferedReader(isr);
            ret = grep_after(inn, keyfilter, linesAfter);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fis.close();
                isr.close();
                inn.close();
            } catch (Exception ee) {
                ee.printStackTrace();
            }
        }
        return ret;
    }


    /**
     * find String in buffer
     *
     * @param inn buffer to read
     * @param keyfilter String to find, not a regexp
     * @param linesAfter selecting the line 'number of lines after the regex match'
     * @return lines taken in file
     */
    public static List<String> grep_basic_after(BufferedReader inn, String keyfilter, int linesAfter) {
        LinkedList<String> lines = new LinkedList<String>();
        String tmp;
        Set<Integer> linesIdxToAdd = new HashSet<>();
        try {
            int line = 0;
            while ((tmp = inn.readLine()) != null) {
                if (tmp.contains(keyfilter)) {
                    linesIdxToAdd.add(line+linesAfter);
                }
                if(linesIdxToAdd.contains(line)) {
                    lines.add(tmp);
                    linesIdxToAdd.remove(line);
                }
                line++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }

    /**
     * find String in ParserUtils files matching given file name regexp
     *
     * @param filefilter filename filter regexp
     * (http://java.sun.com/j2se/1.4.2/docs/api/java/util/regex/Pattern.html)
     * @param keyfilter String to find
     * @param linesAfter selecting the line 'number of lines after the regex match'
     * @return list of lines taken from selected files
     */
    public List<String> grep_basic_after(String filefilter, String keyfilter, int linesAfter) {
        LinkedList<String> p = new LinkedList<String>();
        List<File> fs = find(filefilter);
        if (fs.size() == 0) {
            return p;
        }
        for (File f : fs) {
            p.addAll(grep_basic_after(f, keyfilter, linesAfter));
        }
        return p;
    }

    /**
     * find String in ParserUtils file
     *
     * @param file ParserUtils file to read
     * @param keyfilter String to find
     * @param linesAfter selecting the line 'number of lines after the regex match'
     * @return lines taken in file
     */
    public static List<String> grep_basic_after(File file, String keyfilter, int linesAfter) {
        if (!file.isFile()) {
            return null;
        }
        java.io.BufferedReader inn = null;
        InputStreamReader isr = null;
        FileInputStream fis = null;
        List<String> ret = null;
        try {
            fis = new FileInputStream(file);
            isr = new InputStreamReader(fis, CHARSET);
            inn = new BufferedReader(isr);
            ret = grep_basic_after(inn, keyfilter, linesAfter);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fis.close();
                isr.close();
                inn.close();
            } catch (Exception ee) {
                ee.printStackTrace();
            }
        }
        return ret;
    }

    public static List<String> gnotrep(BufferedReader inn, String keyfilter) {
        return grep_basic(inn, keyfilter);
    }

    /**
     * get lines in ParserUtils files matching given file name regexp
     *
     * @param filefilter filename filter regexp
     * (http://java.sun.com/j2se/1.4.2/docs/api/java/util/regex/Pattern.html)
     * @param numbers line numbers to get
     * @return list of lines taken from selected files
     */
    public List<String> lines(String filefilter, int... numbers) {
        LinkedList<String> p = new LinkedList<String>();
        List<File> fs = find(filefilter);
        if (fs.size() == 0) {
            return p;
        }
        for (File f : fs) {
            String[] lines = ParserUtils.getASCIIFileLines(f);
            for (int i : numbers) {
                if (i < 0) {
                    i = lines.length + i;
                }
                p.addLast(lines[i]);
            }
        }
        return p;
    }

    /**
     * get lines in ParserUtils files
     *
     * @param numbers line numbers to get
     * @return list of lines taken from selected files
     */
    public List<String> lines(int... numbers) {
        return lines(filefilter, numbers);
    }

    /**
     * get lines in ParserUtils files matching given file name regexp
     *
     * @param filefilter filename filter regexp
     * (http://java.sun.com/j2se/1.4.2/docs/api/java/util/regex/Pattern.html)
     * @return list of lines taken from selected files
     */
    public List<String> lines(String filefilter) {
        LinkedList<String> p = new LinkedList<String>();
        List<File> fs = find(filefilter);
        if (fs.size() == 0) {
            return p;
        }
        for (File f : fs) {
            String[] lines = ParserUtils.getASCIIFileLines(f);
            for (int i = 0; i < lines.length; i++) {
                p.addLast(lines[i]);
            }
        }
        return p;
    }

    /**
     * get lines in ParserUtils files
     *
     * @return list of lines taken from selected files
     */
    public List<String> lines() {
        return lines(filefilter);
    }

    /**
     * test if String is in ParserUtils file
     *
     * @param file ParserUtils file to read
     * @param keyfilter String to find
     * @return keyfilter found in file
     */
    public static Boolean contains(File file, String keyfilter) {
        if (file == null || file.isDirectory()) {
            return false;
        }
        java.io.BufferedReader inn = null;
        InputStreamReader isr = null;
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            isr = new InputStreamReader(fis, CHARSET);
            inn = new BufferedReader(isr);
            return contains(inn, keyfilter);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fis.close();
                isr.close();
                inn.close();
            } catch (Exception ee) {
                ee.printStackTrace();
            }
        }
        return false;
    }

    /**
     * test if String is in String
     *
     * @param input buffer to read
     * @param keyfilter String to find
     * @return keyfilter found in file
     */
    public static Boolean containsIn(String input, String keyfilter) {
        if (input == null) {
            return null;
        }
        java.io.BufferedReader inn = null;
        InputStreamReader isr = null;
        InputStream fis = null;
        Boolean ret = false;
        try {
            fis = new ByteArrayInputStream(input.getBytes(CHARSET));
            isr = new InputStreamReader(fis, CHARSET);
            inn = new BufferedReader(isr);
            ret = contains(inn, keyfilter);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fis.close();
                isr.close();
                inn.close();
            } catch (Exception ee) {
                ee.printStackTrace();
            }
        }
        return ret;
    }

    /**
     * test if String is in buffer
     *
     * @param inn buffer to read
     * @param keyfilter String to find
     * @return keyfilter found in file
     */
    public static Boolean contains(BufferedReader inn, String keyfilter) {
        LinkedList<String> lines = new LinkedList<String>();
        String tmp;
        Pattern p = Pattern.compile(ANY + keyfilter + ANY);
        try {
            while ((tmp = inn.readLine()) != null) {
                if (p.matcher(tmp).matches()) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * test if String is in any file
     *
     * @param filefilter ParserUtils files to read
     * @param keyfilter String to find
     * @return keyfilter found in any file matching filefilter name
     */
    public Boolean contains(String filefilter, String keyfilter) {
        List<File> fs = find(filefilter);
        if (fs.size() == 0) {
            return false;
        }
        for (File f : fs) {
            if (contains(f, keyfilter)) {
                return true;
            }
        }
        return false;
    }

    /**
     * test if String is in any file
     *
     * @param keyfilter String to find
     * @return keyfilter found in any file
     */
    public Boolean contains(String keyfilter) {
        return contains(filefilter, keyfilter);
    }

    /**
     * get lines matching given content regexp
     *
     * @param lines list of String to clean
     * @param regexp regular expression to match
     * (http://java.sun.com/j2se/1.4.2/docs/api/java/util/regex/Pattern.html)
     * @return list of lines taken from selected files
     */
    public static List<String> filter(List<String> lines, String regexp) {
        LinkedList<String> p = new LinkedList<String>();
        for (String line : lines) {
            if (line.matches(regexp)) {
                p.add(line);
            }
        }
        return p;
    }

    /**
     * get lines matching given content regexp
     *
     * @param lines list of String to clean
     * @param regexp regular expression to match
     * (http://java.sun.com/j2se/1.4.2/docs/api/java/util/regex/Pattern.html)
     * @return list of lines taken from selected files
     */
    public static int[] filterIndexes(List<String> lines, String regexp) {
        LinkedList<Integer> p = new LinkedList<Integer>();
        int i = 0;
        for (String line : lines) {
            if (line.matches(regexp)) {
                p.addLast(i);
            }
            i++;
        }
        int[] pa = new int[p.size()];
        for (int j = 0; j < pa.length; j++) {
            pa[j] = p.get(j);
        }
        return pa;
    }

    /**
     * concatenante many strings as lines
     *
     * @param lines String array to concatenate
     * @return cat lines (separated by line return)
     */
    public static String strcat(String... lines) {
        LinkedList<String> l = new LinkedList<String>();
        l.addAll(Arrays.asList(lines));
        return cat(l, "\n");
    }

    /**
     * concatenante many strings as lines
     *
     * @param lines String array to concatenate
     * @return cat lines (separated by line return)
     */
    public static String strcat(LinkedList lines) {
        return cat(lines, "\n");
    }

    /**
     * concatenante many strings
     *
     * @param lines String array to concatenate
     * @param separator String
     * @return cat lines
     */
    public static String cat(List lines, String separator) {
        StringBuilder sb = new StringBuilder();
        for (Object l : lines) {
            sb.append(l.toString()).append(separator);
        }
        return sb.toString();
    }

    /**
     * recursive merge of all lists and sub-lists in one list
     *
     * @param lines List<List<List<...<...<String>>>
     * @return LinkedList<String> list merging all elements of arguments
     */
    public static List<String> merge(List lines) {
        LinkedList<String> list = new LinkedList<String>();
        for (Object l : lines) {
            if (l instanceof List) {
                list.addAll(merge((List) l));
            } else {
                list.add(l.toString());
            }
        }
        return list;
    }

    /**
     * remove whitespaces, ...
     *
     * @param line String to clean
     * @return line without whitespace at end or begining
     */
    public static int length(String line) {
        if (line == null) {
            return 0;
        }
        return line.length();
    }
    
        /**
     * remove whitespaces, ...
     *
     * @param lines list of String to clean
     * @return list of lines without whitespace at end or begining
     */
    public static List<Integer> length(List<String> lines) {
        LinkedList<Integer> p = new LinkedList<Integer>();
        for (String line : lines) {
            p.add(length(line));
        }
        return p;
    }
    
    /**
     * remove whitespaces, ...
     *
     * @param line String to clean
     * @return line without whitespace at end or begining
     */
    public static String trim(String line) {
        if (line == null) {
            return null;
        }
        return line.trim();
    }

    /**
     * remove whitespaces, ...
     *
     * @param lines list of String to clean
     * @return list of lines without whitespace at end or begining
     */
    public static List<String> trim(List<String> lines) {
        LinkedList<String> p = new LinkedList<String>();
        for (String line : lines) {
            p.add(trim(line));
        }
        return p;
    }

    /**
     * remove (any) quote, ...
     *
     * @param line String to clean
     * @return line without quotes at end or begining
     */
    public static String unquote(String line) {
        if (line == null) {
            return null;
        }
        line=line.trim();
        if (line.charAt(0)=='"' && line.charAt(line.length()-1)=='"') return line.substring(1,line.length()-1);
        if (line.charAt(0)=='\'' && line.charAt(line.length()-1)=='\'') return line.substring(1,line.length()-1);
        return line;
    }

    /**
     * remove (any) quote, ...
     *
     * @param lines list of String to clean
     * @return list of lines without quotes at end or begining
     */
    public static List<String> unquote(List<String> lines) {
        LinkedList<String> p = new LinkedList<String>();
        for (String line : lines) {
            p.add(unquote(line));
        }
        return p;
    }

    /**
     * split a line in several parts
     *
     * @param line String to split
     * @param separator separating string (removed from results)
     * @return list of String
     */
    public static List<String> split(String line, String separator) {
        String[] ss = line.split(separator);
        LinkedList<String> p = new LinkedList<String>();
        p.addAll(Arrays.asList(ss));
        return p;
    }

    /**
     * split a line in several parts and keep one of them
     *
     * @param line String to split
     * @param separator separating string (removed from results)
     * @index index of part to keep (0 means last, 1 means first)
     * @return String part of line to keep
     */
    public static String cut(String line, String separator, int index) {
        return line.split(separator)[index - 1];
    }

    /**
     * split several lines in several parts and keep one of them
     *
     * @param lines list of String to split
     * @param separator separating string (removed from results)
     * @index index of part to keep (0 means last, 1 means first)
     * @return list of String part of lines to keep
     */
    public static List<String> cut(List<String> lines, String separator, int index) {
        LinkedList<String> p = new LinkedList<String>();
        for (String line : lines) {
            p.add(cut(line, separator, index));
        }
        return p;
    }

    /**
     * get part of a String
     *
     * @param line String to cut
     * @param begin first char position to keep (-1 means first char)
     * @param end last char position to keep (-1 means last char)
     * @return line cut
     */
    public static String substring(String line, int begin, int end) {
        if (begin > line.length()-1) {
            begin = line.length()-1;
        }
        if (begin < 0) {
            begin = 0;
        }
        if (end < 0) {
            end = 0;
        }
        if (end > line.length()) {
            end = line.length();
        }
        if (begin > end) {
            begin = end;
        }
        return line.substring(begin, end);
    }

    /**
     * get part of a String
     *
     * @param line String to cut
     * @param beginstr first chars to keep
     * @param endstr first chars to reject
     * @return line cut
     */
    public static String substring(String line, String beginstr, String endstr) {
        int begin = line.indexOf(beginstr);
        if (begin == -1) {
            begin = 0;
        }
        int end = line.indexOf(endstr);
        if (end == -1) {
            end = line.length() - 1;
        }
        return substring(line, begin, end);
    }

    /**
     * get part of a many String
     *
     * @param lines Strings to cut
     * @param begin first char position to keep (-1 means first char)
     * @param end last char position to keep (-1 means last char)
     * @return lines cut
     */
    public static List<String> substring(List<String> lines, int begin, int end) {
        LinkedList<String> p = new LinkedList<String>();
        for (String line : lines) {
            p.add(substring(line, begin, end));
        }
        return p;
    }

    /**
     * get part of a several Strings
     *
     * @param lines String to cut
     * @param beginstr first chars to keep
     * @param endstr first chars to reject
     * @return lines cut
     */
    public static List<String> substring(List<String> lines, String beginstr, String endstr) {
        LinkedList<String> p = new LinkedList<String>();
        for (String line : lines) {
            p.add(substring(line, beginstr, endstr));
        }
        return p;
    }

    /**
     * get part of a String
     *
     * @param line String to cut
     * @param begin first char position to keep (-1 means first char)
     * @return line cut
     */
    public static String substring(String line, int begin) {
        return line.substring(begin);
    }

    /**
     * get part of a String
     *
     * @param line String to cut
     * @param beginstr first chars to keep
     * @return line cut
     */
    public static String substring(String line, String beginstr) {
        int begin = line.indexOf(beginstr);
        if (begin == -1) {
            begin = 0;
        }
        return substring(line, begin);
    }

    /**
     * get part of a many String
     *
     * @param lines Strings to cut
     * @param begin first char position to keep (-1 means first char)
     * @return lines cut
     */
    public static List<String> substring(List<String> lines, int begin) {
        LinkedList<String> p = new LinkedList<String>();
        for (String line : lines) {
            p.add(substring(line, begin));
        }
        return p;
    }

    /**
     * get part of a several Strings
     *
     * @param lines String to cut
     * @param beginstr first chars to keep
     * @return lines cut
     */
    public static List<String> substring(List<String> lines, String beginstr) {
        LinkedList<String> p = new LinkedList<String>();
        for (String line : lines) {
            p.add(substring(line, beginstr));
        }
        return p;
    }

    /**
     * replace parts of a String
     *
     * @param line String to change in
     * @param toreplace part of string to replace
     * @param replacer replacement
     * @return line with replacement
     */
    public static String replace(String line, String toreplace, String replacer) {
        return line.replace(toreplace, replacer);
    }

    /**
     * replace parts of a String
     *
     * @param lines String to change in
     * @param toreplace part of string to replace
     * @param replacer replacement
     * @return lines with replacement
     */
    public static List<String> replace(List<String> lines, String toreplace, String replacer) {
        if (lines == null) {
            return null;
        }
        LinkedList<String> p = new LinkedList<String>();
        for (String line : lines) {
            p.add(replace(line, toreplace, replacer));
        }
        return p;
    }

    /**
     * replace parts of a String
     *
     * @param line String to change in
     * @param toreplace part of string to replace
     * @param replacer replacement
     * @return line with replacement
     */
    public static String replace_regexp(String line, String toreplace, String replacer) {
        return line.replaceAll(toreplace, replacer);
    }

    /**
     * replace parts of a String
     *
     * @param lines String to change in
     * @param toreplace part of string to replace
     * @param replacer replacement
     * @return lines with replacement
     */
    public static List<String> replace_regexp(List<String> lines, String toreplace, String replacer) {
        if (lines == null) {
            return null;
        }
        LinkedList<String> p = new LinkedList<String>();
        for (String line : lines) {
            p.add(replace_regexp(line, toreplace, replacer));
        }
        return p;
    }

    /**
     * get the part following of a String
     *
     * @param line String to cut
     * @param beginstr last chars to reject
     * @return line part following beginstr
     */
    public static String after(String line, String beginstr) {
        int begin = line.indexOf(beginstr);
        if (begin == -1) {
            begin = 0;
        } else {
            begin += beginstr.length();
        }
        return substring(line, begin);
    }

    /**
     * get following part of several Strings
     *
     * @param lines String to cut
     * @param beginstr last chars to reject
     * @return lines parts following beginstr
     */
    public static List<String> after(List<String> lines, String beginstr) {
        LinkedList<String> p = new LinkedList<String>();
        for (String line : lines) {
            p.add(after(line, beginstr));
        }
        return p;
    }

    /**
     * get the part following of a String
     *
     * @param line String to cut
     * @param beginstr last chars to reject
     * @return line part following beginstr
     */
    public static String afterLast(String line, String beginstr) {
        int begin = line.lastIndexOf(beginstr);
        if (begin == -1) {
            begin = 0;
        } else {
            begin += beginstr.length();
        }
        return substring(line, begin);
    }

    /**
     * get following part of several Strings
     *
     * @param lines String to cut
     * @param beginstr last chars to reject
     * @return lines parts following beginstr
     */
    public static List<String> afterLast(List<String> lines, String beginstr) {
        LinkedList<String> p = new LinkedList<String>();
        for (String line : lines) {
            p.add(afterLast(line, beginstr));
        }
        return p;
    }

    /**
     * get the part before of a String
     *
     * @param line String to cut
     * @param endstr first chars to reject
     * @return line part before endstr
     */
    public static String before(String line, String endstr) {
        int end = line.indexOf(endstr);
        if (end == -1) {
            end = line.length();
        }
        return substring(line, 0, end);
    }

    /**
     * get part before of several Strings
     *
     * @param lines String to cut
     * @param beginstr first chars to reject
     * @return lines parts before beginstr
     */
    public static List<String> before(List<String> lines, String beginstr) {
        LinkedList<String> p = new LinkedList<String>();
        for (String line : lines) {
            p.add(before(line, beginstr));
        }
        return p;
    }

    /**
     * get the part between delimiters of a String
     *
     * @param line String to cut
     * @param beginstr last chars to reject
     * @param endstr first chars to reject
     * @return line part between beginstr and endstr
     */
    public static String between(String line, String beginstr, String endstr) {
        //println(line+" <"+beginstr+"> <"+endstr+">")
        int begin = line.indexOf(beginstr);
        if (begin == -1) {
            if (line.indexOf(endstr) == -1) {
                return null;
            }
            begin = 0;
        } else {
            begin += beginstr.length();
        }
        //println(" "+begin)
        int end = line.indexOf(endstr, begin);
        if (end == -1) {
            end = line.length() - 1;
        }
        //println(" "+end)
        return substring(line, begin, end);
    }

    /**
     * get the part between delimiters of several Strings
     *
     * @param lines Strings to cut
     * @param beginstr last chars to reject
     * @param endstr first chars to reject
     * @return lines parts between beginstr and endstr
     */
    public static List<String> between(List<String> lines, String beginstr, String endstr) {
        LinkedList<String> p = new LinkedList<String>();
        for (String line : lines) {
            p.add(between(line, beginstr, endstr));
        }
        return p;
    }

    public static List<String> get(List<String> lines, int... numbers) {
        LinkedList<String> p = new LinkedList<String>();
        for (int i : numbers) {
            if (i <= 0) {
                i = lines.size() + i;
            }
            p.add(lines.get(i - 1));
        }
        return p;
    }

    public static List<String> head(List<String> lines, int l) {
        l = Math.min(l,lines.size());
        List<String> h = new ArrayList<>(l);
        for (int i = 0; i < l; i++) {
            h.add(lines.get(i));
        }
        return h;
    }

    public static List<String> tail(List<String> lines, int l) {
        l = Math.min(l,lines.size());
        List<String> t = new ArrayList<>(l);
        for (int i = lines.size()-l; i < lines.size(); i++) {
            t.add(lines.get(i));
        }
        return t;
    }

    public static List<String> skip(List<String> lines, int skip) {
        for (int i = 0; i < skip; i++) {
            lines.remove(0);
        }
        return lines;
    }

    public static List<String> getAfter(List<String> lines, int after, int... numbers) {
        LinkedList<String> p = new LinkedList<String>();
        for (int i : numbers) {
            if (i <= 0) {
                i = lines.size() + i;
            }
            p.add(lines.get(i - 1 + after));
        }
        return p;
    }

    public static List<String> getBefore(List<String> lines, int before, int... numbers) {
        LinkedList<String> p = new LinkedList<String>();
        for (int i : numbers) {
            if (i <= 0) {
                i = lines.size() + i;
            }
            p.add(lines.get(i - 1 - before));
        }
        return p;
    }

    /**
     * get element of a list of Strings
     *
     * @param lines list of String
     * @param i index of element to get (0 means last, 1 means first)
     * @return String element to get
     */
    public static String get(List<String> lines, int i) {
        if (lines == null || lines.size() == 0) {
            return null;
        }
        if (i <= 0) {
            i = lines.size() + i;
        }
        return lines.get(i - 1);
    }

    /**
     * get element of a list of Strings
     *
     * @param lines list of String
     * @param start index of element to start from (0 means last, 1 means first)
     * @param step increment between two elements
     * @return String element to get
     */
    public static List<String> getBy(List<String> lines, int start, int step) {
        if (lines == null || lines.size() == 0) {
            return null;
        }
        if (start <= 0) {
            start = lines.size() + start;
        }
        LinkedList<String> p = new LinkedList<String>();
        int i = start;
        while (i >= 1 && i <= lines.size()) {
            p.add(lines.get(i - 1));
            i = i + step;
        }
        return p;
    }

    /**
     * get element of a list of Strings
     *
     * @param lines list of String
     * @param start index of element to start from (0 means last, 1 means first)
     * @param step increment between two elements
     * @param end index of element to end
     * @return String element to get
     */
    public static List<String> getBy(List<String> lines, int start, int step, int end) {
        if (lines == null || lines.size() == 0) {
            return null;
        }
        if (start <= 0) {
            start = lines.size() + start;
        }
        LinkedList<String> p = new LinkedList<String>();
        int i = start;
        while (i >= 1 && i <= lines.size() && i <= end) {
            p.add(lines.get(i - 1));
            i = i + step;
        }
        return p;
    }

    /**
     * get element of a list of String lists
     *
     * @param lines list of String lists
     * @param i index of element to get (0 means last, 1 means first)
     * @return String elements to get
     */
    public static List<String> getAll(List<List<String>> lines, int i) {
        LinkedList<String> p = new LinkedList<String>();
        for (List<String> line : lines) {
            p.add(get(line, i));
        }
        return p;
    }

    public static double times(double d, double t) {
        return d * t;
    }

    public static double[] times(double[] d, double t) {
        double[] dt = new double[d.length];
        for (int i = 0; i < dt.length; i++) {
            dt[i] = d[i] * t;
        }
        return dt;
    }

    public static double[][] times(double[][] d, double t) {
        double[][] dt = new double[d.length][];
        for (int i = 0; i < dt.length; i++) {
            dt[i] = times(d[i], t);
        }
        return dt;
    }

    public static double asNumeric(boolean boolValue) {
        return boolValue ? 1.0 : 0.0;
    }

    public static double asNumeric(Boolean boolValue) {
        return boolValue ? 1.0 : 0.0;
    }

    /**
     * wrap String in float
     *
     * @param line String
     * @return floating number read, NaN if impossible to parse
     */
    public static double asNumeric(String line) {
        double value = Double.NaN;
        if (line != null) {
            try {
                // Attempt to parse as a double first
                value = Double.parseDouble(line);
            } catch (NumberFormatException nfe) {
                // If parsing fails, check if the string is a boolean representation
                if ("true".equalsIgnoreCase(line)) {
                    value = 1.0;
                } else if ("false".equalsIgnoreCase(line)) {
                    value = 0.0;
                }
            }
        }
        return value;
    }

    public static double asNumeric(List line) {
        if (line.get(0) instanceof String) {
            return asNumeric((String) line.get(0));
        } else {
            return asNumeric((LinkedList) line.get(0));
        }
    }

    /**
     * wrap String in float array
     *
     * @param line String
     * @param delim String to separate values
     * @return float array
     */
    public static double[] asNumeric1DArray(String line, String delim) {
        return ArrayString.readString1DDouble(line, delim);
    }

    public static double[] asNumeric1DArray(String line) {
        return ArrayString.readString1DDouble(line);
    }

    public static double[] asNumeric1DArray(List<String> lines) {
        double[] values = new double[lines.size()];
        int i = 0;
        for (String line : lines) {
            values[i++] = asNumeric(line);
        }
        return values;
    }

    /**
     * wrap String in 2D float array
     *
     * @param line String
     * @param coldelim String to separate columns
     * @param rowdelim String to separate rows
     * @return 2D float array
     */
    public static double[][] asNumeric2DArray(String line, String coldelim, String rowdelim) {
        return ArrayString.readStringDouble(line, coldelim, rowdelim);
    }

    public static double[][] asNumeric2DArray(String line) {
        return ArrayString.readStringDouble(line);
    }

    public static double[][] asNumeric2DArray(List<String> lines, String coldelim) {
        double[][] values = new double[lines.size()][];
        int i = 0;
        for (String line : lines) {
            values[i++] = asNumeric1DArray(line, coldelim);
        }
        return values;
    }

    public static double[][] asNumeric2DArray(List<String> lines) {
        double[][] values = new double[lines.size()][];
        int i = 0;
        for (String line : lines) {
            values[i++] = asNumeric1DArray(line);
        }
        return values;
    }
    /*public Matrix asVector(String line, String coldelim)  {
     return new Matrix(asDouble1DArray( line,  coldelim))
     }
     public Matrix asVector(String line)  {
     return new Matrix(asDouble1DArray( line))
     }
     public Matrix asVector(LinkedList<String> lines)  {
     return new Matrix(asDouble1DArray( lines))
     }
     public Matrix asMatrix(String line, String coldelim, String rowdelim)  {
     return new Matrix(asDouble2DArray( line,  coldelim,  rowdelim))
     }
     public Matrix asMatrix(String line)  {
     return new Matrix(asDouble2DArray( line))
     }
     public Matrix asMatrix(LinkedList<String> lines, String coldelim)  {
     return new Matrix(asDouble2DArray( lines, coldelim))
     }
     public Matrix asMatrix(LinkedList<String> lines)  {
     return new Matrix(asDouble2DArray( lines))
     }*/

    public static String asString(HashMap o) {
        return Data.asString(o);
    }

    public static String asString(Object o) {
        return Data.asString(o);
    }
    
    public static String toString(List o) {
        return o.toString();
    }
    
    public static String toString(Object o) {
        return o.toString();
    }

    public static String concatString(Object o1, Object o2) {
        return o1.toString()+o2.toString();
    }

    public static String concatString(String string1, String string2) {
        return string1+string2;
    }

    public static int doubleToInt(double d) {
        return (int)d;
    }
}
