package com.asjngroup.ncash.common.database.hibernate.util;

import org.hibernate.HibernateException;
import org.hibernate.Interceptor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.metadata.ClassMetadata;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.asjngroup.ncash.common.util.StringUtil;

public class HibernateTransaction
{

	  // map of classes to all object changes on this class
    private Map< Class, TransactionState > transactionMap = new HashMap< Class, TransactionState >();

    // session factory
    private SessionFactory sessionFactory;

    // interceptor
    private Interceptor interceptor;

    public HibernateTransaction( SessionFactory sessionFactory )
    {
        this( sessionFactory, null );
    }

    public HibernateTransaction( SessionFactory sessionFactory, Interceptor interceptor )
    {
        this.sessionFactory = sessionFactory;
        this.interceptor = interceptor;
    }

    // associate a new object
    public void save( Object obj ) throws HibernateException
    {
        TransactionState transactionState = getTransactionState( obj.getClass() );

        ClassMetadata classMetadata = sessionFactory.getClassMetadata( obj.getClass() );

        Integer currentId = (Integer)classMetadata.getIdentifier( obj );

        transactionState.newObjects.put( currentId, obj );
    }

    // update an object
    public void update( Object obj ) throws HibernateException
    {
        TransactionState transactionState = getTransactionState( obj.getClass() );

        ClassMetadata classMetadata = sessionFactory.getClassMetadata( obj.getClass() );
        Serializable id = classMetadata.getIdentifier( obj );

        if ( transactionState.newObjects.containsKey( id ) )
        {
            transactionState.newObjects.put( id, obj );
            return;
        }

        if ( transactionState.deletedObjects.containsKey( id ) )
        {
            throw new IllegalArgumentException( "A deleted object of class " + obj.getClass().getName() + " id " + id + " is already associated with this transaction" );
        }

        if ( transactionState.unchangedObjects.containsKey( id ) )
        {
            transactionState.unchangedObjects.remove( id );
        }

        transactionState.updatedObjects.put( id, obj );
    }

    //
    public void delete( Object obj ) throws HibernateException
    {
        TransactionState transactionState = getTransactionState( obj.getClass() );

        ClassMetadata classMetadata = sessionFactory.getClassMetadata( obj.getClass() );
        Serializable id = classMetadata.getIdentifier( obj );

        if ( transactionState.newObjects.containsKey( id ) )
        {
            transactionState.newObjects.remove( id );
            return;
        }

        if ( transactionState.deletedObjects.containsKey( id ) )
        {
            throw new IllegalArgumentException( StringUtil.create( "A deleted object of class %1 id %2 is already associated with this transaction", obj.getClass(), id ) );
        }

        if ( transactionState.updatedObjects.containsKey( id ) )
        {
            transactionState.updatedObjects.remove( id );
        }

        if ( transactionState.unchangedObjects.containsKey( id ) )
        {
            transactionState.unchangedObjects.remove( id );
        }

        transactionState.deletedObjects.put( id, obj );
    }

    public void deleteObjects( List objects ) throws HibernateException
    {
        for ( Object obj : objects )
        {
            delete( obj );
        }
    }

    public void attach( List objects ) throws HibernateException
    {
        if ( objects.size() == 0 )
            return;

        TransactionState transactionState = getTransactionState( objects.get( 0 ).getClass() );
        ClassMetadata metadata = sessionFactory.getClassMetadata( objects.get( 0 ).getClass() );

        for ( Object obj : objects )
        {
            Serializable id = metadata.getIdentifier( obj );
            transactionState.unchangedObjects.put( id, obj );
        }
    }

    public <T> T get( Class< T > clazz, Serializable id )
    {
        TransactionState transactionState = getTransactionState( clazz );

        T obj = (T)transactionState.newObjects.get( id );
        if ( obj != null )
            return obj;

        obj = (T)transactionState.updatedObjects.get( id );
        if ( obj != null )
            return obj;

        obj = (T)transactionState.unchangedObjects.get( id );
        if ( obj != null )
            return obj;

        return null;
    }

    public boolean hasChanges()
    {
        for ( Map.Entry< Class, TransactionState > entry : transactionMap.entrySet() )
        {
            TransactionState transactionState = entry.getValue();

            if ( transactionState.newObjects.size() > 0 )
                return true;
            if ( transactionState.updatedObjects.size() > 0 )
                return true;
            if ( transactionState.deletedObjects.size() > 0 )
                return true;
        }

        return false;
    }

    public void rollback()
    {
        transactionMap.clear();
    }

    public void flushTo( Session session ) throws HibernateException
    {
        List< Object > newObjects = new ArrayList< Object >();
        List< Object > updatedObjects = new ArrayList< Object >();
        List< Object > deletedObjects = new ArrayList< Object >();

        getNewChangeDeletedObjects( newObjects, updatedObjects, deletedObjects );

        HibernateUtil.updateDatabase( session, newObjects, updatedObjects, deletedObjects );

        clear();
    }

    public void commit() throws HibernateException
    {
        List< Object > newObjects = new ArrayList< Object >();
        List< Object > updatedObjects = new ArrayList< Object >();
        List< Object > deletedObjects = new ArrayList< Object >();

        getNewChangeDeletedObjects( newObjects, updatedObjects, deletedObjects );

        HibernateUtil.updateDatabase( sessionFactory, interceptor, newObjects, updatedObjects, deletedObjects );

        clear();
    }

    public void clear()
    {
        for ( Map.Entry< Class, TransactionState > entry : transactionMap.entrySet() )
        {
            TransactionState transactionState = entry.getValue();

            transactionState.newObjects.clear();
            transactionState.updatedObjects.clear();
            transactionState.deletedObjects.clear();
        }
    }

    private void getNewChangeDeletedObjects( List< Object > newObjects, List< Object > updatedObjects, List< Object > deletedObjects ) throws HibernateException
    {
        for ( Map.Entry< Class, TransactionState > entry : transactionMap.entrySet() )
        {
            TransactionState transactionState = entry.getValue();

            ClassMetadata metadata = sessionFactory.getClassMetadata( entry.getKey() );

            for ( Object obj : transactionState.newObjects.values() )
            {
                Integer id = (Integer)metadata.getIdentifier( obj );

                if ( id < 0 )
                {
                    metadata.setIdentifier( obj, HibernateSession.generateId( entry.getKey() ),null );
                }
            }

            newObjects.addAll( transactionState.newObjects.values() );
            updatedObjects.addAll( transactionState.updatedObjects.values() );
            deletedObjects.addAll( transactionState.deletedObjects.values() );
        }
    }

    public <T> List< T > getNewObjects( Class< T > clazz )
    {
        if ( !transactionMap.containsKey( HibernateUtil.getMappedInterfaceFromClass( clazz ) ) )
        {
            return new ArrayList< T >();
        }

        TransactionState transactionState = transactionMap.get( HibernateUtil.getMappedInterfaceFromClass( clazz ) );

        List< T > allObjects = new ArrayList< T >();

        allObjects.addAll( transactionState.newObjects.values() );

        return allObjects;
    }

    public <T> List< T > getAllObjects( Class< T > clazz )
    {
        if ( !transactionMap.containsKey( HibernateUtil.getMappedInterfaceFromClass( clazz ) ) )
        {
            return new ArrayList< T >();
        }

        TransactionState transactionState = transactionMap.get( HibernateUtil.getMappedInterfaceFromClass( clazz ) );

        List< T > allObjects = new ArrayList< T >();

        allObjects.addAll( transactionState.newObjects.values() );
        allObjects.addAll( transactionState.updatedObjects.values() );
        allObjects.addAll( transactionState.unchangedObjects.values() );

        return allObjects;
    }

    private TransactionState getTransactionState( Class clazz )
    {
        Class toClazz = HibernateUtil.getMappedInterfaceFromClass( clazz );

        // create a new map
        if ( !transactionMap.containsKey( toClazz ) )
        {
            transactionMap.put( HibernateUtil.getMappedInterfaceFromClass( toClazz ), new TransactionState() );
        }

        return transactionMap.get( toClazz );
    }
}

class TransactionState<T>
{
    Map< Serializable, T > newObjects = new HashMap< Serializable, T >();
    Map< Serializable, T > updatedObjects = new HashMap< Serializable, T >();
    Map< Serializable, T > deletedObjects = new HashMap< Serializable, T >();
    Map< Serializable, T > unchangedObjects = new HashMap< Serializable, T >();
}
