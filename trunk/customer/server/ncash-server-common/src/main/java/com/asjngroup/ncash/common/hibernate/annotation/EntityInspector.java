package com.asjngroup.ncash.common.hibernate.annotation;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.hibernate.SessionFactory;
import org.hibernate.metadata.ClassMetadata;

import com.asjngroup.ncash.common.exception.NCashRuntimeException;
import com.asjngroup.ncash.common.util.StringHelper;
import com.asjngroup.ncash.common.util.StringUtil;


/**
 * User: mike.quilleash Date: 12-Oct-2006 Time: 17:53:42
 */
public class EntityInspector<T> implements Serializable
{
	private static final long serialVersionUID = 1L;
	private final SessionFactory sessionFactory;
	private final Class<T> entityClass;
	private final ClassMetadata classMetadata;
	private boolean areShortDisplayPropertiesOfTypeString;

	public EntityInspector(SessionFactory sessionFactory, Class<T> entityClass) {
		this.sessionFactory = sessionFactory;
		this.entityClass = entityClass;
		this.classMetadata = sessionFactory.getClassMetadata(entityClass);
	}

	public Class<T> getEntityClass() {
		return entityClass;
	}

	public ClassMetadata getClassMetadata() {
		return classMetadata;
	}

	public boolean isMandatory(String propertyName) {
		Column column = findColumnOfProperty(propertyName);

		if (column != null)
			return !column.nullable();

		JoinColumn joinColumn = findJoinColumnOfProperty(propertyName);

		if (joinColumn == null)
			throw new NCashRuntimeException(
					"Could not find column or join for property %1",
					propertyName);

		return !joinColumn.nullable();
	}

	public Map<String, String[]> getUniqueConstraintPropertySet() {
		Map<String, String[]> uniquePropertySets = new HashMap<String, String[]>();

		for (Index index : entityClass.getAnnotation(Table.class).indexes()) {
			// only interested in unique indexes
			if (!index.unique())
				continue;

			// TODO: cheesy
			if (index.name().endsWith("_pk"))
				continue;

			String[] propertyNames = new String[index.columnNames().length];

			for (int i = 0; i < index.columnNames().length; i++) {
				String columnName = index.columnNames()[i];

				// see if this column is a join to another table
				Method method = findMethodWithJoinColumn(columnName);

				// if not a join then get the normal method accessor
				if (method == null)
					method = findMethodWithColumn(columnName);

				// sanity check
				if (method == null)
					throw new NCashRuntimeException(
							"Could not find a method for column %1 in index %2 on table %3.",
							columnName, index.name(), entityClass.getName());

				// only interested in 'get' methods
				if (method.getName().startsWith("get")) {
					propertyNames[i] = StringHelper.lowerFirstChar(method
							.getName().substring(3));
				}
			}

			uniquePropertySets.put(index.name(), propertyNames);
		}

		return uniquePropertySets;
	}

	public String getShortDisplay(T obj) {
		String shortDisplayProperty = getShortDisplayProperty();

		if (shortDisplayProperty == null)
			return null;

		return (String) getPropertyValue(obj, shortDisplayProperty);
	}

	public String getShortDisplayProperty() {
		Index displayIndex = getDisplayIndex();

		if (displayIndex == null)
			return null;

		String nonStringPropertyName = null;
		for (String propertyName : findIndexProperties(displayIndex)) {
			if (getPropertyClass(propertyName).equals(String.class)) {
				areShortDisplayPropertiesOfTypeString = true;
				return propertyName;
			}
			nonStringPropertyName = propertyName;
		}

		areShortDisplayPropertiesOfTypeString = false;
		return entityClass.getSimpleName() + "_" + nonStringPropertyName;
	}

	public String getSimpleDisplayProperty() {
		Index displayIndex = getDisplayIndex();

		if (displayIndex == null)
			return null;

		if (displayIndex.columnNames().length > 1)
			return null;

		return findBasicPropertyByColumn(displayIndex.columnNames()[0]);
	}

	public String getSimpleDisplay(T obj) {
		Index displayIndex = getDisplayIndex();

		if (displayIndex == null)
			return null;

		if (displayIndex.columnNames().length > 1)
			return null;

		String propertyName = findBasicPropertyByColumn(displayIndex
				.columnNames()[0]);

		if (!getPropertyClass(propertyName).equals(String.class)) {
			areShortDisplayPropertiesOfTypeString = false;
			return null;
		}

		areShortDisplayPropertiesOfTypeString = true;
		return (String) getPropertyValue(obj, propertyName);
	}

	public boolean hasSimpleDisplay() {
		Index displayIndex = getDisplayIndex();

		if (displayIndex == null)
			return false;

		if (displayIndex.columnNames().length > 1)
			return false;

		String propertyName = findBasicPropertyByColumn(displayIndex
				.columnNames()[0]);

		return getPropertyClass(propertyName).equals(String.class);
	}

	public Class<?> getPropertyClass(String propertyName) {
		try {
			return entityClass.getDeclaredMethod(
					"get" + StringHelper.upperFirstChar(propertyName))
					.getReturnType();
		} catch (NoSuchMethodException e) {
			throw new NCashRuntimeException(e);
		}
	}

	public Object getPropertyValue(Object object, String propertyName) {
		try {
			if (areShortDisplayPropertiesOfTypeString)
				return entityClass.getDeclaredMethod(
						"get" + StringHelper.upperFirstChar(propertyName))
						.invoke(object);
				return entityClass.getDeclaredMethod("getDisplayString")
						.invoke(object);
		} catch (NoSuchMethodException e) {
			throw new NCashRuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new NCashRuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new NCashRuntimeException(e);
		}

	}

	public Index getDisplayIndex() {
		Table table = entityClass.getAnnotation(Table.class);

		for (Index index : table.indexes()) {
			if (index.displayName())
				return index;
		}

		return null;
	}

	public boolean hasBusinessConstraint() {
		return getBusinessConstraintIndex() != null;
	}

	public Index getBusinessConstraintIndex() {
		Table table = entityClass.getAnnotation(Table.class);

		for (Index index : table.indexes()) {
			if (index.businessConstraint())
				return index;
		}

		return null;
	}

	public Method findMethodWithColumn(String columnName) {
		for (Method method : entityClass.getMethods()) {
			Column column = method.getAnnotation(Column.class);

			if (column == null)
				continue;

			if (column.name().equals(columnName)) {
				return method;
			}
		}

		return null;
	}

	private Method findMethodWithJoinColumn(String columnName) {
		for (Method method : entityClass.getMethods()) {
			JoinColumn column = method.getAnnotation(JoinColumn.class);

			if (column == null)
				continue;

			if (column.name().equals(columnName)) {
				return method;
			}
		}

		return null;
	}

	private Class<?> findManyToOneClassByColumn(String columnName) {
		Method method = findMethodWithJoinColumn(columnName);

		if (method == null)
			return null;

		if (method.getAnnotation(ManyToOne.class) != null) {
			if (method.getName().startsWith("get")) {
				return method.getReturnType();
			}
		}

		return null;
	}

	private String findManyToOnePropertyByColumn(String columnName) {
		Method method = findMethodWithJoinColumn(columnName);

		if (method == null)
			return null;

		if (method.getAnnotation(ManyToOne.class) != null) {
			if (method.getName().startsWith("get")) {
				return StringHelper.lowerFirstChar(method.getName()
						.substring(3));
			}
		}

		return null;
	}

	private String findBasicPropertyByColumn(String columnName) {
		Method method = findMethodWithColumn(columnName);

		if (method == null)
			return null;

		boolean isId = (method.getAnnotation(Id.class) != null);
		boolean isBasic = (method.getAnnotation(Basic.class) != null);

		if (isId || isBasic) {
			if (method.getName().startsWith("get")) {
				return StringHelper.lowerFirstChar(method.getName()
						.substring(3));
			}
		}

		return null;
	}

	public String findColumnNameOfProperty(String propertyName) {
		Method method = findMethodOfProperty(propertyName);

		Column column = method.getAnnotation(Column.class);

		String columnName = null;

		if (column != null)
			columnName = column.name();

		if (columnName == null) {
			JoinColumn joinColumn = method.getAnnotation(JoinColumn.class);

			if (joinColumn != null)
				columnName = joinColumn.name();
		}

		return columnName;
	}

	private Column findColumnOfProperty(String propertyName) {
		Method method = findMethodOfProperty(propertyName);

		if (method == null)
			return null;

		return method.getAnnotation(Column.class);
	}

	public JoinColumn findJoinColumnOfProperty(String propertyName) {
		Method method = findMethodOfProperty(propertyName);

		if (method == null)
			return null;

		return method.getAnnotation(JoinColumn.class);
	}

	private Method findMethodOfProperty(String propertyName) {
		try {
			return entityClass.getMethod("get"
					+ StringHelper.upperFirstChar(propertyName));
		} catch (NoSuchMethodException e) {
			return null;
		}
	}

	public String findBasicPropertyMatchingManyToOneProperty(
			String manyToOnePropery) {
		String columnName = findColumnNameOfProperty(manyToOnePropery);

		if (columnName == null)
			throw new NCashRuntimeException(
					"Property '%1' does not have a column specified",
					manyToOnePropery);

		return findBasicPropertyByColumn(columnName);
	}

	public Class<?> getAssociationPropertyClass(String propertyName) {
		String columnName = findColumnNameOfProperty(propertyName);

		Class<?> associationClass = findManyToOneClassByColumn(columnName);

		return associationClass;
	}

	public boolean isAssociationProperty(String propertyName) {
		return getAssociationPropertyClass(propertyName) != null;
	}

	public boolean isBasicProperty(String basicProperty) {
		Method method = findMethodOfProperty(basicProperty);

		if (method == null)
			throw new NCashRuntimeException("Invalid property '%1'",
					basicProperty);

		return method.isAnnotationPresent(Basic.class);
	}

	public boolean isOneToOneProperty(String propertyName) {
		Method method = findMethodOfProperty(propertyName);

		if (method == null)
			throw new NCashRuntimeException("Invalid property '%1'",
					propertyName);

		return method.isAnnotationPresent(OneToOne.class);
	}

	public String findManyToOnePropertyMatchingBasicProperty(
			String basicProperty) {
		Column column = findColumnOfProperty(basicProperty);

		if (column == null)
			throw new NCashRuntimeException(
					"Property '%1' does not have a column specified",
					basicProperty);

		return findManyToOnePropertyByColumn(column.name());
	}

	public String[] getBusinessContraintProperties() {
		Index index = getBusinessConstraintIndex();

		if (index == null)
			return null;

		return findIndexProperties(index);
	}

	public String[] findIndexProperties(Index index) {
		List<String> indexProperties = new ArrayList<String>();

		for (String columnName : index.columnNames()) {
			// first try a many to one property
			String propertyName = findManyToOnePropertyByColumn(columnName);

			if (propertyName != null) {
				indexProperties.add(propertyName);
				continue;
			}

			// otherwise a basic property
			propertyName = findBasicPropertyByColumn(columnName);

			if (propertyName == null)
				throw new NCashRuntimeException(
						"Could not find property for column %1 in display index %2",
						columnName, index.name());

			indexProperties.add(propertyName);
		}

		return indexProperties.toArray(new String[0]);
	}

	public String[] getDisplayPropertiesFlattened() {
		return getIndexPropertiesFlattened(true);
	}

	public String[] getBusinessConstraintPropertiesFlattened() {
		return getIndexPropertiesFlattened(false);
	}

	@SuppressWarnings("unchecked")
	private String[] getIndexPropertiesFlattened(boolean displayIndex) {
		List<String> displayProperties = new ArrayList<String>();

		Index index = displayIndex ? getDisplayIndex()
				: getBusinessConstraintIndex();

		if (index == null)
			return null;

		for (String columnName : index.columnNames()) {
			Class<?> manyToOneClass = findManyToOneClassByColumn(columnName);

			if (manyToOneClass != null) {
				String manyToOneProperty = findManyToOnePropertyByColumn(columnName);

				if (manyToOneClass.equals(this.entityClass)) {
					displayProperties.add(manyToOneProperty);
					continue;
				}

				String[] subDisplayProperties = new EntityInspector(
						sessionFactory, manyToOneClass)
						.getIndexPropertiesFlattened(displayIndex);

				if (subDisplayProperties == null)
					return null;

				for (String subDisplayProperty : subDisplayProperties)
					displayProperties.add(manyToOneProperty + "."
							+ subDisplayProperty);
			} else {
				String propertyName = findBasicPropertyByColumn(columnName);

				if (propertyName == null)
					throw new NCashRuntimeException(
							"Could not find property for column %1 in display index %2",
							columnName, index.name());

				displayProperties.add(propertyName);
			}
		}

		return displayProperties.toArray(new String[0]);
	}

	@SuppressWarnings("unchecked")
	public String getReverseBidirectionalProperty(String propertyName) {
		Method method = findMethodOfProperty(propertyName);

		if (method == null)
			return null;

		if (method.isAnnotationPresent(OneToMany.class)) {
			OneToMany oneToMany = method.getAnnotation(OneToMany.class);

			if (oneToMany.mappedBy().length() > 0)
				return oneToMany.mappedBy();

			return null;
		}

		if (method.isAnnotationPresent(ManyToOne.class)) {
			EntityInspector entityInspector = new EntityInspector(
					sessionFactory, method.getReturnType());
			return entityInspector.findOneToManyMappedBy(propertyName);
		}

		return null;
	}

	public String findOneToManyMappedBy(String mappedBy) {
		for (Method method : entityClass.getMethods()) {
			OneToMany oneToMany = method.getAnnotation(OneToMany.class);

			if (oneToMany == null)
				continue;

			if (oneToMany.mappedBy().equals(mappedBy))
				return methodNameToPropertyName(method.getName());
		}

		return null;
	}

	public String methodNameToPropertyName(String methodName) {
		return StringHelper.lowerFirstChar(StringHelper.removeLeft(methodName,
				3));
	}

	public String getOrderByProperty(String collectionProperty) {
		org.hibernate.annotations.OrderBy orderBy = null;
		Method method = findMethodOfProperty(collectionProperty);
		if (method != null)
			orderBy = method.getAnnotation(org.hibernate.annotations.OrderBy.class);

		if (orderBy == null)
			return null;

		return StringHelper.lowerFirstChar(StringHelper
				.underScoreToCamelCase(orderBy.clause()));
	}

	public List<OneToMany> findOneToManys() {
		List<OneToMany> oneToManys = new ArrayList<OneToMany>();

		for (Method method : entityClass.getMethods()) {
			OneToMany oneToMany = method.getAnnotation(OneToMany.class);

			if (oneToMany != null)
				oneToManys.add(oneToMany);
		}

		return oneToManys;
	}

	public List<ManyToOne> findManyToOnes() {
		List<ManyToOne> manyToOnes = new ArrayList<ManyToOne>();

		for (Method method : entityClass.getMethods()) {
			ManyToOne manyToOne = method.getAnnotation(ManyToOne.class);

			if (manyToOne != null)
				manyToOnes.add(manyToOne);
		}

		return manyToOnes;
	}

	public boolean isAreShortDisplayPropertiesOfTypeString() {
		return areShortDisplayPropertiesOfTypeString;
	}

	public void setAreShortDisplayPropertiesOfTypeString(
			boolean areShortDisplayPropertiesOfTypeString) {
		this.areShortDisplayPropertiesOfTypeString = areShortDisplayPropertiesOfTypeString;
	}
}
