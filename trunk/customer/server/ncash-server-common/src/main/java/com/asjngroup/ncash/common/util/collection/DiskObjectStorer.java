package com.asjngroup.ncash.common.util.collection;


import java.util.NoSuchElementException;

import com.asjngroup.ncash.common.io.util.FileHelper;

import java.io.*;

public class DiskObjectStorer<T extends Serializable> implements ObjectStorer< T >
{
    private File tempFile = null;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;

    public void add( T o ) throws ObjectStorerException
    {
        try
        {
            if ( tempFile == null )
            {
                tempFile = FileHelper.createTempFile( "disk_object_storage", "" );
                outputStream = new ObjectOutputStream( new BufferedOutputStream( new FileOutputStream( tempFile ) ) );
            }
            outputStream.writeObject( o );
        }
        catch ( FileNotFoundException e )
        {
            throw new ObjectStorerException( e );
        }
        catch ( IOException e )
        {
            throw new ObjectStorerException( e );
        }
    }

    public IteratorEx< T > iterator() throws ObjectStorerException
    {
        try
        {
            if ( outputStream != null )
            {
                outputStream.close();
            }
            inputStream = new ObjectInputStream( new BufferedInputStream( new FileInputStream( tempFile ) ) );
            return new IteratorImpl< T >();
        }
        catch ( FileNotFoundException e )
        {
            throw new ObjectStorerException( e );
        }
        catch ( IOException e )
        {
            throw new ObjectStorerException( e );
        }
    }

    public void close() throws ObjectStorerException
    {
        try
        {
            if ( outputStream != null )
                outputStream.close();
            if ( inputStream != null )
                inputStream.close();
            if ( tempFile != null )
                tempFile.delete();
        }
        catch ( IOException e )
        {
            throw new ObjectStorerException( e );
        }
    }

    private class IteratorImpl<T> implements IteratorEx< T >
    {
        private T currentObject;
        private boolean hasMore = true;

        public IteratorImpl() throws ObjectStorerException
        {
            loadNext();
        }

        public boolean hasNext() throws Exception
        {
            return hasMore;
        }

        public T next() throws Exception
        {
            if ( !hasMore )
                throw new NoSuchElementException( "No more elements" );

            T nextObj = currentObject;

            loadNext();

            return nextObj;
        }

        private void loadNext() throws ObjectStorerException
        {
            try
            {
                inputStream.readObject();
            }
            catch ( OptionalDataException e )
            {
                if ( e.eof )
                {
                    hasMore = false;
                    currentObject = null;
                }
            }
            catch ( IOException e )
            {
                throw new ObjectStorerException( e );
            }
            catch ( ClassNotFoundException e )
            {
                throw new ObjectStorerException( e );
            }
        }
    }
}
