package com.asjngroup.ncash.common.hibernate.generate;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.asjngroup.ncash.common.database.hibernate.ColumnDataType;
import com.asjngroup.ncash.common.util.StringHelper;

public class JavaObjectBuilder
{
	private EntityMapping entityMapping;
	private List<SchemaTag> allSchemas;
	private SchemaTag schema;
	private Table table;
	private String className;

	public JavaObjectBuilder( List<SchemaTag> paramList, SchemaTag paramSchemaTag, EntityMapping paramEntityMapping )
	{
		this.allSchemas = paramList;
		this.schema = paramSchemaTag;
		this.entityMapping = paramEntityMapping;
		this.table = paramEntityMapping.getTable();
	}

	private static String indent( int paramInt )
	{
		return StringHelper.fill( ' ', paramInt * 4 );
	}

	public void generateImplementation( PrintWriter paramPrintWriter, ArrayList<String> paramArrayList ) throws GenerateException
	{
		this.className = this.entityMapping.getEntityName();
		paramPrintWriter.println( new StringBuilder().append( "// Generated Code, DO NOT MODIFY!!!" ).append( StringHelper.NEW_LINE ).toString() );
		paramPrintWriter.println( new StringBuilder().append( "package " ).append( this.schema.getServerPackage() ).append( ";" ).append( StringHelper.NEW_LINE ).toString() );
		outputImports( paramPrintWriter, false, paramArrayList );
		paramPrintWriter.println( "@SuppressWarnings({\"unused\"})" );
		paramPrintWriter.println( "@javax.persistence.Entity" );
		paramPrintWriter.println( new StringBuilder().append( "@javax.persistence.Table( name = \"" ).append( this.table.getTableName() ).append( "\" )" ).toString() );
		outputTableAndIndexAnnotations( paramPrintWriter );
		outputFilters( paramPrintWriter );
		paramPrintWriter.println( new StringBuilder().append( "public class " ).append( this.className ).append( " extends AbstractHibernateObject" ).append( StringHelper.NEW_LINE ).append( "{" ).toString() );
		paramPrintWriter.println( "private static final long serialVersionUID = 1L;" );
		outputJavaInternalMembers( paramPrintWriter, null );
		outputJavaConstructors( paramPrintWriter, null );
		outputJavaAccessors( paramPrintWriter, null );
		outputSimpleDisplayString( paramPrintWriter );
		outputDisplayString( paramPrintWriter, this.entityMapping.getDisplayString(), "getDisplayString()" );
		outputAuditDisplayString( paramPrintWriter, ( StringHelper.isEmpty( this.entityMapping.getAuditingDisplayString() ) ) ? this.entityMapping.getDisplayString() : this.entityMapping.getAuditingDisplayString(), "getAuditingDisplayString()" );
		outputDisplayValue( paramPrintWriter );
		outputCopyEntity( paramPrintWriter );
		paramPrintWriter.println( "}" );
	}

	public void generateAuditImplementation( PrintWriter paramPrintWriter ) throws GenerateException
	{
		this.className = this.entityMapping.getEntityName();
		paramPrintWriter.println( new StringBuilder().append( "// Generated Code, DO NOT MODIFY!!!" ).append( StringHelper.NEW_LINE ).toString() );
		paramPrintWriter.println( new StringBuilder().append( "package " ).append( this.schema.getServerPackage() ).append( ".audit" ).append( ";" ).append( StringHelper.NEW_LINE ).toString() );
		outputImports( paramPrintWriter, true, null );
		paramPrintWriter.println( "@javax.persistence.Entity" );
		paramPrintWriter.println( new StringBuilder().append( "@javax.persistence.Table( name = \"" ).append( this.table.getTableName() ).append( "_adt" ).append( "\" )" ).toString() );
		outputTableAndIndexAnnotations( paramPrintWriter );
		outputFilters( paramPrintWriter );
		paramPrintWriter.println( "@SuppressWarnings({\"unused\"})" );
		paramPrintWriter.println( new StringBuilder().append( "public class " ).append( this.className ).append( "Adt" ).append( " extends AuditAbstractHibernateObject" ).append( StringHelper.NEW_LINE ).append( "{" ).toString() );
		paramPrintWriter.println( "private static final long serialVersionUID = 1L;" );
		outputJavaInternalMembers( paramPrintWriter, "Adt" );
		outputJavaConstructors( paramPrintWriter, "Adt" );
		outputJavaAccessors( paramPrintWriter, "Adt" );
		outputAuditFields( paramPrintWriter );
		outputSimpleDisplayString( paramPrintWriter );
		outputDisplayString( paramPrintWriter, this.entityMapping.getDisplayString(), "getDisplayString()" );
		outputDisplayValue( paramPrintWriter );
		outputCopyEntity( paramPrintWriter );
		paramPrintWriter.println( "}" );
	}

	private void outputImports( PrintWriter paramPrintWriter, boolean paramBoolean, ArrayList<String> paramArrayList )
	{
		paramPrintWriter.println( "import java.math.BigDecimal;" );
		paramPrintWriter.println( "import java.util.Set;" );
		paramPrintWriter.println( "import java.util.Collection;" );
		paramPrintWriter.println( "import java.util.List;" );
		paramPrintWriter.println( "import java.util.ArrayList;" );
		paramPrintWriter.println( "import java.util.LinkedList;" );
		paramPrintWriter.println( "import java.util.Map;" );
		if ( !( paramBoolean ) )
			paramPrintWriter.println( "import java.util.HashMap;" );
		paramPrintWriter.println();
		paramPrintWriter.println( "import org.hibernate.annotations.NotFound;" );
		paramPrintWriter.println( "import org.hibernate.annotations.NotFoundAction;" );
		paramPrintWriter.println();
		paramPrintWriter.println( "import org.joda.time.DateTime;" );
		paramPrintWriter.println( "import org.joda.time.format.DateTimeFormatter;" );
		paramPrintWriter.println();
		paramPrintWriter.println( "import com.asjngroup.ncash.common.database.hibernate.references.*;" );
		paramPrintWriter.println( "import com.asjngroup.ncash.common.util.StringUtil;" );
		paramPrintWriter.println( "import com.asjngroup.ncash.common.util.ResourceManager;" );
		if ( ( paramArrayList != null ) && ( !( paramArrayList.isEmpty() ) ) )
		{
			Iterator localIterator = paramArrayList.iterator();
			while ( localIterator.hasNext() )
			{
				String str = ( String ) localIterator.next();
				paramPrintWriter.println( new StringBuilder().append( "import " ).append( str ).append( ";" ).toString() );
			}
		}
		if ( paramBoolean )
		{
			paramPrintWriter.println( "import java.io.Serializable;" );
			paramPrintWriter.println( "import java.io.IOException;" );
			paramPrintWriter.println( "import com.asjngroup.ncash.common.database.helper.IdHelper;" );
			//paramPrintWriter.println( "import com.asjngroup.ncash.database.hibernate.audit.AuditHelper;" );
			//paramPrintWriter.println( "import com.asjngroup.ncash.database.hibernate.mappings.reference.*;" );
			//paramPrintWriter.println( "import com.asjngroup.ncash.database.hibernate.util.*;" );
			if ( !( "com.asjngroup.ncash.common.database.hibernate.references".equals( this.schema.getServerPackage() ) ) )
				paramPrintWriter.println( new StringBuilder().append( "import " ).append( this.schema.getServerPackage() ).append( ".*;" ).toString() );
		}
		outputParentSchemaImports( paramPrintWriter, this.schema );
		paramPrintWriter.println();
	}

	private void outputTableAndIndexAnnotations( PrintWriter paramPrintWriter )
	{
		paramPrintWriter.print( "@com.asjngroup.ncash.common.hibernate.annotation.Table( " );
		if ( this.table.getIndexes().size() > 0 )
		{
			paramPrintWriter.print( "indexes = { " );
			ArrayList localArrayList1 = new ArrayList();
			Iterator localIterator1 = this.table.getIndexes().iterator();
			while ( localIterator1.hasNext() )
			{
				Index localIndex = ( Index ) localIterator1.next();
				ArrayList localArrayList2 = new ArrayList();
				Iterator localIterator2 = localIndex.getColumnList().iterator();
				while ( localIterator2.hasNext() )
				{
					String str = ( String ) localIterator2.next();
					this.entityMapping.findFieldMapping( str );
					localArrayList2.add( StringHelper.quoteDouble( str ) );
				}
				localArrayList1.add( new StringBuilder().append( "@com.asjngroup.ncash.common.hibernate.annotation.Index( name = \"" ).append( localIndex.getIndexName() ).append( "\", columnNames = { " ).append( StringHelper.merge( localArrayList2, ", " ) ).append( " }, unique = " ).append( localIndex.isUnique() ).append( ", businessConstraint = " ).append( localIndex.isBusinessConstraint() ).append( ", displayName = " ).append( localIndex.isDisplayName() ).append( " )" ).toString() );
			}
			paramPrintWriter.println();
			paramPrintWriter.print( new StringBuilder().append( indent( 1 ) ).append( StringHelper.merge( localArrayList1, new StringBuilder().append( "," ).append( StringHelper.NEW_LINE ).append( indent( 1 ) ).toString() ) ).toString() );
			paramPrintWriter.println();
			paramPrintWriter.print( "}, " );
		}
		paramPrintWriter.print( new StringBuilder().append( "prefix = \"" ).append( this.table.getTablePrefix() ).append( "\"" ).toString() );
		paramPrintWriter.println( " )" );
	}

	private void outputFilters( PrintWriter paramPrintWriter )
	{
		ArrayList localArrayList = new ArrayList();
		if ( !( this.entityMapping.getEntityName().equals( "PartitionTbl" ) ) )
			localArrayList.add( "@org.hibernate.annotations.Filter( name = \"partitionFilter\" )" );
		localArrayList.add( new StringBuilder().append( "@org.hibernate.annotations.Filter( name = \"deletedFilter\", condition = \"" ).append( "delete_fl = :deleteFl\" )" ).toString() );
		paramPrintWriter.println( "@org.hibernate.annotations.Filters( {" );
		int i = localArrayList.size();
		for ( int j = 0; j < i; ++j )
		{
			paramPrintWriter.print( new StringBuilder().append( indent( 1 ) ).append( ( String ) localArrayList.get( j ) ).toString() );
			if ( j < i - 1 )
				paramPrintWriter.print( "," );
			paramPrintWriter.println();
		}
		paramPrintWriter.println( "} )" );
	}

	private void outputCopyEntity( PrintWriter paramPrintWriter )
	{
		printShallowCopy( paramPrintWriter );
		paramPrintWriter.println();
		printDeepCopy( paramPrintWriter );
	}

	private void printShallowCopy( PrintWriter paramPrintWriter )
	{
		paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( "public " ).append( this.className ).append( " shallowCopy(Map<AbstractHibernateObject, AbstractHibernateObject> map) {" ).toString() );
		String str1 = new StringBuilder().append( "__" ).append( StringHelper.lowerFirstChar( this.className ) ).toString();
		paramPrintWriter.println( new StringBuilder().append( indent( 2 ) ).append( this.className ).append( " " ).append( str1 ).append( " = new " ).append( this.className ).append( "();" ).toString() );
		paramPrintWriter.println( new StringBuilder().append( indent( 2 ) ).append( "super.shallowCopy( " ).append( str1 ).append( ");" ).toString() );
		int i = 0;
		Iterator localIterator = this.entityMapping.getFieldMappings().iterator();
		Object localObject1;
		Object localObject2;
		String str2;
		while ( localIterator.hasNext() )
		{
			localObject1 = ( FieldMapping ) localIterator.next();
			if ( HibernateObjectCodeGenerator.inBaseClass( this.entityMapping, ( FieldMapping ) localObject1 ) )
				continue;
			if ( ( ( FieldMapping ) localObject1 ).isNoCopy() )
				continue;
			localObject2 = SchemaHelper.getNestedObject( this.allSchemas, this.entityMapping, ( FieldMapping ) localObject1 );
			if ( ( localObject2 != null ) && ( ( ( !( ( ( FieldMapping ) localObject1 ).isOneToOneNestedObject() ) ) || ( ( ( FieldMapping ) localObject1 ).isRefCopy() ) ) ) )
			{
				str2 = printReferredObject( paramPrintWriter, new StringBuilder().append( ( ( NestedObject ) localObject2 ).getFieldName() ).append( ++i ).toString(), new StringBuilder().append( "get" ).append( StringHelper.upperFirstChar( ( ( NestedObject ) localObject2 ).getFieldName() ) ).append( "()" ).toString(), ( ( NestedObject ) localObject2 ).getForeignEntityMapping().getEntityName(), 2 );
				paramPrintWriter.println( new StringBuilder().append( indent( 2 ) ).append( str1 ).append( ".set" ).append( StringHelper.upperFirstChar( ( ( NestedObject ) localObject2 ).getFieldName() ) ).append( "(" ).append( str2 ).append( ");" ).toString() );
				paramPrintWriter.println( new StringBuilder().append( indent( 2 ) ).append( str1 ).append( ".set" ).append( StringHelper.upperFirstChar( ( ( FieldMapping ) localObject1 ).getFieldName() ) ).append( "(" ).append( "get" ).append( StringHelper.upperFirstChar( ( ( FieldMapping ) localObject1 ).getFieldName() ) ).append( "());" ).toString() );
			}
			else
			{
				paramPrintWriter.println( new StringBuilder().append( indent( 2 ) ).append( str1 ).append( ".set" ).append( StringHelper.upperFirstChar( ( ( FieldMapping ) localObject1 ).getFieldName() ) ).append( "(" ).append( "get" ).append( StringHelper.upperFirstChar( ( ( FieldMapping ) localObject1 ).getFieldName() ) ).append( "());" ).toString() );
			}
		}
		localIterator = this.entityMapping.getNestedCollections().iterator();
		while ( localIterator.hasNext() )
		{
			localObject1 = ( NestedCollection ) localIterator.next();
			if ( ( ( NestedCollection ) localObject1 ).isNoCopy() )
				continue;
			if ( !( ( ( NestedCollection ) localObject1 ).isRefCopy() ) )
				continue;
			localObject2 = getAddMethodPart( ( NestedCollection ) localObject1 );
			paramPrintWriter.println( new StringBuilder().append( indent( 2 ) ).append( "for(" ).append( ( ( NestedCollection ) localObject1 ).getForeignEntityMappingName() ).append( "  _" ).append( StringHelper.lowerFirstChar( ( ( NestedCollection ) localObject1 ).getForeignEntityMappingName() ) ).append( " : get" ).append( StringHelper.upperFirstChar( ( ( NestedCollection ) localObject1 ).getFieldName() ) ).append( "()) {" ).toString() );
			str2 = printReferredObject( paramPrintWriter, new StringBuilder().append( ( ( NestedCollection ) localObject1 ).getForeignEntityMappingName() ).append( ++i ).toString(), new StringBuilder().append( "_" ).append( StringHelper.lowerFirstChar( ( ( NestedCollection ) localObject1 ).getForeignEntityMappingName() ) ).toString(), ( ( NestedCollection ) localObject1 ).getForeignEntityMappingName(), 3 );
			paramPrintWriter.println( new StringBuilder().append( indent( 3 ) ).append( str1 ).append( ".add" ).append( StringHelper.upperFirstChar( ( String ) localObject2 ) ).append( "( " ).append( str2 ).append( ");" ).toString() );
			paramPrintWriter.println( new StringBuilder().append( indent( 2 ) ).append( "}" ).toString() );
		}
		paramPrintWriter.println( new StringBuilder().append( indent( 2 ) ).append( "return " ).append( str1 ).append( ";" ).toString() );
		paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( "}" ).toString() );
	}

	private String printReferredObject( PrintWriter paramPrintWriter, String paramString1, String paramString2, String paramString3, int paramInt )
	{
		String str = new StringBuilder().append( "_" ).append( StringHelper.lowerFirstChar( paramString1 ) ).toString();
		paramPrintWriter.println( new StringBuilder().append( indent( paramInt ) ).append( StringHelper.upperFirstChar( paramString3 ) ).append( " " ).append( str ).append( " = " ).append( paramString2 ).append( ";" ).toString() );
		paramPrintWriter.println( new StringBuilder().append( indent( paramInt ) ).append( "if (map.containsKey(" ).append( str ).append( "))" ).toString() );
		paramPrintWriter.println( new StringBuilder().append( indent( paramInt + 1 ) ).append( str ).append( " = (" ).append( StringHelper.upperFirstChar( paramString3 ) ).append( ") map.get(" ).append( str ).append( ");" ).toString() );
		return str;
	}

	private void printDeepCopy( PrintWriter paramPrintWriter )
	{
		String str1 = "_entity";
		paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( "public void deepCopy(AbstractHibernateObject " ).append( str1 ).append( ", Map<AbstractHibernateObject, AbstractHibernateObject> map, LinkedList<AbstractHibernateObject> queue) {" ).toString() );
		String str2 = new StringBuilder().append( "__" ).append( StringHelper.lowerFirstChar( this.className ) ).toString();
		paramPrintWriter.println( new StringBuilder().append( indent( 2 ) ).append( this.className ).append( " " ).append( str2 ).append( " = (" ).append( this.className ).append( ")" ).append( str1 ).append( ";" ).toString() );
		int i = 0;
		Iterator localIterator = this.entityMapping.getFieldMappings().iterator();
		Object localObject1;
		Object localObject2;
		String str3;
		String str4;
		while ( localIterator.hasNext() )
		{
			localObject1 = ( FieldMapping ) localIterator.next();
			if ( HibernateObjectCodeGenerator.inBaseClass( this.entityMapping, ( FieldMapping ) localObject1 ) )
				continue;
			if ( ( ( FieldMapping ) localObject1 ).isNoCopy() )
				continue;
			localObject2 = SchemaHelper.getNestedObject( this.allSchemas, this.entityMapping, ( FieldMapping ) localObject1 );
			if ( ( localObject2 != null ) && ( ( ( ( ( ( FieldMapping ) localObject1 ).isOneToOneNestedObject() ) && ( !( ( ( FieldMapping ) localObject1 ).isRefCopy() ) ) ) || ( ( ( FieldMapping ) localObject1 ).isDeepCopy() ) ) ) )
			{
				str3 = new StringBuilder().append( "_entity" ).append( ++i ).toString();
				paramPrintWriter.println( new StringBuilder().append( indent( 2 ) ).append( StringHelper.upperFirstChar( ( ( NestedObject ) localObject2 ).getForeignEntityMapping().getEntityName() ) ).append( " " ).append( str3 ).append( " = get" ).append( StringHelper.upperFirstChar( ( ( NestedObject ) localObject2 ).getFieldName() ) ).append( "();" ).toString() );
				str4 = new StringBuilder().append( "copyEntity" ).append( i ).toString();
				paramPrintWriter.println( new StringBuilder().append( indent( 2 ) ).append( " if (" ).append( str3 ).append( " != null ) {" ).toString() );
				printOwnedObject( paramPrintWriter, ( ( NestedObject ) localObject2 ).getForeignEntityMapping().getEntityName(), str3, str4, 3 );
				paramPrintWriter.println( new StringBuilder().append( indent( 3 ) ).append( str2 ).append( ".set" ).append( StringHelper.upperFirstChar( ( ( NestedObject ) localObject2 ).getFieldName() ) ).append( "(" ).append( str4 ).append( ");" ).toString() );
				paramPrintWriter.println( new StringBuilder().append( indent( 2 ) ).append( "}" ).toString() );
			}
		}
		localIterator = this.entityMapping.getNestedCollections().iterator();
		while ( localIterator.hasNext() )
		{
			localObject1 = ( NestedCollection ) localIterator.next();
			if ( ( ( NestedCollection ) localObject1 ).isNoCopy() )
				continue;
			if ( ( ( NestedCollection ) localObject1 ).isRefCopy() )
				continue;
			localObject2 = getAddMethodPart( ( NestedCollection ) localObject1 );
			paramPrintWriter.println( new StringBuilder().append( indent( 2 ) ).append( "for(" ).append( ( ( NestedCollection ) localObject1 ).getForeignEntityMappingName() ).append( "  _" ).append( StringHelper.lowerFirstChar( ( ( NestedCollection ) localObject1 ).getForeignEntityMappingName() ) ).append( " : get" ).append( StringHelper.upperFirstChar( ( ( NestedCollection ) localObject1 ).getFieldName() ) ).append( "()) {" ).toString() );
			str3 = new StringBuilder().append( "_" ).append( StringHelper.lowerFirstChar( ( ( NestedCollection ) localObject1 ).getForeignEntityMappingName() ) ).toString();
			str4 = new StringBuilder().append( "copyEntity" ).append( ++i ).toString();
			printOwnedObject( paramPrintWriter, ( ( NestedCollection ) localObject1 ).getForeignEntityMappingName(), str3, str4, 3 );
			paramPrintWriter.println( new StringBuilder().append( indent( 3 ) ).append( str2 ).append( ".add" ).append( StringHelper.upperFirstChar( ( String ) localObject2 ) ).append( "( " ).append( str4 ).append( ");" ).toString() );
			paramPrintWriter.println( new StringBuilder().append( indent( 2 ) ).append( "}" ).toString() );
		}
		paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( "}" ).toString() );
	}

	private String getAddMethodPart( NestedCollection paramNestedCollection )
	{
		String str = null;
		if ( paramNestedCollection.getFieldName().endsWith( "es" ) )
			str = StringHelper.removeRight( paramNestedCollection.getFieldName(), 2 );
		else if ( paramNestedCollection.getFieldName().endsWith( "s" ) )
			str = StringHelper.removeRight( paramNestedCollection.getFieldName(), 1 );
		return str;
	}

	private void printOwnedObject( PrintWriter paramPrintWriter, String paramString1, String paramString2, String paramString3, int paramInt )
	{
		paramPrintWriter.println( new StringBuilder().append( indent( paramInt ) ).append( StringHelper.upperFirstChar( paramString1 ) ).append( " " ).append( paramString3 ).append( " = null;" ).toString() );
		paramPrintWriter.println( new StringBuilder().append( indent( paramInt ) ).append( "if (map.containsKey(" ).append( paramString2 ).append( ")) {" ).toString() );
		paramPrintWriter.println( new StringBuilder().append( indent( paramInt + 1 ) ).append( paramString3 ).append( " = (" ).append( StringHelper.upperFirstChar( paramString1 ) ).append( ") map.get(" ).append( paramString2 ).append( ");" ).toString() );
		paramPrintWriter.println( new StringBuilder().append( indent( paramInt ) ).append( "}" ).toString() );
		paramPrintWriter.println( new StringBuilder().append( indent( paramInt ) ).append( "else {" ).toString() );
		paramPrintWriter.println( new StringBuilder().append( indent( paramInt + 1 ) ).append( paramString3 ).append( " = " ).append( paramString2 ).append( ".shallowCopy(map);" ).toString() );
		paramPrintWriter.println( new StringBuilder().append( indent( paramInt + 1 ) ).append( "map.put(" ).append( paramString2 ).append( "," ).append( paramString3 ).append( ");" ).toString() );
		paramPrintWriter.println( new StringBuilder().append( indent( paramInt + 1 ) ).append( "queue.add(" ).append( paramString2 ).append( ");" ).toString() );
		paramPrintWriter.println( new StringBuilder().append( indent( paramInt ) ).append( "}" ).toString() );
	}

	public static String getJavaType( ColumnDataType paramColumnDataType, boolean paramBoolean ) throws GenerateException
	{
		paramBoolean = true;
		switch( paramColumnDataType.ordinal() )
		{
		case 0:
			if ( paramBoolean )
				return "Long";
			return "long";
		case 1:
			if ( paramBoolean )
				return "Integer";
			return "int";
		case 2:
			return "String";
		case 3:
			return "DateTime";
		case 4:
			if ( paramBoolean )
				return "Boolean";
			return "boolean";
		case 5:
			return "BigDecimal";
		case 6:
		case 7:
		}
		throw new GenerateException( "Unknown ColumnDataType : %1", new Object[]
		{ "getJavaType()" } );
	}

	private void outputParentSchemaImports( PrintWriter paramPrintWriter, SchemaTag paramSchemaTag )
	{
		if ( StringHelper.isEmpty( paramSchemaTag.getParent() ) )
			return;
		Iterator localIterator = this.allSchemas.iterator();
		while ( localIterator.hasNext() )
		{
			SchemaTag localSchemaTag = ( SchemaTag ) localIterator.next();
			if ( localSchemaTag.getName().equals( paramSchemaTag.getParent() ) )
			{
				paramPrintWriter.println( new StringBuilder().append( "import " ).append( localSchemaTag.getServerPackage() ).append( ".*" ).append( ";" ).toString() );
				outputParentSchemaImports( paramPrintWriter, localSchemaTag );
				return;
			}
		}
	}

	private void outputJavaInternalMembers( PrintWriter paramPrintWriter, String paramString ) throws GenerateException
	{
		paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( "// Properties" ).toString() );
		Iterator localIterator = this.entityMapping.getFieldMappings().iterator();
		Object localObject1;
		Object localObject2;
		while ( localIterator.hasNext() )
		{
			localObject1 = ( FieldMapping ) localIterator.next();
			if ( HibernateObjectCodeGenerator.inBaseClass( this.entityMapping, ( FieldMapping ) localObject1 ) )
				continue;
			localObject2 = SchemaHelper.getNestedObject( this.allSchemas, this.entityMapping, ( FieldMapping ) localObject1 );
			if ( localObject2 != null )
			{
				paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( "private " ).append( ( ( NestedObject ) localObject2 ).getForeignEntityMapping().getEntityName() ).append( " " ).append( ( ( NestedObject ) localObject2 ).getFieldName() ).append( " = null;" ).toString() );
				if ( "entClientAsmId".equals( ( ( FieldMapping ) localObject1 ).getFieldName() ) )
					paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( "private " ).append( getJavaType( ( ( FieldMapping ) localObject1 ).getDatatype(), !( ( ( FieldMapping ) localObject1 ).isMandatory() ) ) ).append( " " ).append( ( ( FieldMapping ) localObject1 ).getFieldName() ).append( " = " ).append( "0" ).append( ";" ).toString() );
				else
					paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( "private " ).append( getJavaType( ( ( FieldMapping ) localObject1 ).getDatatype(), !( ( ( FieldMapping ) localObject1 ).isMandatory() ) ) ).append( " " ).append( ( ( FieldMapping ) localObject1 ).getFieldName() ).append( " = " ).append( getJavaDefaultValue( ( ( FieldMapping ) localObject1 ).getDatatype(), !( ( ( FieldMapping ) localObject1 ).isMandatory() ) ) ).append( ";" ).toString() );
			}
			else
			{
				paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( "private " ).append( getJavaType( ( ( FieldMapping ) localObject1 ).getDatatype(), !( ( ( FieldMapping ) localObject1 ).isMandatory() ) ) ).append( " " ).append( ( ( FieldMapping ) localObject1 ).getFieldName() ).append( " = " ).append( getJavaDefaultValue( ( ( FieldMapping ) localObject1 ).getDatatype(), !( ( ( FieldMapping ) localObject1 ).isMandatory() ) ) ).append( ";" ).toString() );
			}
		}
		localIterator = this.entityMapping.getNestedCollections().iterator();
		while ( localIterator.hasNext() )
		{
			localObject1 = ( NestedCollection ) localIterator.next();
			localObject2 = getNestedCollectionOrderColumn( ( NestedCollection ) localObject1 );
			String str = ( localObject2 != null ) ? "List" : "Collection";
			paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( "private " ).append( str ).append( "< " ).append( ( ( NestedCollection ) localObject1 ).getForeignEntityMappingName() ).append( " > " ).append( ( ( NestedCollection ) localObject1 ).getFieldName() ).append( " = new ArrayList< " ).append( ( ( NestedCollection ) localObject1 ).getForeignEntityMappingName() ).append( " >();" ).toString() );
		}
		if ( paramString != null )
			paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( "private Integer " ).append( this.table.getTablePrefix() ).append( "Id = 0;" ).toString() );
		paramPrintWriter.println();
	}

	private void outputJavaConstructors( PrintWriter paramPrintWriter, String paramString )
	{
		if ( paramString != null )
			paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( "public " ).append( this.className ).append( paramString ).append( "()" ).toString() );
		else
			paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( "public " ).append( this.className ).append( "()" ).toString() );
		paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( "{" ).toString() );
		if ( paramString != null )
			paramPrintWriter.println( new StringBuilder().append( indent( 2 ) ).append( "super( ); " ).toString() );
		else
			paramPrintWriter.println( new StringBuilder().append( indent( 2 ) ).append( "this( false ); " ).toString() );
		paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( "}" ).toString() );
		paramPrintWriter.println();
		if ( paramString == null )
		{
			paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( "public " ).append( this.className ).append( "(boolean generateId)" ).toString() );
			paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( "{" ).toString() );
			paramPrintWriter.println( new StringBuilder().append( indent( 2 ) ).append( "super( generateId ); " ).toString() );
			paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( "}" ).toString() );
		}
		paramPrintWriter.println();
	}

	private void outputAuditFields( PrintWriter paramPrintWriter )
	{
		paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( "@javax.persistence.Id" ).toString() );
		paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( "@javax.persistence.Column( name = \"audit_id\" )" ).toString() );
		paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( "@org.hibernate.annotations.Type( type = \"int\" )" ).toString() );
		paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( ( "PartitionTbl".equals( this.className ) ) ? "@javax.validation.constraints.Min( 0 )" : "@javax.validation.constraints.Min( 1 )" ).toString() );
		paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( "public int getAuditId() { return super.getId(); }" ).toString() );
		paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( "public void setAuditId( int auditId ){ super.setId( auditId ); }" ).toString() );
		paramPrintWriter.println();
		paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( "public void initialise( HibernateObject object, String auditOperationCd )" ).toString() );
		paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( "{" ).toString() );
		paramPrintWriter.println( new StringBuilder().append( indent( 2 ) ).append( "super.initialise( object, auditOperationCd );" ).toString() );
		paramPrintWriter.println();
		paramPrintWriter.println( new StringBuilder().append( indent( 2 ) ).append( this.className ).append( " obj = (" ).append( this.className ).append( ")object;" ).toString() );
		Iterator localIterator = this.entityMapping.getFieldMappings().iterator();
		while ( localIterator.hasNext() )
		{
			FieldMapping localFieldMapping = ( FieldMapping ) localIterator.next();
			if ( localFieldMapping.getFieldName().equals( new StringBuilder().append( this.entityMapping.getEntityPrefix() ).append( "DeleteFl" ).toString() ) )
				continue;
			if ( localFieldMapping.getFieldName().equals( new StringBuilder().append( this.entityMapping.getEntityPrefix() ).append( "VersionId" ).toString() ) )
				continue;
			if ( localFieldMapping.getFieldName().equals( "ptnId" ) )
				paramPrintWriter.println( new StringBuilder().append( indent( 2 ) ).append( "this.setPartitionId ( obj.getPartitionId()" ).append( "  );" ).toString() );
			paramPrintWriter.println( new StringBuilder().append( indent( 2 ) ).append( "this.set" ).append( StringHelper.upperFirstChar( localFieldMapping.getFieldName() ) ).append( "( obj.get" ).append( StringHelper.upperFirstChar( localFieldMapping.getFieldName() ) ).append( "()" ).append( "  );" ).toString() );
			NestedObject localNestedObject = SchemaHelper.getNestedObject( this.allSchemas, this.entityMapping, localFieldMapping );
			if ( localNestedObject != null )
				paramPrintWriter.println( new StringBuilder().append( indent( 2 ) ).append( "this.set" ).append( StringHelper.upperFirstChar( localNestedObject.getFieldName() ) ).append( "( obj.get" ).append( StringHelper.upperFirstChar( localNestedObject.getFieldName() ) ).append( "()" ).append( "  );" ).toString() );
			paramPrintWriter.println();
		}
		paramPrintWriter.println( new StringBuilder().append( indent( 2 ) ).append( "if(!auditOperationCd.equals(AuditHelper.AUDIT_OBJECT_UPDATE_PROPERTIES)){" ).toString() );
		paramPrintWriter.println( new StringBuilder().append( indent( 4 ) ).append( "Integer id = IdHelper.generateId( this.getClass() );" ).toString() );
		paramPrintWriter.println( new StringBuilder().append( indent( 4 ) ).append( "this.setAuditId(id);" ).toString() );
		paramPrintWriter.println( new StringBuilder().append( indent( 2 ) ).append( "}" ).toString() );
		paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( "}" ).toString() );
	}

	private void outputJavaAccessors( PrintWriter paramPrintWriter, String paramString ) throws GenerateException
	{
		int i = ( !( this.entityMapping.getEntityName().equals( "ChangedClass" ) ) ) ? 1 : 0;
		if ( paramString == null )
		{
			paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( "@javax.persistence.Id" ).toString() );
			paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( "@javax.persistence.Column( name = \"" ).append( this.table.getTablePrefix() ).append( "_id" ).append( "\" )" ).toString() );
			paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( "@org.hibernate.annotations.Type( type = \"int\" )" ).toString() );
			paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( ( "PartitionTbl".equals( this.className ) ) ? "@javax.validation.constraints.Min( 0 )" : "@javax.validation.constraints.Min( 1 )" ).toString() );
			paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( "public int get" ).append( StringHelper.underScoreToCamelCase( this.table.getTablePrefix() ) ).append( "Id() { return getId(); }" ).toString() );
			paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( "public void set" ).append( StringHelper.underScoreToCamelCase( this.table.getTablePrefix() ) ).append( "Id( int id ) { setId( id ); }" ).toString() );
		}
		else
		{
			paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( "@javax.persistence.Column( name = \"" ).append( this.table.getTablePrefix() ).append( "_id" ).append( "\" )" ).toString() );
			paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( "@org.hibernate.annotations.Type( type = \"int\" )" ).toString() );
			paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( ( "PartitionTbl".equals( this.className ) ) ? "@javax.validation.constraints.Min( 0 )" : "@javax.validation.constraints.Min( 1 )" ).toString() );
			paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( "public int get" ).append( StringHelper.underScoreToCamelCase( this.table.getTablePrefix() ) ).append( "Id() { return this." ).append( this.table.getTablePrefix() ).append( "Id; }" ).toString() );
			paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( "public void set" ).append( StringHelper.underScoreToCamelCase( this.table.getTablePrefix() ) ).append( "Id( int id ) { this." ).append( this.table.getTablePrefix() ).append( "Id = id; }" ).toString() );
		}
		paramPrintWriter.println();
		if ( i != 0 )
		{
			if ( ( paramString == null ) || ( !( paramString.equals( "Adt" ) ) ) )
				paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( "@javax.persistence.Version" ).toString() );
			paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( "@javax.persistence.Column( name = \"" ).append( "version_id" ).append( "\" )" ).toString() );
			paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( "@org.hibernate.annotations.Type( type = \"int\" )" ).toString() );
			paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( "public int getVersionId() { return super.getVersionId(); }" ).toString() );
		}
		else
		{
			paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( "@javax.persistence.Transient" ).toString() );
			paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( "public int getVersionId() { throw new UnsupportedOperationException(); }" ).toString() );
			paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( "public void setVersionId( int versionId ) { throw new UnsupportedOperationException(); }" ).toString() );
		}
		paramPrintWriter.println();
		paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( "@javax.persistence.Basic" ).toString() );
		paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( "@javax.persistence.Column( name = \"" ).append( "delete_fl" ).append( "\" )" ).toString() );
		paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( "@org.hibernate.annotations.Type( type = \"yes_no\" )" ).toString() );
		paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( "@javax.validation.constraints.NotNull" ).toString() );
		paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( "public Boolean getDeleteFl() { return super.getDeleteFl(); }" ).toString() );
		paramPrintWriter.println();
		/*paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( "@javax.persistence.Basic" ).toString() );
		paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( "@javax.persistence.Column( name = \"" ).append( "system_generated_fl" ).append( "\" )" ).toString() );
		paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( "@org.hibernate.annotations.Type( type = \"yes_no\" )" ).toString() );
		paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( "public Boolean getSystemGeneratedFl() { return super.getSystemGeneratedFl(); }" ).toString() );*/
		paramPrintWriter.println();
		if ( !( SchemaHelper.getIdPropertyName( this.entityMapping.getEntityPrefix() ).equalsIgnoreCase( "ptnId" ) ) )
		{
			paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( "@javax.persistence.Basic" ).toString() );
			paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( "@javax.persistence.Column( name = \"ptn_id\" )" ).toString() );
			paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( "@org.hibernate.annotations.Type( type = \"int\" )" ).toString() );
			paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( "@javax.validation.constraints.Min( 0 )" ).toString() );
			paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( "public int getPartitionId() { return super.getPartitionId(); }" ).toString() );
		}
		else
		{
			paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( "@javax.persistence.Transient" ).toString() );
			paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( "public int getPartitionId() { return -1; }" ).toString() );
			paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( "public void setPartitionId( int partitionId ) {}" ).toString() );
		}
		paramPrintWriter.println();
		Iterator localIterator = this.entityMapping.getFieldMappings().iterator();
		Object localObject1;
		Object localObject2;
		Object localObject3;
		while ( localIterator.hasNext() )
		{
			localObject1 = ( FieldMapping ) localIterator.next();
			if ( HibernateObjectCodeGenerator.inBaseClass( this.entityMapping, ( FieldMapping ) localObject1 ) )
				continue;
			localObject2 = SchemaHelper.getNestedObject( this.allSchemas, this.entityMapping, ( FieldMapping ) localObject1 );
			if ( localObject2 != null )
			{
				localObject3 = this.table.findColumn( ( ( FieldMapping ) localObject1 ).getColumn().getColumnName() );
				if ( ( ( FieldMapping ) localObject1 ).isOneToOneNestedObject() )
					paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( "@javax.persistence.OneToOne( fetch = javax.persistence.FetchType.EAGER )" ).toString() );
				else
					paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( "@javax.persistence.ManyToOne( fetch = javax.persistence.FetchType.LAZY )" ).toString() );
				paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( "@javax.persistence.JoinColumn( name = \"" ).append( ( ( FieldMapping ) localObject1 ).getColumn().getColumnName() ).append( "\", nullable = " ).append( !( ( ( Column ) localObject3 ).isMandatory() ) ).append( ", insertable = true, updatable = true )" ).toString() );
				paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( "@NotFound( action = NotFoundAction.IGNORE )" ).toString() );
				paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( "public " ).append( ( ( NestedObject ) localObject2 ).getForeignEntityMapping().getEntityName() ).append( " get" ).append( StringHelper.upperFirstChar( ( ( NestedObject ) localObject2 ).getFieldName() ) ).append( "() { return " ).append( ( ( NestedObject ) localObject2 ).getFieldName() ).append( "; }" ).toString() );
				paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( "public void set" ).append( StringHelper.upperFirstChar( ( ( NestedObject ) localObject2 ).getFieldName() ) ).append( "( " ).append( ( ( NestedObject ) localObject2 ).getForeignEntityMapping().getEntityName() ).append( " val ) { this." ).append( ( ( NestedObject ) localObject2 ).getFieldName() ).append( " = val; }" ).toString() );
				paramPrintWriter.println();
				paramPrintWriter.println();
			}
			localObject3 = this.table.findColumn( ( ( FieldMapping ) localObject1 ).getColumn().getColumnName() );
			paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( "@javax.persistence.Basic" ).toString() );
			paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( "@javax.persistence.Column( name = \"" ).append( ( ( FieldMapping ) localObject1 ).getColumn().getColumnName() ).append( "\", nullable = " ).append( !( ( ( Column ) localObject3 ).isMandatory() ) ).append( ", insertable = " ).append( ( localObject2 == null ) ? "true" : "false" ).append( ", updatable = " ).append( ( localObject2 == null ) ? "true" : "false" ).append( " )" ).toString() );
			paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( "@org.hibernate.annotations.Type( type = \"" ).append( getHibernateType( ( ( Column ) localObject3 ).getDataType() ) ).append( "\" )" ).toString() );
			if ( localObject2 == null )
			{
				if ( ( ( Column ) localObject3 ).isMandatory() )
					paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( "@javax.validation.constraints.NotNull" ).toString() );
				if ( ( ( Column ) localObject3 ).getDataType() == ColumnDataType.String )
				{
					if ( ( ( Column ) localObject3 ).isMandatory() )
						paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( "@org.hibernate.validator.constraints.NotEmpty" ).toString() );
					if ( ( ( Column ) localObject3 ).getLength() == -1 )
						paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( "@org.hibernate.validator.constraints.Length( max = 255 )" ).toString() );
					else
						paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( "@org.hibernate.validator.constraints.Length( max = " ).append( ( ( Column ) localObject3 ).getLength() ).append( " )" ).toString() );
				}
			}
			paramPrintWriter.print( new StringBuilder().append( indent( 1 ) ).append( "public " ).append( getJavaType( ( ( Column ) localObject3 ).getDataType(), !( ( ( Column ) localObject3 ).isMandatory() ) ) ).append( " get" ).append( StringHelper.upperFirstChar( ( ( FieldMapping ) localObject1 ).getFieldName() ) ).append( "() { " ).toString() );
			paramPrintWriter.println( new StringBuilder().append( "return this." ).append( ( ( FieldMapping ) localObject1 ).getFieldName() ).append( "; }" ).toString() );
			paramPrintWriter.print( new StringBuilder().append( indent( 1 ) ).append( ( localObject2 == null ) ? "public " : "public " ).append( "void set" ).append( StringHelper.upperFirstChar( ( ( FieldMapping ) localObject1 ).getFieldName() ) ).append( "( " ).append( getJavaType( ( ( Column ) localObject3 ).getDataType(), !( ( ( Column ) localObject3 ).isMandatory() ) ) ).append( " val ) {" ).toString() );
			paramPrintWriter.println( new StringBuilder().append( "this." ).append( ( ( FieldMapping ) localObject1 ).getFieldName() ).append( " = val; }" ).append( StringHelper.NEW_LINE ).toString() );
		}
		localIterator = this.entityMapping.getNestedCollections().iterator();
		while ( localIterator.hasNext() )
		{
			localObject1 = ( NestedCollection ) localIterator.next();
			localObject2 = SchemaHelper.getOneToManyRelationshipPrefix( ( ( NestedCollection ) localObject1 ).getForeignFieldName() );
			localObject3 = new StringBuilder().append( ( String ) localObject2 ).append( this.entityMapping.getEntityName() ).toString();
			String str1 = StringHelper.lowerFirstChar( ( String ) localObject3 );
			paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( "@javax.persistence.OneToMany( fetch = javax.persistence.FetchType.LAZY, mappedBy = \"" ).append( str1 ).append( "\" )" ).toString() );
			String str2 = getNestedCollectionOrderColumn( ( NestedCollection ) localObject1 );
			if ( str2 != null )
				paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( "@org.hibernate.annotations.OrderBy(  clause= \"" ).append( str2 ).append( "\" )" ).toString() );
			String str3 = ( str2 != null ) ? "List" : "Collection";
			paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( "public " ).append( str3 ).append( "< " ).append( ( ( NestedCollection ) localObject1 ).getForeignEntityMappingName() ).append( " > get" ).append( StringHelper.upperFirstChar( ( ( NestedCollection ) localObject1 ).getFieldName() ) ).append( "() { return " ).append( ( ( NestedCollection ) localObject1 ).getFieldName() ).append( "; }" ).toString() );
			paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( "public void set" ).append( StringHelper.upperFirstChar( ( ( NestedCollection ) localObject1 ).getFieldName() ) ).append( "( " ).append( str3 ).append( "< " ).append( ( ( NestedCollection ) localObject1 ).getForeignEntityMappingName() ).append( " > val ) { this." ).append( ( ( NestedCollection ) localObject1 ).getFieldName() ).append( " = val; }" ).toString() );
			paramPrintWriter.println();
			String str4 = getAddMethodPart( ( NestedCollection ) localObject1 );
			if ( paramString != null )
				paramPrintWriter.println();
			paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( "public void add" ).append( StringHelper.upperFirstChar( str4 ) ).append( "( " ).append( ( ( NestedCollection ) localObject1 ).getForeignEntityMappingName() ).append( " " ).append( str4 ).append( "Param  ) {" ).toString() );
			paramPrintWriter.println( new StringBuilder().append( indent( 2 ) ).append( str4 ).append( "Param.set" ).append( StringHelper.upperFirstChar( ( String ) localObject3 ) ).append( "( this );" ).toString() );
			paramPrintWriter.println( new StringBuilder().append( indent( 2 ) ).append( ( ( NestedCollection ) localObject1 ).getFieldName() ).append( ".add( " ).append( str4 ).append( "Param );" ).toString() );
			paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( "}" ).toString() );
			paramPrintWriter.println();
		}
	}

	private void outputSimpleDisplayString( PrintWriter paramPrintWriter )
	{
		paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( "@javax.persistence.Transient" ).toString() );
		paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( "public String getSimpleDisplayString()" ).toString() );
		paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( "{" ).toString() );
		if ( StringHelper.isEmpty( this.entityMapping.getDisplayProperty() ) )
		{
			paramPrintWriter.println( new StringBuilder().append( indent( 2 ) ).append( "return null;" ).toString() );
		}
		else
		{
			FieldMapping localFieldMapping = this.entityMapping.getDisplayFieldMapping();
			paramPrintWriter.println( new StringBuilder().append( indent( 2 ) ).append( "return " ).append( getNullSafeDisplayExpression( localFieldMapping ) ).append( ";" ).toString() );
		}
		paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( "}" ).toString() );
		paramPrintWriter.println();
	}

	private void outputDisplayValue( PrintWriter paramPrintWriter )
	{
		paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( "@javax.persistence.Transient" ).toString() );
		paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( "public String getDisplayValue()" ).toString() );
		paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( "{" ).toString() );
		if ( StringHelper.isEmpty( this.entityMapping.getDisplayString() ) )
		{
			paramPrintWriter.println( new StringBuilder().append( indent( 2 ) ).append( "return null;" ).toString() );
		}
		else
		{
			List localList = SchemaHelper.getDisplayStringFieldMappings( this.entityMapping );
			String str = this.entityMapping.getDisplayString();
			String[] arrayOfString = new String[localList.size()];
			for ( int i = 0; i < localList.size(); ++i )
			{
				FieldMapping localFieldMapping = ( FieldMapping ) localList.get( i );
				str = str.replace( localFieldMapping.getFieldName(), new StringBuilder().append( "%" ).append( i + 1 ).toString() );
				NestedObject localNestedObject = SchemaHelper.getNestedObject( this.allSchemas, this.entityMapping, localFieldMapping );
				if ( localNestedObject != null )
					arrayOfString[i] = new StringBuilder().append( "( " ).append( localNestedObject.getFieldName() ).append( " == null ) ? \"\" : " ).append( localNestedObject.getFieldName() ).append( ".getDisplayValue()" ).toString();
				else
					arrayOfString[i] = getNullSafeDisplayExpression( localFieldMapping );
			}
			paramPrintWriter.println( new StringBuilder().append( indent( 2 ) ).append( "Object[] params = new Object[] {" ).toString() );
			for ( int i = 0; i < arrayOfString.length; ++i )
			{
				paramPrintWriter.print( new StringBuilder().append( indent( 3 ) ).append( arrayOfString[i] ).toString() );
				if ( i < arrayOfString.length - 1 )
					paramPrintWriter.print( "," );
				paramPrintWriter.println();
			}
			paramPrintWriter.println( new StringBuilder().append( indent( 2 ) ).append( "};" ).toString() );
			paramPrintWriter.println();
			paramPrintWriter.println( new StringBuilder().append( indent( 2 ) ).append( "return StringUtil.create( \"" ).append( str ).append( "\", params );" ).toString() );
		}
		paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( "}" ).toString() );
		paramPrintWriter.println();
	}

	private void outputDisplayString( PrintWriter paramPrintWriter, String paramString1, String paramString2 )
	{
		paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( "@javax.persistence.Transient" ).toString() );
		paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( "public String " ).append( paramString2 ).toString() );
		paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( "{" ).toString() );
		if ( StringHelper.isEmpty( paramString1 ) )
		{
			paramPrintWriter.println( new StringBuilder().append( indent( 2 ) ).append( "return super." ).append( paramString2 ).append( ";" ).toString() );
		}
		else
		{
			List localList = SchemaHelper.getDisplayStringFieldMappings( this.entityMapping, paramString1 );
			String str1 = paramString1;
			String[] arrayOfString = new String[localList.size()];
			for ( int i = 0; i < localList.size(); ++i )
			{
				FieldMapping localFieldMapping = ( FieldMapping ) localList.get( i );
				String str2 = new StringBuilder().append( "\"" ).append( this.entityMapping.getEntityName() ).append( "." ).append( ( ( FieldMapping ) localList.get( i ) ).getFieldName() ).append( "\", " ).toString();
				NestedObject localNestedObject = SchemaHelper.getNestedObject( this.allSchemas, this.entityMapping, localFieldMapping );
				if ( localNestedObject != null )
					arrayOfString[i] = new StringBuilder().append( "(( " ).append( localNestedObject.getFieldName() ).append( " == null ) ? \"\" : " ).append( localNestedObject.getFieldName() ).append( "." ).append( paramString2 ).append( ")" ).toString();
				else
					arrayOfString[i] = getNullSafeDisplayExpression( localFieldMapping, str2 );
				str1 = str1.replace( localFieldMapping.getFieldName(), new StringBuilder().append( "\" + " ).append( arrayOfString[i] ).append( " + \"" ).toString() );
			}
			paramPrintWriter.println();
			paramPrintWriter.println( new StringBuilder().append( indent( 2 ) ).append( "return \"" ).append( str1 ).append( "\" ;" ).toString() );
		}
		paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( "}" ).toString() );
		paramPrintWriter.println();
	}

	private void outputAuditDisplayString( PrintWriter paramPrintWriter, String paramString1, String paramString2 )
	{
		paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( "@javax.persistence.Transient" ).toString() );
		paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( "public String " ).append( paramString2 ).toString() );
		paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( "{" ).toString() );
		if ( StringHelper.isEmpty( paramString1 ) )
		{
			paramPrintWriter.println( new StringBuilder().append( indent( 2 ) ).append( "return super." ).append( paramString2 ).append( ";" ).toString() );
		}
		else
		{
			List localList = SchemaHelper.getDisplayStringFieldMappings( this.entityMapping, paramString1 );
			String str = paramString1;
			String[] arrayOfString = new String[localList.size()];
			for ( int i = 0; i < localList.size(); ++i )
			{
				FieldMapping localPropertyMapping = ( FieldMapping ) localList.get( i );
				NestedObject localNestedObject = SchemaHelper.getNestedObject( this.allSchemas, this.entityMapping, localPropertyMapping );
				if ( localNestedObject != null )
					arrayOfString[i] = new StringBuilder().append( "(( " ).append( localNestedObject.getFieldName() ).append( " == null ) ? \"\" : " ).append( localNestedObject.getFieldName() ).append( "." ).append( paramString2 ).append( ")" ).toString();
				else
					arrayOfString[i] = getNullSafeAuditDisplayExpression( localPropertyMapping );
				str = str.replace( localPropertyMapping.getFieldName(), new StringBuilder().append( "\" + " ).append( arrayOfString[i] ).append( " + \"" ).toString() );
			}
			paramPrintWriter.println();
			paramPrintWriter.println( new StringBuilder().append( indent( 2 ) ).append( "return \"" ).append( str ).append( "\" ;" ).toString() );
		}
		paramPrintWriter.println( new StringBuilder().append( indent( 1 ) ).append( "}" ).toString() );
		paramPrintWriter.println();
	}

	private String getNullSafeDisplayExpression( FieldMapping paramFieldMapping )
	{
		return getNullSafeDisplayExpression( paramFieldMapping, "" );
	}

	private String getNullSafeDisplayExpression( FieldMapping paramFieldMapping, String paramString )
	{
		String str1 = new StringBuilder().append( "get" ).append( StringHelper.upperFirstChar( paramFieldMapping.getFieldName() ) ).append( "()" ).toString();
		String str2 = ( paramString.equals( "" ) ) ? "" : "ResourceManager.getI18NString(";
		String str3 = ( paramString.equals( "" ) ) ? "" : ")";
		if ( SchemaHelper.isPrimaryKeyFieldMapping( this.entityMapping.getEntityPrefix(), paramFieldMapping ) )
			return new StringBuilder().append( str2 ).append( paramString ).append( " String.valueOf( " ).append( str1 ).append( " )" ).append( str3 ).toString();
		if ( paramFieldMapping.getDatatype() == ColumnDataType.String )
			return new StringBuilder().append( str2 ).append( paramString ).append( "( " ).append( str1 ).append( " == null ) ? \"\" : " ).append( str1 ).append( str3 ).toString();
		if ( paramFieldMapping.getDatatype() == ColumnDataType.DateTime )
			return new StringBuilder().append( str2 ).append( paramString ).append( "( " ).append( str1 ).append( " == null ) ? \"\" :  " ).append( str1 ).append( ".toString()" ).append( str3 ).toString();
		return new StringBuilder().append( str2 ).append( paramString ).append( "( " ).append( str1 ).append( " == null ) ? \"\" : String.valueOf( " ).append( str1 ).append( " )" ).append( str3 ).toString();
	}

	private String getNullSafeAuditDisplayExpression( FieldMapping paramFieldMapping )
	{
		String str = new StringBuilder().append( "get" ).append( StringHelper.upperFirstChar( paramFieldMapping.getFieldName() ) ).append( "()" ).toString();
		if ( SchemaHelper.isPrimaryKeyFieldMapping( this.entityMapping.getEntityPrefix(), paramFieldMapping ) )
			return str;
		if ( paramFieldMapping.getDatatype() == ColumnDataType.String )
			return new StringBuilder().append( "(" ).append( str ).append( " == null ? \"\" : " ).append( str ).append( ")" ).toString();
		if ( paramFieldMapping.getDatatype() == ColumnDataType.DateTime )
			return new StringBuilder().append( "(" ).append( str ).append( " == null  ? \"\" :  " ).append( str ).append( ".toString())" ).toString();
		return new StringBuilder().append( "(" ).append( str ).append( " == null ? \"\" : String.valueOf( " ).append( str ).append( " ))" ).toString();
	}

	private String getNestedCollectionOrderColumn( NestedCollection paramNestedCollection )
	{
		EntityMapping localEntityMapping = SchemaHelper.findEntityMapping( this.allSchemas, paramNestedCollection.getForeignEntityMappingName() );
		Iterator localIterator = localEntityMapping.getFieldMappings().iterator();
		while ( localIterator.hasNext() )
		{
			FieldMapping localFieldMapping = ( FieldMapping ) localIterator.next();
			String str = StringHelper.lowerFirstChar( localFieldMapping.getFieldName() );
			if ( str.equals( new StringBuilder().append( localEntityMapping.getEntityPrefix() ).append( "OrderNo" ).toString() ) )
				return localFieldMapping.getColumn().getColumnName();
		}
		return null;
	}

	private String getJavaDefaultValue( ColumnDataType paramColumnDataType, boolean paramBoolean ) throws GenerateException
	{
		paramBoolean = true;
		switch( paramColumnDataType )
		{
		  case Bool:
	            return "false";

	        case DateTime:
	            return "null";

	        case Int:
	        	 if (paramBoolean )
		                return "0";
		            else
		                return "null";
	        case Long:
	            if (paramBoolean )
	                return "0L";
	            else
	                return "null";

	        case Text:
	        case String:
	            return "\"\"";

	        case Decimal:
	            if (paramBoolean )
	                return "BigDecimal.ZERO";
	            else
	                return "null";

	        default:
		}
		throw new GenerateException( "Unknown ColumnDataType : %1", new Object[]
		{ "getJavaDefaultValue()" } );
	}

	public static String getHibernateType( ColumnDataType paramColumnDataType ) throws GenerateException
	{
		switch( paramColumnDataType.ordinal() )
		{
		case 0:
			return "long";
		case 1:
			return "int";
		case 2:
			return "org.hibernate.type.StringType";
		case 3:
			return "org.jadira.usertype.dateandtime.joda.PersistentDateTime";
		case 4:
			return "yes_no";
		case 5:
			return "com.asjngroup.ncash.database.hibernate.type.DecimalType";
		case 6:
			return "text";
		case 7:
		
		}
		throw new GenerateException( "Unknown ColumnDataType : %1", new Object[]
		{ "getHibernateType()" } );
	}
}