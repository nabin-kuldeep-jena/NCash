package com.asjngroup.ncash.common.hibernate.generate;


import org.hibernate.persister.entity.PropertyMapping;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.StringTokenizer;
import java.util.TreeMap;

import com.asjngroup.ncash.common.database.hibernate.ColumnDataType;
import com.asjngroup.ncash.common.exception.NCashRuntimeException;
import com.asjngroup.ncash.common.util.StringHelper;
import com.asjngroup.ncash.common.util.StringUtil;


public class SchemaHelper
{
	public static final int MAX_TABLE_NAME_LENGTH = 28;
	public static final int MAX_AUDITING_TABLE_NAME_LENGTH = 29;
	public static final int MAX_INDEX_NAME_LENGTH = 30;
	public static final int MAX_COLUMN_NAME_LENGTH = 30;

	public static String entityNameToDatabaseName( String paramString )
	{
		return StringHelper.camelCaseToUnderScore( StringHelper.lowerFirstChar( paramString ) );
	}

	public static String databaseNameToEntityName( String paramString )
	{
		return StringHelper.upperFirstChar( StringHelper.underScoreToCamelCase( paramString ) );
	}

	public static NestedObject getNestedObject( List<SchemaTag> paramList, EntityMapping paramObjectMapping, FieldMapping paramPropertyMapping )
	{
		String fieldName = paramPropertyMapping.getFieldName();
		if ( ( !( fieldName.endsWith( "Id" ) ) ) || ( !( paramPropertyMapping.isForeignKey() ) ) )
			return null;
		if ( isPrimaryKeyFieldMapping( paramObjectMapping.getEntityPrefix(), paramPropertyMapping ) )
			return null;
		if ( isVersionKeyPropertyMapping( paramPropertyMapping ) )
			return null;
		if ( isPtnIdPropertyMapping( paramPropertyMapping ) )
			return null;
		if ( fieldName.length() < 5 )
			throw new IllegalArgumentException( StringUtil.create( "The property name '%1' is invalid - it should be at least 5 characters long", new Object[]
			{ fieldName } ) );
		if ( !( paramPropertyMapping.isLoadNestedObject() ) )
			return null;
		String str2 = getNestedObjectPrefix( fieldName );
		if ( str2 == null )
			throw new IllegalArgumentException( StringUtil.create( "The property name '%1' is invalid ", new Object[]
			{ fieldName } ) );
		EntityMapping localObject = null;
		Iterator localIterator = paramList.iterator();
		while ( localIterator.hasNext() )
		{
			SchemaTag localSchemaTag = ( SchemaTag ) localIterator.next();
			EntityMapping localObjectMapping = localSchemaTag.findEntityMappingWithPrefix( str2 );
			if ( localObjectMapping != null )
				localObject = localObjectMapping;
		}
		if ( localObject == null )
			throw new NCashRuntimeException( "Failed to find the nested object for the property mapping '%1'", new Object[]
			{ fieldName } );
		return new NestedObject( paramPropertyMapping, localObject );
	}

	public static List<NestedObject> findNestedObjects( List<SchemaTag> paramList, EntityMapping paramObjectMapping )
	{
		ArrayList localArrayList = new ArrayList();
		Iterator localIterator = paramObjectMapping.getFieldMappings().iterator();
		while ( localIterator.hasNext() )
		{
			FieldMapping localPropertyMapping = ( FieldMapping ) localIterator.next();
			NestedObject localNestedObject = getNestedObject( paramList, paramObjectMapping, localPropertyMapping );
			localArrayList.add( localNestedObject );
		}
		return localArrayList;
	}

	public static List<SchemaTag> getLatestLoadedSchemaTags()
	{
		ArrayList localArrayList = new ArrayList();
		Iterator localIterator = Metadata.getAllMetadata().iterator();
		while ( localIterator.hasNext() )
		{
			Metadata localMetadata = ( Metadata ) localIterator.next();
			if ( localMetadata.getDatabaseRootSchema() != null )
				localArrayList.add( localMetadata.getDatabaseRootSchema().getLatestSchema().getSchema() );
		}
		return localArrayList;
	}

	public static EntityMapping findEntityMapping( String paramString )
	{
		return findEntityMapping( getLatestLoadedSchemaTags(), paramString );
	}

	public static EntityMapping findEntityMapping( List<SchemaTag> paramList, String paramString )
	{
		EntityMapping localObjectMapping = null;
		Iterator localIterator = paramList.iterator();
		while ( localIterator.hasNext() )
		{
			SchemaTag localSchemaTag = ( SchemaTag ) localIterator.next();
			localObjectMapping = localSchemaTag.findEntityMapping( paramString );
			if ( localObjectMapping != null )
				break;
		}
		return localObjectMapping;
	}

	public static String getIdPropertyName( String paramString )
	{
		return paramString + "Id";
	}

	public static String getDeleteFlPropertyName( String paramString )
	{
		return /*paramString +*/ "deleteFl";
	}

	public static String getVersionIdPropertyName( String paramString )
	{
		return /*paramString +*/ "versionId";
	}

	public static String getSystemGeneratedFlPropertyName()
	{
		return "systemGeneratedFl";
	}

	public static String getPtnIdPropertyName()
	{
		return "ptnId";
	}

	public static boolean isPrimaryKeyFieldMapping( String paramString, FieldMapping paramPropertyMapping )
	{
		return paramPropertyMapping.getFieldName().equals( getIdPropertyName( paramString ) );
	}

	public static boolean isDeleteFlPropertyMapping( FieldMapping paramPropertyMapping )
	{
		return paramPropertyMapping.getFieldName().endsWith( "deleteFl" );
	}

	public static boolean isVersionKeyPropertyMapping( FieldMapping paramPropertyMapping )
	{
		return paramPropertyMapping.getFieldName().endsWith( "versionId" );
	}

	public static boolean isPtnIdPropertyMapping( FieldMapping paramPropertyMapping )
	{
		return paramPropertyMapping.getFieldName().equals( getPtnIdPropertyName() );
	}

	public static boolean isSystemPropertyMapping( String paramString, FieldMapping paramPropertyMapping )
	{
		return ( ( isPrimaryKeyFieldMapping( paramString, paramPropertyMapping ) ) || ( isDeleteFlPropertyMapping( paramPropertyMapping ) ) || ( isVersionKeyPropertyMapping( paramPropertyMapping ) ) || ( isPtnIdPropertyMapping( paramPropertyMapping ) ) );
	}

	public static String getOrderNoPropertyName( String paramString )
	{
		return paramString + "OrderNo";
	}

	public static String formatObjectPrefix( String paramString ) throws IllegalArgumentException
	{
		if ( paramString == null )
			throw new IllegalArgumentException( StringUtil.create( "entity Prefix must be set", new Object[0] ) );
		if ( ( paramString.length() < 3 ) && ( paramString.length() > 4 ) )
			throw new IllegalArgumentException( StringUtil.create( "entity Prefix must be 3 or 4 characters long", new Object[0] ) );
		return StringHelper.lowerFirstChar( paramString.toLowerCase() );
	}

	public static List<PropertyMapping> getDisplayStringFieldMappings( EntityMapping paramObjectMapping, String paramString )
	{
		ArrayList localArrayList = new ArrayList();
		if ( StringHelper.isEmpty( paramString ) )
			return localArrayList;
		Object localObject1 = paramObjectMapping.getFieldMappings().iterator();
		while ( ( ( Iterator ) localObject1 ).hasNext() )
		{
			Object localObject2 = ( FieldMapping ) ( ( Iterator ) localObject1 ).next();
			if ( paramString.contains( ( ( FieldMapping ) localObject2 ).getFieldName() ) )
				localArrayList.add( localObject2 );
		}
		localObject1 = new TreeMap();
		Object localObject2 = localArrayList.iterator();
		while ( ( ( Iterator ) localObject2 ).hasNext() )
		{
			FieldMapping localPropertyMapping = ( FieldMapping ) ( ( Iterator ) localObject2 ).next();
			int i = paramString.indexOf( localPropertyMapping.getFieldName() );
			( ( SortedMap ) localObject1 ).put( Integer.valueOf( i ), localPropertyMapping );
		}
		return ( ( List<PropertyMapping> ) ( List<PropertyMapping> ) new ArrayList( ( ( SortedMap ) localObject1 ).values() ) );
	}

	public static List<PropertyMapping> getDisplayStringFieldMappings( EntityMapping paramObjectMapping )
	{
		return getDisplayStringFieldMappings( paramObjectMapping, paramObjectMapping.getDisplayString() );
	}

	public static List<Constraint> getUniqueConstraints( EntityMapping paramObjectMapping )
	{
		ArrayList localArrayList = new ArrayList();
		Iterator localIterator = paramObjectMapping.getConstraints().iterator();
		while ( localIterator.hasNext() )
		{
			Constraint localConstraint = ( Constraint ) localIterator.next();
			if ( localConstraint.isUnique() )
				localArrayList.add( localConstraint );
		}
		return localArrayList;
	}

	public static String getNestedObjectPrefix( String paramString )
	{
		String str = StringHelper.camelCaseToUnderScore( paramString );
		StringTokenizer localStringTokenizer = new StringTokenizer( str, "_" );
		int i = localStringTokenizer.countTokens() - 1;
		for ( int j = 1; localStringTokenizer.hasMoreTokens(); ++j )
		{
			if ( j == i )
				return localStringTokenizer.nextToken();
			localStringTokenizer.nextToken();
		}
		return null;
	}

	public static String getOneToManyRelationshipPrefix( String paramString )
	{
		return getOneToManyRelationshipPrefix( paramString, false );
	}

	public static String getOneToManyRelationshipPrefix( String paramString, boolean paramBoolean )
	{
		String str1 = StringHelper.camelCaseSeparate( paramString, "_" );
		StringTokenizer localStringTokenizer = new StringTokenizer( str1, "_" );
		String str2 = "";
		if ( localStringTokenizer.countTokens() == 2 )
		{
			str2 = "";
		}
		else
		{
			int i = localStringTokenizer.countTokens();
			int j = 1;
			if ( !( paramBoolean ) )
			{
				j = 2;
				localStringTokenizer.nextToken();
			}
			while ( j <= i - 2 )
			{
				str2 = str2 + localStringTokenizer.nextToken();
				++j;
			}
		}
		return str2;
	}

	public static String getTblPrefixFromCol( String paramString )
	{
		String str1 = StringHelper.camelCaseToUnderScore( paramString );
		StringTokenizer localStringTokenizer = new StringTokenizer( str1, "_" );
		String str2 = "";
		if ( localStringTokenizer.countTokens() >= 2 )
			str2 = localStringTokenizer.nextToken();
		return str2;
	}

	public static void validateTable( Table paramTable )
	{
		if ( ( paramTable.getTableName().length() > 4 ) && ( paramTable.getTableName().substring( paramTable.getTableName().length() - 4, paramTable.getTableName().length() ).equals( "_adt" ) ) )
			if ( paramTable.getTableName().length() > 29 )
				throw new NCashRuntimeException( "Table name '%1' is too long (max %2 chars supported)", new Object[]
				{ paramTable.getTableName(), Integer.valueOf( 29 ) } );
			else if ( paramTable.getTableName().length() > 26 )
				throw new NCashRuntimeException( "Table name '%1' is too long (max %2 chars supported)", new Object[]
				{ paramTable.getTableName(), Integer.valueOf( 26 ) } );
		Iterator localIterator = paramTable.getColumns().iterator();
		Object localObject;
		while ( localIterator.hasNext() )
		{
			localObject = ( Column ) localIterator.next();
			validateColumn( ( Column ) localObject );
		}
		localIterator = paramTable.getIndexes().iterator();
		while ( localIterator.hasNext() )
		{
			localObject = ( Index ) localIterator.next();
			validateIndex( ( Index ) localObject );
		}
	}

	public static void validateIndex( Index paramIndex )
	{
		if ( paramIndex.getIndexName().length() <= 30 )
			return;
		throw new NCashRuntimeException( "Index name '%1' is too long (max %2 chars supported)", new Object[]
		{ paramIndex.getIndexName(), Integer.valueOf( 30 ) } );
	}

	public static void validateColumn( Column paramColumn )
	{
		if ( paramColumn.getColumnName().length() <= 30 )
			return;
		throw new NCashRuntimeException( "Column name '%1' is too long (max %2 chars supported)", new Object[]
		{ paramColumn.getColumnName(), Integer.valueOf( 30 ) } );
	}

	public static String[] getTableColumnNames( Table paramTable )
	{
		String[] arrayOfString = new String[paramTable.getColumns().size()];
		for ( int i = 0; i < paramTable.getColumns().size(); ++i )
			arrayOfString[i] = ( ( Column ) paramTable.getColumns().get( i ) ).getColumnName();
		return arrayOfString;
	}

	public static ColumnDataType[] getTableColumnTypes( Table paramTable )
	{
		ColumnDataType[] arrayOfColumnDataType = new ColumnDataType[paramTable.getColumns().size()];
		for ( int i = 0; i < paramTable.getColumns().size(); ++i )
			arrayOfColumnDataType[i] = ( ( Column ) paramTable.getColumns().get( i ) ).getDataType();
		return arrayOfColumnDataType;
	}
}