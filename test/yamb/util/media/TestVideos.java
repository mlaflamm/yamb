package yamb.util.media;

import junit.framework.TestCase;

import java.io.File;

/**
 * @author manuel.laflamme
 * @since 17-Aug-2008
 */
public class TestVideos extends TestCase
{
    public void testIsVideoFile() throws Exception
    {
        // todo : more 
        assertEquals(true, Videos.isVideoFile(new File("file.ext.avi")));
    }
}
