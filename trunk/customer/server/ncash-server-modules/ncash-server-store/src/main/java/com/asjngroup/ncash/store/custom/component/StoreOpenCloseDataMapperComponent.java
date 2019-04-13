package com.asjngroup.ncash.store.custom.component;

import org.hibernate.metadata.ClassMetadata;
import org.joda.time.DateTime;

import com.asjngroup.ncash.common.database.hibernate.references.StoreBranch;
import com.asjngroup.ncash.framework.generic.custom.component.CustomizeDataMapper;
import com.asjngroup.ncash.framework.generic.models.EntityModel;

public class StoreOpenCloseDataMapperComponent implements CustomizeDataMapper
{

	public void mapCutomeData( Object obj, ClassMetadata metadata, EntityModel model )
	{
		if ( obj instanceof StoreBranch )
		{
			Integer stbOpeningHours = ( Integer ) metadata.getPropertyValue( obj, "stbOpeningHours" );

			String stbOpenHrFormat = null;
			if ( stbOpeningHours >= 0 && stbOpeningHours < 100 )
				stbOpenHrFormat = "12 :" + stbOpeningHours.toString().substring( stbOpeningHours.toString().length() - 2 );
			else if ( stbOpeningHours <= 1259 )
				stbOpenHrFormat = stbOpeningHours.toString().substring( 0, stbOpeningHours.toString().length() - 2 ) + ":" + stbOpeningHours.toString().substring( stbOpeningHours.toString().length() - 2 );
			else
				stbOpenHrFormat = Integer.valueOf( stbOpeningHours.toString().substring( 0, stbOpeningHours.toString().length() - 2 ) ) - 12 + ":" + stbOpeningHours.toString().substring( stbOpeningHours.toString().length() - 2 );

			Integer stbClosingHours = ( Integer ) metadata.getPropertyValue( obj, "stbClosingHours" );
			String stbClosingHrFormat = null;
			if ( stbClosingHours >= 0 && stbClosingHours < 100 )
				stbClosingHrFormat = "12:" + stbClosingHours.toString().substring( stbClosingHours.toString().length() - 2 );
			else if ( stbClosingHours <= 1159 )
				stbClosingHrFormat = stbClosingHours.toString().substring( 0, stbClosingHours.toString().length() - 2 ) + ":" + stbClosingHours.toString().substring( stbClosingHours.toString().length() - 2 );
			else
				stbClosingHrFormat = Integer.valueOf( stbClosingHours.toString().substring( 0, stbClosingHours.toString().length() - 2 ) )-12 + ":" + stbClosingHours.toString().substring( stbClosingHours.toString().length() - 2 );

			if ( stbOpeningHours >= 0 && stbOpeningHours < 1159 )
				model.addProperty( "stbOpeningHours", stbOpenHrFormat + " AM" );
			else if ( stbOpeningHours > 1159 && stbOpeningHours < 2359 )
				model.addProperty( "stbOpeningHours", stbOpenHrFormat + " PM" );

			if ( stbClosingHours >= 0 && stbClosingHours < 1159 )
				model.addProperty( "stbClosingHours", stbClosingHrFormat + " AM" );
			else if ( stbClosingHours > 1159 && stbClosingHours <= 2359 )
				model.addProperty( "stbClosingHours", stbClosingHrFormat + " PM" );

			DateTime now = new DateTime();
			int hr = now.getHourOfDay();
			int min = now.getMinuteOfHour();
			int curTime = Integer.parseInt( hr + "" + min );
			model.addProperty( "isStoreOpenNow", ( stbOpeningHours <= curTime && stbClosingHours >= curTime ) );
		}
	}
}
