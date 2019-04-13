
package com.asjngroup.ncash.installer.tasks.schema;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.asjngroup.ncash.common.database.datasource.DataSource;
import com.asjngroup.ncash.common.database.datasource.DataSourceException;
import com.asjngroup.ncash.common.database.datasource.DataSourceHelper;
import com.asjngroup.ncash.common.database.datasource.MySqlDataSource;
import com.asjngroup.ncash.common.database.datasource.SyncDatabaseListener;
import com.asjngroup.ncash.common.database.hibernate.exception.HibernateUtilException;
import com.asjngroup.ncash.common.database.hibernate.util.HibernateSession;
import com.asjngroup.ncash.common.database.schema.EntityMapping;
import com.asjngroup.ncash.common.database.schema.Index;
import com.asjngroup.ncash.common.database.schema.Schema;
import com.asjngroup.ncash.common.database.schema.SchemaHelper;
import com.asjngroup.ncash.common.database.schema.SchemaParseException;
import com.asjngroup.ncash.common.database.schema.Table;
import com.asjngroup.ncash.common.installer.util.InstallerSessionBuilder;
import com.asjngroup.ncash.installer.tasks.InstallerData;
import com.asjngroup.ncash.installer.tasks.InstallerTask;
import com.asjngroup.ncash.installer.tasks.InstallerTaskException;

public class UpdateSchemaTask implements InstallerTask
{
	private static Log log = LogFactory.getLog( UpdateSchemaTask.class );

	private boolean isSilentInstallation = false;

	private InstallerData installerData;

	private DataSource dataSource;

	public UpdateSchemaTask( InstallerData installerData, DataSource dataSource )
	{
		this.installerData = installerData;
		this.dataSource = dataSource;
	}

	public void execute() throws SchemaTaskException
	{
		log.info( "Updating schema" );
		try
		{
			List<String> tableCreated = new ArrayList<String>();
			// Now we can sychronise each data source with the database
			Schema schema = SchemaHelper.buildSchema( installerData.getData( "schemaPath" ).toString() );
			dataSource.attachSchema( schema );
			syncDatabaseToSchema( schema, dataSource, tableCreated );
		}
		catch ( SchemaParseException e )
		{
			throw new SchemaTaskException( e );
		}
		finally
		{
			dataSource.closeAll();
		}
	}

	private void syncDatabaseToSchema( final Schema schema, final DataSource dataSource, final List<String> tableCreated ) throws SchemaTaskException
	{
		try
		{
			// sync to the database, with callback for confirming table drops
			DataSourceHelper.syncDatabaseToSchema( schema, dataSource, "", "", new SyncDatabaseListener()
			{
				public boolean createTable( Table table )
				{
					EntityMapping objectMapping = schema.findEntityMappingFromTableName( table.database.DatabaseName, table.TableName );

					if ( objectMapping != null )
					{
						tableCreated.add( objectMapping.EntityName );
					}

					return true;
				}

				public boolean updateTable( Table fromTable, Table toTable )
				{
					// Always update tables (even if they are in the ignore list)
					return true;
				}

				public boolean updateIndex( Index fromIndex, Index toIndex )
				{
					return true;
				}

				public boolean dropTable( Table table )
				{
					// Never drop tables in the installer
					return false;
				}
			} );
			dataSource.updateAllNextNumber();
		}
		catch ( DataSourceException e )
		{
			throw new SchemaTaskException( e );
		}
	}

	public void setData( String taskName, InstallerData installerData )
	{

	}

	public void initialise() throws InstallerTaskException
	{

	}

	public boolean isSilentInstallation()
	{
		return isSilentInstallation;
	}

	public void setSilentInstallation( boolean isSilent )
	{
		this.isSilentInstallation = isSilent;

	}

	public void postInstallationTaskInitialise() throws InstallerTaskException
	{

	}

	public void setData( InstallerData istallerData )
	{
		// TODO Auto-generated method stub

	}

	public static void start()
	{
		InstallerData installerData = new InstallerData();
		installerData.setData( "schemaPath", "/com/asjngroup/ncash/common/database/metadata/ncash_customer_server_schema.xml" );
		try
		{
		/*	File file= new File(UpdateSchemaTask.class.getResource("/hibernate.properties").toURI());
			Properties hikariProperties=new Properties();
			hikariProperties.load( new InputStreamReader( new FileInputStream( file ) ) );
			HikariConfig hikariConfig = new HikariConfig( hikariProperties );
			javax.sql.DataSource dataSource = new HikariDataSource( hikariConfig );
			LocalSessionFactoryBean sessionFactory=new NCashSessionFactoryBean();
			sessionFactory.setDataSource( dataSource );
			sessionFactory.setAnnotatedPackages( "com.asjngroup.ncash.common.database.hibernate" );
			HibernateInfo hibernateInfo=new HibernateInfo( (SessionFactory)sessionFactory, dataSource, new MysqlIdGenerator() );
			HibernateSession.initialise(hibernateInfo );*/
			InstallerSessionBuilder.load();
			new UpdateSchemaTask( installerData, new MySqlDataSource( HibernateSession.getDataSource() ) ).execute();
		}
		catch ( SchemaTaskException | SQLException e )
		{
			e.printStackTrace();
		}
	}
};