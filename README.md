Yet Another Multimedia Browser (YAMB) is a Java app I created in early 2008 to browse and organize video files. 

YAMB only works on windows. It uses some undocumented windows file system classes from the JDK. It uses JNA to access Windows Shell32 functionalities. It also uses DSJ, a DirectShow Java wrapper to generate thumbnails.

I've put this app together quickly and I cannibalized the code from a few other personal projects in the process.
It unfortunately lacks Javadoc and, even worse, unit tests! :-( I did this project for myself and I never intended to release it publicly until now. A lot of work is still needed to make it usable by external peoples.

## User Interface
YAMB is built around a tabbed MDI interface which can contain 4 types of view: Explorer view, Tab view, Series view and Stat view.

### Explorer View
Browse your file system like windows explorer. 
  
You can flag and unflag a directory as a video library. YAMB will scan the directory and all subdirectories for tagged video files. A tagged file contains an underscore followed by one or multiple tags separated by commas like this:   ```Series Name - Vol #, Ep # - Title_Tag1, Tag2.ext```. At this time this is not very flexible but it works.

[![Explorer view](http://github.com/mlaflamm/yamb/wiki/explorerview.jpg)](http://github.com/mlaflamm/yamb/wiki/explorerview.jpg)

### Tag View
Browse video files by tags. 

[![Tag view](http://github.com/mlaflamm/yamb/wiki/tagview.jpg)](http://github.com/mlaflamm/yamb/wiki/tagview.jpg)

### Series View
Browse video files by series.

[![Series view](http://github.com/mlaflamm/yamb/wiki/seriesview.jpg)](http://github.com/mlaflamm/yamb/wiki/seriesview.jpg)

### Stats View
Display various stats and graphs about tagged files and libraries. You can double click on the graph components to open a corresponding Tag or Series view.

[![Stats view](http://github.com/mlaflamm/yamb/wiki/statsview.jpg)](http://github.com/mlaflamm/yamb/wiki/statsview.jpg)

## Building and running YAMB

I modify and run YAMB from my favorite Java IDE, IntelliJ IDEA. So YAMB has no build file, no startup script and no real configuration file yet. 

**If you are adventurous enough to build YAMB as is, here are the required dependencies:**

* [Dsj 0.8.47 - DirectShow Java wrapper](http://www.humatic.de/htools/dsj.htm). Used to generate video thumbnails. Note that YAMB is not compatible yet with newer versions of Dsj.
* [Fobs4jmf 0.4.2 - JMF plugin for ffmpeg](http://fobs.sourceforge.net/). Alternative way to generate video thumbnails. 
* [JNA - Java Native Access](http://jna.java.net/). Used to rename, copy, move and delete files via Shell32.
* [Apache commons (collections, io, lang, primitives)](http://commons.apache.org/)
* [JFreeChart 1.0.10](http://www.jfree.org/jfreechart/). Used in the Stat View.
* [Cismet Gui Common](http://blogs.cismet.de/gadgets/jpopupmenubutton/)
* [Riverlayout 1.1](http://www.datadosen.se/riverlayout/)
* [Swingx 1.6 - SwingLabs Swing Component Extension](http://java.net/projects/swingx/). Used in experimental grouping by year file list.
  
**YAMB has these additional dependencies at runtime.**

* [FFmpeg executable](http://www.ffmpeg.org/). Yet another alternative way to generate video thumbnails. 
* [Mediainfo executable](http://mediainfo.sourceforge.net/en). Used to fetch video details like length, resolution, bitrate, codec. 

**I use this runtime configuration to run YAMB from my IDE:**

*Main Class*
<pre>
yamb.Main
</pre>
  
*VM Parameters*
<pre>
"-Ddsj.path=../_3rdparties/dsj-b0.8.47" "-Dffmpeg.path=../_3rdparties/ffmpeg-11870" "-Dmediainfo.path=../_3rdparties/mediainfo-0.7.7.6" "-Dyamb.datadir=../_data"
</pre>

## Video Thumbnail Generation

Generating video thumbnails from Java is not easy as it involves native code. YAMB can generate video thumbnails using few libraries and tools. All of them have pros and cons.

* Dsj is fast and relatively stable but sometimes hangs when processing some videos. YAMB use Dsj in a separate process to circumvent possible Dsj issues.
* Ffmpeg external executable is stable but very slow. YAMB use it as a fallback when Dsj fail.
* Fobs4jmf is very fast but also very unstable, it is currently disabled.
* [QuickTime for Java](http://en.wikipedia.org/wiki/QuickTime_for_Java) is not supported by Apple anymore. QTJ support has been removed just before uploading YAMB on Github.
* [Xuggle](http://www.xuggle.com/xuggler/) is not yet integrated in YAMB but look promising.
