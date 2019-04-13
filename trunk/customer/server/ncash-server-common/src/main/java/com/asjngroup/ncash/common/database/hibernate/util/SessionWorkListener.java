/**
 * 
 */
package com.asjngroup.ncash.common.database.hibernate.util;

import org.hibernate.HibernateException;
import org.hibernate.Session;

import com.asjngroup.ncash.common.database.hibernate.exception.NCashDataAccessException;

/**
 * @author nabin.jena
 *
 */
public interface SessionWorkListener
{

	Object doSessionWork( Session session ) throws HibernateException, NCashDataAccessException;

}
