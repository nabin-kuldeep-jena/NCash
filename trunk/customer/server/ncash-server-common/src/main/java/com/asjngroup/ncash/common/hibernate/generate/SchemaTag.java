package com.asjngroup.ncash.common.hibernate.generate;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.asjngroup.ncash.common.ObjectHelper;
import com.asjngroup.ncash.common.exception.NCashRuntimeException;
import com.asjngroup.ncash.common.io.util.FileIOResourceHelper;
import com.asjngroup.ncash.common.util.StringHelper;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.strategy.Strategy;
import org.simpleframework.xml.strategy.Type;
import org.simpleframework.xml.strategy.Value;
import org.simpleframework.xml.stream.Format;
import org.simpleframework.xml.stream.InputNode;
import org.simpleframework.xml.stream.NodeMap;
import org.simpleframework.xml.stream.OutputNode;

@Root( name = "schema" )
public class SchemaTag implements Comparable<SchemaTag>
{

	@Attribute( name = "name" )
	private String name;

	@Attribute( name = "majorVersion" )
	private int majorVersion;

	@Attribute( name = "minorVersion" )
	private int minorVersion;

	@Attribute( name = "parentMajorVersion", required = false )
	private int parentMajorVersion;

	@Attribute( name = "parentMinorVersion", required = false )
	private int parentMinorVersion;

	@Attribute( name = "parent", required = false )
	private String parent;

	@Attribute( name = "product", required = false )
	private String product = new String();

	@Attribute( name = "serverPackage" )
	private String serverPackage;

	@ElementList( name = "entities", type = EntityMapping.class )
	private List<EntityMapping> entities = new ArrayList();

	public SchemaTag copy()
	{
		SchemaTag schemaTag = new SchemaTag();
		schemaTag.name = this.name;
		schemaTag.majorVersion = this.majorVersion;
		schemaTag.minorVersion = this.minorVersion;
		schemaTag.parentMajorVersion = this.parentMajorVersion;
		schemaTag.parentMinorVersion = this.parentMinorVersion;
		schemaTag.parent = this.parent;
		return schemaTag;
	}

	public EntityMapping findEntityMappingFromClass( Class< ? > paramClass )
	{
		return findEntityMapping( ObjectHelper.getClassOnlyName( paramClass ) );
	}

	public EntityMapping findEntityMapping( String paramString )
	{
		Iterator localIterator = this.entities.iterator();
		while ( localIterator.hasNext() )
		{
			EntityMapping localEntityMapping = ( EntityMapping ) localIterator.next();
			if ( localEntityMapping.getEntityName().equals( paramString ) )
				return localEntityMapping;
		}
		return null;
	}

	public EntityMapping findEntityMappingWithPrefix( String paramString )
	{
		Iterator localIterator = this.entities.iterator();
		while ( localIterator.hasNext() )
		{
			EntityMapping localEntityMapping = ( EntityMapping ) localIterator.next();
			if ( localEntityMapping.getEntityPrefix().equals( paramString ) )
				return localEntityMapping;
		}
		return null;
	}

	public Table findTable( String paramString )
	{
		String str = SchemaHelper.databaseNameToEntityName( paramString );
		EntityMapping localEntityMapping = findEntityMapping( str );
		if ( localEntityMapping == null )
			return null;
		return localEntityMapping.getTable();
	}

	public static SchemaTag loadSchema( InputStream paramInputStream )
	{
		try
		{
			Persister localPersister = new Persister();
			return ( ( SchemaTag ) localPersister.read( SchemaTag.class, paramInputStream ) );
		}
		catch ( Exception localException )
		{
			throw new NCashRuntimeException( localException );
		}
	}

	public void saveSchema( OutputStream paramOutputStream )
	{
		try
		{
			Persister localPersister = new Persister( new Strategy()
			{
				public Value read( Type type, NodeMap<InputNode> node, Map map ) throws Exception
				{
					return null;
				}

				public boolean write( Type type, Object value, NodeMap<OutputNode> node, Map map ) throws Exception
				{
					return false;
				}
			}, new Format( 2 ) );
			localPersister.write( this, paramOutputStream );
		}
		catch ( Exception localException )
		{
		}
		finally
		{
			FileIOResourceHelper.closeSilent( paramOutputStream );
		}
	}

	public static List<SchemaTag> sortSchemasByProduct( List<SchemaTag> paramList )
	{
		ArrayList localArrayList1 = new ArrayList();
		ArrayList localArrayList2 = new ArrayList( paramList );
		while ( !( localArrayList2.isEmpty() ) )
		{
			int i = localArrayList2.size();
			Iterator localIterator1 = localArrayList2.iterator();
			while ( localIterator1.hasNext() )
			{
				SchemaTag localSchemaTag1 = ( SchemaTag ) localIterator1.next();
				if ( StringHelper.isEmpty( localSchemaTag1.getParent() ) )
				{
					localArrayList1.add( localSchemaTag1 );
					localIterator1.remove();
				}
				else
				{
					ArrayList localArrayList3 = new ArrayList();
					Iterator localIterator2 = localArrayList1.iterator();
					while ( localIterator2.hasNext() )
					{
						SchemaTag localSchemaTag2 = ( SchemaTag ) localIterator2.next();
						if ( localSchemaTag1.getParent().equals( localSchemaTag2.getName() ) )
							localArrayList3.add( localSchemaTag2 );
					}
					localArrayList1.addAll( localArrayList3 );
				}
			}
			if ( localArrayList2.size() == i )
				throw new NCashRuntimeException( "Could not sort schemas, possible circular or broken reference." );
		}
		return localArrayList1;
	}

	public int compareTo( SchemaTag paramSchemaTag )
	{
		if ( !( getName().equals( paramSchemaTag.getName() ) ) )
			throw new IllegalArgumentException( "Cannot compare two schemas of a different name." );
		if ( !( getProduct().equals( paramSchemaTag.getProduct() ) ) )
			return -1;
		if ( getMajorVersion() != paramSchemaTag.getMajorVersion() )
			return ( getMajorVersion() - paramSchemaTag.getMajorVersion() );
		if ( getMinorVersion() != paramSchemaTag.getMinorVersion() )
			return ( getMinorVersion() - paramSchemaTag.getMinorVersion() );
		return 0;
	}

	public boolean equals( Object paramObject )
	{
		if ( this == paramObject )
			return true;
		if ( ( paramObject == null ) || ( super.getClass() != paramObject.getClass() ) )
			return false;
		SchemaTag localSchemaTag = ( SchemaTag ) paramObject;
		if ( this.majorVersion != localSchemaTag.majorVersion )
			return false;
		if ( this.minorVersion != localSchemaTag.minorVersion )
			return false;
		if ( this.parentMajorVersion != localSchemaTag.parentMajorVersion )
			return false;
		if ( this.parentMinorVersion != localSchemaTag.parentMinorVersion )
			return false;
		if ( !( this.name.equals( localSchemaTag.name ) ) )
			return false;
		if ( !( this.entities.equals( localSchemaTag.entities ) ) )
			return false;
		if ( this.parent != null )
			if ( this.parent.equals( localSchemaTag.parent ) )
				return true;
			else if ( localSchemaTag.parent == null )
				return false;
		return true;
	}

	public int hashCode()
	{
		int i = this.name.hashCode();
		i = 31 * i + this.majorVersion;
		i = 31 * i + this.minorVersion;
		return i;
	}

	public boolean isParentSchemaSufficient( SchemaTag paramSchemaTag )
	{
		if ( paramSchemaTag.getMajorVersion() < getParentMajorVersion() )
			return false;
		if ( paramSchemaTag.getMajorVersion() > getParentMajorVersion() )
			return true;
		if ( paramSchemaTag.getMinorVersion() < getParentMinorVersion() )
			return false;
		return ( paramSchemaTag.getMinorVersion() > getParentMinorVersion() );
	}

	public String toString()
	{
		return getName() + " v" + getMajorVersion() + "." + getMinorVersion();
	}

	public boolean isEqual( String paramString, int paramInt1, int paramInt2 )
	{
		if ( !( paramString.equals( getName() ) ) )
			return false;
		if ( paramInt1 != getMajorVersion() )
			return false;
		return ( paramInt2 == getMinorVersion() );
	}

	public String getName()
	{
		return this.name;
	}

	public void setName( String paramString )
	{
		this.name = paramString;
	}

	public int getMajorVersion()
	{
		return this.majorVersion;
	}

	public void setMajorVersion( int paramInt )
	{
		this.majorVersion = paramInt;
	}

	public int getMinorVersion()
	{
		return this.minorVersion;
	}

	public void setMinorVersion( int paramInt )
	{
		this.minorVersion = paramInt;
	}

	public int getParentMajorVersion()
	{
		return this.parentMajorVersion;
	}

	public void setParentMajorVersion( int paramInt )
	{
		this.parentMajorVersion = paramInt;
	}

	public int getParentMinorVersion()
	{
		return this.parentMinorVersion;
	}

	public void setParentMinorVersion( int paramInt )
	{
		this.parentMinorVersion = paramInt;
	}

	public String getParent()
	{
		return this.parent;
	}

	public void setParent( String paramString )
	{
		this.parent = paramString;
	}

	public String getServerPackage()
	{
		return this.serverPackage;
	}

	public void setServerPackage( String paramString )
	{
		this.serverPackage = paramString;
	}

	public List<EntityMapping> getEntityMappings()
	{
		return this.entities;
	}

	public void setEntityMappings( List<EntityMapping> paramList )
	{
		this.entities = paramList;
	}

	public Schema toSchema()
	{
		Schema localSchema = new Schema();
		Iterator localIterator = this.entities.iterator();
		while ( localIterator.hasNext() )
		{
			EntityMapping localEntityMapping = ( EntityMapping ) localIterator.next();
			List localList = localSchema.getDatabases();
			if ( !( localList.isEmpty() ) )
				( ( DataBase ) localList.get( 0 ) ).getTables().add( localEntityMapping.getTable() );
		}
		return localSchema;
	}

	public String getProduct()
	{
		return this.product;
	}

	public void setProduct( String paramString )
	{
		this.product = paramString;
	}
}