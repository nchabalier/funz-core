package org.funz;

import java.util.LinkedList;
import java.util.List;
import org.junit.Test;
import static org.funz.util.Format.XML.merge;

/**
 *
 * @author richet
 */
public class XMLTest {

    public static void main(String args[]) {
        org.junit.runner.JUnitCore.main(XMLTest.class.getName());
    }

    public XMLTest() {
    }

    @Test
    public void testMerge() {
        List<String> s = new LinkedList<String>();
        s.add("<toto name='titi'>(a,b,c)</toto>");
        s.add("<toto name='titi'>(d,e,f)</toto>");
        String merged = merge(";", s);
        assert merged.equals("<toto name=\"titi\">(a,b,c);(d,e,f)</toto>") : "bad merge operation: " + merged;
    }
}