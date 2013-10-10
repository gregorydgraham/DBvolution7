package nz.co.gregs.dbvolution.internal;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nz.co.gregs.dbvolution.DBRuntimeException;

/**
 * Low-level internal utility for finding properties within classes.
 * @author Malcolm Lett
 */
// Note: java.beans.Introspector
class JavaPropertyFinder {
	public static enum PropertyType { FIELD, BEAN_PROPERTY };
	public static enum Visibility { PUBLIC, PROTECTED, DEFAULT, PRIVATE };
	
	private Set<PropertyType> propertyTypes = EnumSet.allOf(PropertyType.class);
	private Visibility fieldVisibility = Visibility.PUBLIC;
	private Visibility methodVisibility = Visibility.PUBLIC;
	private JavaPropertyFilter filter = JavaPropertyFilter.ANY_PROPERTY_FILTER;
	
	/**
	 * New default instance with default search characteristics.
	 */
	public JavaPropertyFinder() {
	}
	
	/**
	 * 
	 * @param fieldVisibilityLevel the most private level of field that should be retrieved
	 * @param methodVisibility the most private level of method that should be retrieved
	 * @param propertyTypes
	 */
	public JavaPropertyFinder(Visibility fieldVisibility, Visibility methodVisibility,
			JavaPropertyFilter filter, PropertyType... propertyTypes) {
		this.fieldVisibility = fieldVisibility;
		this.methodVisibility = methodVisibility;
		this.filter = filter;
		
		this.propertyTypes = EnumSet.noneOf(PropertyType.class);
		for (PropertyType propertyType: propertyTypes) {
			this.propertyTypes.add(propertyType);
		}		
	}
	
	/**
	 * Gets all properties according to configured criteria.
	 * @param clazz the type to inspect
	 * @return the non-null list of properties found on the given class
	 */
	public List<JavaProperty> getProperties(Class<?> clazz) {
		List<JavaProperty> properties = new ArrayList<JavaProperty>();
		
		// retrieve fields
		if (propertyTypes.contains(PropertyType.FIELD)) {
			properties.addAll(getFields(clazz));			
		}
		
		// retrieve bean-properties
		if (propertyTypes.contains(PropertyType.BEAN_PROPERTY)) {
			properties.addAll(getBeanProperties(clazz));
		}
		
		return properties;
	}
	
	/**
	 * Gets the field-based properties.
	 * @param clazz
	 * @return
	 */
	// TODO: this may not be able to handle inheritance of protected/default fields
	private List<JavaProperty> getFields(Class<?> clazz) {
		List<JavaProperty> properties = new ArrayList<JavaProperty>();
		
		Set<String> observedFieldNames = new HashSet<String>();
		
		// get all public fields
		// (these are inherited, so need to use the proper inheritance-aware method)
		for (Field field: clazz.getFields()) {
			if (filter.acceptField(field)) {
				properties.add(new JavaProperty.JavaField(field));
			}
			observedFieldNames.add(field.getName());
		}
		
		// get all non-public fields
		// (getDeclaredFields() isn't inheritance aware,
		//  so we're probably not going to be inherited protected/default fields this way)
		if (fieldVisibility.ordinal() > Visibility.PUBLIC.ordinal()) {
			for (Field field: clazz.getDeclaredFields()) {
				if (!observedFieldNames.contains(field.getName())) {
					// skip standard java fields
					if (field.getName().equals("serialVersionUID")) {
						continue;
					}
					
					// add field if accepted
					// (plus set accessible)
					if (filter.acceptField(field)) {
						// make accessible
						// TODO: pretty sure there's exception types that need to be caught on this call
						field.setAccessible(true);
						
						properties.add(new JavaProperty.JavaField(field));
					}
				}
			}
		}
		
		return properties;
	}

	/**
	 * Gets the bean-property-based properties.
	 * @param clazz
	 * @return
	 */
	private List<JavaProperty> getBeanProperties(Class<?> clazz) {
		List<JavaProperty> properties = new ArrayList<JavaProperty>();
		
		// get all public bean-properties
		try {
			BeanInfo beanInfo = Introspector.getBeanInfo(clazz);
			for (PropertyDescriptor descriptor: beanInfo.getPropertyDescriptors()) {
				Method getter = descriptor.getReadMethod();
				Method setter = descriptor.getWriteMethod();
				
				// skip standard java fields
				if (descriptor.getName().equals("class")) {
					continue;
				}
				
				// add field if accepted
				if (filter.acceptBeanProperty(getter, setter)) {
					properties.add(new JavaProperty.JavaBeanProperty(descriptor));
				}
			}
		} catch (IntrospectionException e) {
			// TODO: handle this properly
			throw new DBRuntimeException("Error inspecting "+clazz.getName()+": "+e.getMessage(), e);
		}
		
		// get all non-public bean-properties
		if (methodVisibility.ordinal() > Visibility.PUBLIC.ordinal()) {
			throw new UnsupportedOperationException("Using non-public property accessors is not supported");
		}
		
		return properties;
	}
}
