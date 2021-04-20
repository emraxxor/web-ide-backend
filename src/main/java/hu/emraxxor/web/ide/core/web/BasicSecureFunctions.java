package hu.emraxxor.web.ide.core.web;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 *
 * @author attila
 */
public class BasicSecureFunctions {

    /**
     * Check if the given string contains a pattern which could be lead to a
     * possible attack . This function only checks the existence of the given pattern.
     *
     * @param arg
     *
     * @url https://www.owasp.org/index.php/Path_Traversal
     *
     * @return
     */
    public static boolean directoryTraversalInputCheck(String arg) {
        return !(arg.contains("%2e") || arg.contains("%5c") || arg.contains("%25")
                || arg.contains("%c0") || arg.contains("%c1") || arg.contains("."));
    }
    
    private static boolean startsWith(String arg) {
    	 return (arg.startsWith("%2e") || arg.startsWith("%5c") || arg.startsWith("%25")
                 || arg.startsWith("%c0") || arg.startsWith("%c1") || arg.startsWith("."));
    }
    
    public static boolean directoryTraversalInputCheckStartsWith(String arg) {
    	return Arrays.asList(arg.split("/")).stream().anyMatch(e -> startsWith(e))
    			||
    			Arrays.asList(arg.split("\\\\")).stream().anyMatch(e -> startsWith(e)) ;
    }

    public static String decode(String value) throws UnsupportedEncodingException {
        return URLDecoder.decode(value, StandardCharsets.UTF_8.toString());
    }

}
