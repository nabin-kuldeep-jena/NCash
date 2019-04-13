package com.asjngroup.ncash.common.database.datasource;

import java.util.Comparator;

public class NamedParameterComparator implements Comparator
{
    public int compare( Object o, Object o1 )
    {
        // cast out the parameters names
        String param1 = (String)o;
        String param2 = (String)o1;

        // if the same length, return the normal comparison result
        if ( param1.length() == param2.length() )
            return param1.compareTo( param2 );

        // return "less than" if the first parameter is longer than the second
        // NOTE: this ensure the sorted list will be in descending param length order
        return (param1.length() > param2.length() ? -1 : 1);
    }
}
