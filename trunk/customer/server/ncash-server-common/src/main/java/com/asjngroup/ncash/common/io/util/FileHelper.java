package com.asjngroup.ncash.common.io.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import com.asjngroup.ncash.common.util.StringHelper;
import com.asjngroup.ncash.common.util.StringUtil;

public class FileHelper
{
    private static final String LINUX_SEPERATOR = "/";
	private static final String WINDOWS_SEPERATOR = "\\";
	private static final String XML_EXTENSION = ".xml";
	// store the char set the server uses
	public static boolean isUnicodeFileEncoding= false;
	public static final Charset platformCharset = Charset.defaultCharset();
    public static final Charset asciiCharset = Charset.forName( "ISO-8859-1" );
    public static Charset unicodeCharset;
    public static boolean isUnicode = false;
    public static final String fileSeperator = System.getProperty( "file.separator" );
    public static final String pathSeperator = System.getProperty( "path.separator" );
   

    public static String sparkTempPath;
    public static File sparkTempPathFile;

    static
    {
        if ( ByteOrder.nativeOrder().equals( ByteOrder.LITTLE_ENDIAN ) )
        {
            unicodeCharset = Charset.forName("UTF-16LE");
        }
        else
        {
            unicodeCharset = Charset.forName("UTF-16BE");
        }
        // use the spark temp path if set
        sparkTempPath = System.getenv( "SPARK_TEMP" );

        // otherwise use the temp, tmp path
        if ( sparkTempPath == null ) sparkTempPath = System.getenv( "TEMP" );
        if ( sparkTempPath == null ) sparkTempPath = System.getenv( "TMP" );

        // we still might not have one but we can carry on anyway
        if ( sparkTempPath != null ) sparkTempPathFile = new File(sparkTempPath);
    }

    public static File createTempFile( String prefix, String suffix, File directory ) throws IOException
    {
        return File.createTempFile( prefix, suffix, directory );
    }

    public static File createTempFile(String prefix, String suffix) throws IOException
    {
        return File.createTempFile(prefix, suffix, sparkTempPathFile);
    }

    public static String createTempFilename(String prefix, String suffix) throws IOException
    {
        return File.createTempFile(prefix, suffix, sparkTempPathFile).getAbsolutePath();
    }

    public static void filesCopy( File from, File to, String filterString ) throws IOException
    {
        final String filterStringF = filterString;
        FilenameFilter filter = new FilenameFilter()
        {
            public boolean accept( File dir, String name )
            {
                return name.matches( filterStringF );
            }
        };

        File[] files = from.listFiles( filter );

        for ( File file : files )
        {
            fileCopy( file, to );
        }
    }

    private static final int COPY_BLOCK_SIZE = 10000000;

    public static void fileCopy( File from, File to ) throws IOException
    {
        // if to is a directory then make the file copy to the same name
        if ( to.isDirectory() )
        {
            to = new File( to, from.getName() );
        }

        // Create channels for the copy
        FileChannel sourceChannel = new FileInputStream(from.getAbsolutePath()).getChannel();

        try
        {
            FileChannel destChannel = new FileOutputStream(to.getAbsolutePath()).getChannel();

            try
            {
                // Copy file contents from source to destination
                long size = sourceChannel.size();
                long currentOffset = 0;

                while ( size > 0 )
                {
                    long copied = destChannel.transferFrom(sourceChannel, currentOffset, Math.min( COPY_BLOCK_SIZE, size ) );
                    currentOffset += copied;
                    size -= copied;
                }
            }
            finally
            {
                destChannel.close();
            }
        }
        finally
        {
            sourceChannel.close();
        }
    }

    public static String extractFileName(String pathName)
    {
    	String seperator=WINDOWS_SEPERATOR;
    	if(!StringHelper.isEmpty(pathName))
    	{
    		if(pathName.contains(LINUX_SEPERATOR))
    			seperator=LINUX_SEPERATOR;
    		
    		StringBuffer fileNameBuffer = new StringBuffer( pathName.substring( pathName.lastIndexOf( seperator ) + 1 ) );
    		
    		if ( pathName.endsWith( XML_EXTENSION ) )
    			return fileNameBuffer.toString();

    		return fileNameBuffer.append( XML_EXTENSION ).toString();
    	}
    	 return null;   	
    }
    
    public static void fileMove( File from, File to ) throws IOException
    {
        if ( !from.exists() )
        {
            throw new IOException( StringUtil.create( "Source file '%1' does not exist", from.getAbsolutePath() ) );
        }

        if ( to.exists() )
        {
            throw new IOException( StringUtil.create( "Destination file '%1' already exists", to.getAbsolutePath() ) );
        }

        if ( !from.isFile() )
        {
            throw new IOException( StringUtil.create( "Source file '%1' exists but is not a file", to.getAbsolutePath() ) );
        }

        if ( to.isDirectory() )
        {
            to = new File( to, from.getName() );
        }

        if ( !from.canRead() )
        {
            throw new IOException( StringUtil.create( "Source file '%1' is not readable", to.getAbsolutePath() ) );
        }

        if ( !from.canWrite() )
        {
            throw new IOException( StringUtil.create( "Source file '%1' is not writable", to.getAbsolutePath() ) );
        }

        if ( from.renameTo( to ) )
        {
            // done
            return;
        }

        // rename didn't work do just do a copy-delete
        fileCopy( from, to );

        if ( !from.delete() )
        {
            to.delete();
            
            throw new IOException( StringUtil.create( "Failed to delete source file '%1' after copy", from.getAbsolutePath() ) );
        }
    }

    public static ByteBuffer[] splitFileToBuffers( File file, int splitCount, int align ) throws IOException
    {
        ByteBuffer[] buffers = new ByteBuffer[ splitCount ];

        long fileLength = file.length();

        // divvy up the file into blocks
        long totalFrames = fileLength / align;

        long framesPerBuffer = totalFrames / splitCount;
        if ( ( totalFrames % splitCount ) != 0 )
        {
            // if there is a remainder increment the frame count to account for this
            framesPerBuffer++;
        }

        long bytesPerSplit = framesPerBuffer * align;

        // open the file
        FileInputStream fis = new FileInputStream( file );
        FileChannel fc = fis.getChannel();

        try
        {
            long currentOffset = 0;

            // copy all but the last buffer
            for ( int i = 0; i < buffers.length - 1; i++ )
            {
                // map a block of the file into memory
                buffers[ i ] = fc.map( FileChannel.MapMode.READ_ONLY, currentOffset, bytesPerSplit );

                currentOffset += bytesPerSplit;

                // reached the end of the file
                if ( currentOffset >= fileLength )
                {
                    throw new IllegalStateException( "File split did not finish on last buffer!" );
                }
            }

            // copy the last block as this may be smaller than the other
            buffers[ buffers.length - 1 ] = fc.map( FileChannel.MapMode.READ_ONLY, currentOffset, fileLength - currentOffset );
        }
        finally
        {
            // always close the file
            fc.close();
            fis.close();
        }

        return buffers;
    }

    public static void copyDirectoryToDirectory( File copyDirectory, File destinationDirectory ) throws IOException
    {
        copyDirectoryToDirectory( copyDirectory, destinationDirectory, new ArrayList< String >() );
    }

    // performs a "windows" copy of a directory, as if you had dragged copyDirectory INTO destination directory
    public static void copyDirectoryToDirectory( File copyDirectory, File destinationDirectory, List< String > excludeList ) throws IOException
    {
        copyDirectoryToDirectory( copyDirectory, destinationDirectory, null, excludeList );
    }

    // performs a "windows" copy of a directory, as if you had dragged copyDirectory INTO destination directory
    public static void copyDirectoryToDirectory( File copyDirectory, File destinationDirectory, List< String > includeList, List< String > excludeList ) throws IOException
    {
        if ( !copyDirectory.exists() )
        {
            throw new IllegalArgumentException( "Copy directory does not exist: " + copyDirectory.getAbsolutePath() );
        }

        if ( !copyDirectory.isDirectory() )
        {
            throw new IllegalArgumentException( "Copy directory is not a directory: " + copyDirectory.getAbsolutePath() );
        }

        if ( !destinationDirectory.exists() )
        {
            throw new IllegalArgumentException( "Destination directory does not exist: " + destinationDirectory.getAbsolutePath() );
        }

        if ( !destinationDirectory.isDirectory() )
        {
            throw new IllegalArgumentException( "Destination directory is not a directory: " + destinationDirectory.getAbsolutePath() );
        }

        File currentDestinationDirectory = new File( destinationDirectory, copyDirectory.getName() );
        boolean mkdir = currentDestinationDirectory.mkdir();

        if ( !mkdir )
        {
            throw new IOException( "Failed to create directory " + currentDestinationDirectory.getAbsolutePath() );
        }

        File[] subFiles = copyDirectory.listFiles();

        // loop over all the files in the copy directory
        for ( File subFile : subFiles )
        {
            // check for an included file/directory
            if ( includeList != null )
            {
                boolean match = false;

                for ( String include : includeList )
                {
                    if ( subFile.getAbsolutePath().toUpperCase().contains( include.toUpperCase() ) )
                    {
                        match = true;
                        break;
                    }
                }

                if ( !match ) continue;
            }

            // check for an excluded file/directory
            boolean skip = false;
            for ( String exclude : excludeList )
            {
                if ( subFile.getAbsolutePath().toUpperCase().contains( exclude.toUpperCase() ) )
                {
                    skip = true;
                    break;
                }
            }

            if ( skip ) continue;

            if ( subFile.isDirectory() )
            {
                // if its a directory recurse
                copyDirectoryToDirectory( subFile, currentDestinationDirectory, excludeList );
            }
            else
            {
                // a file copy it to the current destination
                fileCopy( subFile, currentDestinationDirectory );
            }
        }
    }

    public static void deleteTree( File directory ) throws IOException
    {
        if ( !directory.exists() )
        {
            throw new IllegalArgumentException( "Delete directory does not exist" );
        }

        if ( !directory.isDirectory() )
        {
            throw new IllegalArgumentException( "Delete directory is not a directory" );
        }

        File[] subFiles = directory.listFiles();

        for ( File subFile : subFiles )
        {
            if ( subFile.isDirectory() )
            {
                // if its a directory recurse
                deleteTree( subFile );
            }
            else
            {
                // a file, just delete it
                if ( !subFile.delete() )
                {
                    throw new IOException( "Unable to delete file: " + subFile.getAbsolutePath() );
                }
            }
        }

        if ( !directory.delete() )
        {
            throw new IOException( "Unable to delete directory: " + directory.getAbsolutePath() );
        }
    }

    public static List< File > findFiles( File directory, String match )
    {
        final String matchF = match;

        FilenameFilter filter = new FilenameFilter()
        {
            public boolean accept( File dir, String name )
            {
                return name.matches( matchF );
            }
        };

        return findFiles( directory, filter );
    }

    public static List< File > findFiles( File directory, FilenameFilter filter )
    {
        if ( !directory.exists() )
        {
            throw new IllegalArgumentException( "Search directory does not exist" );
        }

        if ( !directory.isDirectory() )
        {
            throw new IllegalArgumentException( "Search directory is not a directory" );
        }

        List< File > matchedFiles = new ArrayList< File >();

        File[] files = directory.listFiles();

        // loop all files
        for ( File file : files )
        {
            // recurse directories
            if ( file.isDirectory() )
            {
                matchedFiles.addAll( findFiles( file, filter ) );
            }
            else
            {
                // filter filenames for matches
                if ( filter.accept( file.getParentFile(), file.getName() ) )
                {
                    matchedFiles.add( file );
                }
            }
        }

        return matchedFiles;
    }

    // gets a relative path to get from relativeTo to file
    // eg from C:\Temp to C:\Downloads\myfile would return ..\Downloads\myfile
    public static String getPathRelativeTo( File relativeTo, File file ) throws IOException
    {
        if ( !relativeTo.exists() )
        {
            throw new IllegalArgumentException( "Relative to directory does not exist" );
        }

        if ( !relativeTo.isDirectory() )
        {
            throw new IllegalArgumentException( "Relative to directory is not a directory" );
        }

        if ( !file.exists() )
        {
            throw new IllegalArgumentException( "File does not exist" );
        }

        String relativeToPath = relativeTo.getAbsolutePath();

        if ( relativeToPath.equalsIgnoreCase( file.getAbsolutePath() ) ) return ".";

        String filePath;
        if ( file.isFile() )
        {
            filePath = file.getParentFile().getCanonicalPath();
        }
        else
        {
            filePath = file.getCanonicalPath();
        }

        String[] relativePathArray = StringHelper.split( relativeToPath, fileSeperator );
        String[] filePathArray = StringHelper.split( filePath, fileSeperator );

        String relativePath = "";

        for ( int i = 0; i < Math.min( relativePathArray.length, filePathArray.length ); i++ )
        {
            // ignore parts of path that match
            if ( relativePathArray[ i ].equals( filePathArray[ i ] ) ) continue;

            // add .. to the front the the current filepath part to the end
            if ( relativePath.length() == 0 )
            {
                relativePath = ".." + fileSeperator + filePathArray[ i ];
            }
            else
            {
                relativePath = ".." + fileSeperator + relativePath + fileSeperator + filePathArray[ i ];
            }
        }

        // if the file path is longer than the relative path add the remaining file parts on the end
        if ( relativePathArray.length < filePathArray.length )
        {
            int lengthDiff = filePathArray.length - relativePathArray.length;
            for ( int i = 0; i < lengthDiff; i++ )
            {
                if ( relativePath.length() > 0 )
                {
                    relativePath += fileSeperator;
                }

                relativePath += filePathArray[ filePathArray.length - lengthDiff + i ];
            }
        }

        // if the relative path is longer than the relative path add some .. to the front
        if ( relativePathArray.length > filePathArray.length )
        {
            int lengthDiff = relativePathArray.length - filePathArray.length;
            for ( int i = 0; i < lengthDiff; i++ )
            {
                relativePath = ".." + fileSeperator + relativePath;
            }
        }

        // for a file add the filename back on the end
        if ( file.isFile() )
        {
            relativePath = relativePath + fileSeperator + file.getName();
        }

        return relativePath;
    }
    public static String getFileBase( File file )
    {
        return getFileBase( file.getAbsolutePath() );
    }

    public static String getFileOnly( File file )
    {
        return getFileOnly( file.getAbsolutePath() );
    }

    public static String getFileOnly( String filename )
    {
        int endOfPathIndex = filename.lastIndexOf( fileSeperator );

        String filenameOnly;

        if ( endOfPathIndex == -1 )
        {
            filenameOnly = filename;
        }
        else
        {
            filenameOnly = filename.substring( endOfPathIndex + 1 );
        }

        return filenameOnly;
    }

    // gets a filename only from a path
    // c:\temp\hello.txt -> hello
    public static String getFileBase( String filename )
    {
        String filenameOnly = getFileOnly( filename );

        int endOfFilenameIndex = filenameOnly.lastIndexOf( '.' );

        if ( endOfFilenameIndex == -1 )
        {
            return filenameOnly;
        }
            return filenameOnly.substring( 0, endOfFilenameIndex );
    }

    public static String getFileExtension( String filename )
    {
        int endOfFilenameIndex = filename.lastIndexOf( '.' );

        if ( endOfFilenameIndex == -1 )
        {
            return "";
        }
            if ( endOfFilenameIndex + 1 >= filename.length() ) return "";

            return filename.substring( endOfFilenameIndex + 1 );
    }

    public static String normaliseDirectoryPath( String str )
    {
        // replace all file separator possibilities with the local machine separator
        str = normaliseFilePath( str );

        if ( !str.endsWith( fileSeperator ) )
        {
            // add a separator if it doesn't have one
            str += fileSeperator;
        }

        return str;
    }

    public static String normaliseFilePath( String str )
    {
        // replace all file separator possibilities with the local machine separator
        str = str.replace( WINDOWS_SEPERATOR, fileSeperator );
        str = str.replace( LINUX_SEPERATOR, fileSeperator );

        return str;
    }
    
    public static BufferedReader getFileReader( File file, Charset charset ) throws IOException
	{
		return new SafeBufferedReader( new InputStreamReader( new FileInputStream( file ), charset ) );
	}
    
    public static BufferedWriter getFileWriter( File file, Charset charset ) throws IOException
    {
        return new SafeBufferedWriter( new OutputStreamWriter( new FileOutputStream( file ), charset ) );
    }
	
    public static String getFileContents( File file, Charset charset ) throws IOException
    {
        Reader reader = getFileReader( file, charset );

        StringBuilder sb;
        try
        {
            char[] buffer = new char[8 * 1024];
            int read = -1;

            sb = new StringBuilder();
            while ( ( read = reader.read( buffer ) ) != -1 )
            {
                sb.append( buffer, 0, read );
            }
        }
        finally
        {
            reader.close();
        }

        return sb.toString();
    }
    
    public static File getTemporaryDirectory()
    {
    	File repository = new File(sparkTempPath);
		if ( !repository.exists() )  repository.mkdir();
		return repository; 
    }
}
