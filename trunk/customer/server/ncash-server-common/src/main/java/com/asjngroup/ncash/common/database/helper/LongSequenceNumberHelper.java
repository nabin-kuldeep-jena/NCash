package com.asjngroup.ncash.common.database.helper;

import org.hibernate.HibernateException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import com.asjngroup.ncash.common.database.hibernate.util.HibernateSession;

/**
 * @author nabin.jena
 *
 */
public class LongSequenceNumberHelper
{
    private String objectKey = null;
    private long blockSize = 1;

    public LongSequenceNumberHelper( String objectKey, long blockSize )
    {
        if ( blockSize <= 0 )
        {
            throw new IllegalArgumentException( "Invalid block size " + blockSize + " passed to id helper" );
        }

        this.objectKey = objectKey;
        this.blockSize = blockSize;
    }

    class IdBlock
    {
        final long limit;
        final AtomicLong nextId;

        IdBlock( long start, long limit )
        {
            this.limit = limit;
            this.nextId = new AtomicLong( start );
        }
    }

    final Object mutex = new Object();
    IdBlock currentBlock = null;

    public Long getNextId() throws HibernateException
    {
        IdBlock block = currentBlock;
        if ( block != null )
        {
            long id = block.nextId.getAndIncrement();
            if ( id < block.limit )
                return id;
        }

        synchronized (mutex)
        {
            if ( currentBlock != null && currentBlock != block )
            {
                long id = currentBlock.nextId.getAndIncrement();
                if ( id < currentBlock.limit )
                    return id;
            }

            long lowId = HibernateSession.generateLongId( objectKey, blockSize ).longValue();
            currentBlock = new IdBlock( lowId + 1, lowId + blockSize );
            return lowId;
        }
    }

    private static Map< Integer, Map< String, LongSequenceNumberHelper >> sequenceHelpers = new HashMap< Integer, Map< String, LongSequenceNumberHelper >>();

    public static synchronized LongSequenceNumberHelper getIdHelper( Class clazz, int blockSize )
    {
        return getIdHelper( null, clazz, blockSize );
    }

    public static synchronized LongSequenceNumberHelper getIdHelper( Integer stsId, Class clazz, int blockSize )
    {
        String objectKey = HibernateSession.getIdObjectKeyFromClass( clazz );
        return getIdHelper( stsId, objectKey, blockSize );
    }

    public static synchronized LongSequenceNumberHelper getIdHelper( String objectKey, long blockSize )
    {
        return getIdHelper( null, objectKey, blockSize );
    }

    public static synchronized LongSequenceNumberHelper getIdHelper( Integer stsId, String objectKey, long blockSize )
    {
        Integer stsIdKey = ( stsId == null ? Integer.valueOf( -1 ) : stsId );
        Map< String, LongSequenceNumberHelper > map = sequenceHelpers.get( stsIdKey );
        if ( map == null )
        {
            map = new HashMap< String, LongSequenceNumberHelper >();
            sequenceHelpers.put( stsIdKey, map );
        }

        LongSequenceNumberHelper idHelper = map.get( objectKey );
        if ( idHelper == null )
        {
            idHelper = new LongSequenceNumberHelper( objectKey, blockSize );
            map.put( objectKey, idHelper );
        }
        return idHelper;
    }
}
