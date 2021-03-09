
package app.model.business;

import java.util.Date;

import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Базовый класс-элемент для различных классов-элементов списков в программе
 * @author Igor Makarevich
 * @version 1.00.00.001   31.10.2017
 */
public class SimpleItem {
	protected final LongProperty id;
	protected final StringProperty name;
	protected final StringProperty descr;
	
	protected final ObjectProperty<Date> dateCreated;
	protected final ObjectProperty<Date> dateModified;
	protected final StringProperty userCreated;
	protected final StringProperty userModified;
	
	/**
	 * Конструктор по умолчанию.
	 */
	public SimpleItem() {
		this(0, null, null);
	}
	
	/**
	 * Конструктор с основными данными
	 * @param
	 */
	public SimpleItem(long id, String name, String descr) {
		this.id               = new SimpleLongProperty(id);
		this.name             = new SimpleStringProperty(name);
		this.descr            = new SimpleStringProperty(descr);
		this.dateCreated      = null;
		this.dateModified     = null;
		this.userCreated      = null;
		this.userModified     = null;
	}
	
	/**
	 * Конструктор со всеми данными
	 * @param
	 */
	public SimpleItem(long id, String name, String descr, 
					  Date dateCreated, Date dateModified, String userCreated, String userModified) {
		this.id               = new SimpleLongProperty(id);
		this.name             = new SimpleStringProperty(name);
		this.descr            = new SimpleStringProperty(descr);
		this.dateCreated      = new SimpleObjectProperty<Date>(dateCreated);
		this.dateModified     = new SimpleObjectProperty<Date>(dateModified);
		this.userCreated      = new SimpleStringProperty(userCreated);
		this.userModified     = new SimpleStringProperty(userModified);
	}
	
	/**
	 * Конструктор
	 * @param
	 */
	public SimpleItem(SimpleItem item) {
		this.id           = new SimpleLongProperty(item.getId());
		this.name         = new SimpleStringProperty(item.getName());
		this.descr        = new SimpleStringProperty(item.getDescr());
		this.dateCreated  = new SimpleObjectProperty<Date>(item.getDateCreated());
		this.dateModified = new SimpleObjectProperty<Date>(item.getDateModified());
		this.userCreated  = new SimpleStringProperty(item.getUserCreated());
		this.userModified = new SimpleStringProperty(item.getUserModified());
	}
	
	// id  -- g,s,p
	public long getId() {
	    return id.get();
	}
	public void setId(long id) {
	    this.id.set(id);
	}
	public LongProperty idProperty() {
	    return id;
	}
	
	// name  -- g,s,p
	public String getName() {
		return name.get();
	}
	public void setName(String name) {
		this.name.set(name);
	}
	public StringProperty nameProperty() {
			return name;
	}
	
	// descr -- g,s,p 
	public String getDescr() {
		return descr.get();
	}
	public void setDescr(String descr) {
		this.descr.set(descr);
	}
	public StringProperty descrProperty() {
		return descr;
	}

	// dateCreated -- g,s,p
    public Date getDateCreated() {
    	if (dateCreated != null) return dateCreated.get();
    	else                     return null;
    }
    public void setDateCreated(Date dateCreated) {
        this.dateCreated.set(dateCreated);
    }
    public ObjectProperty<Date> dateCreatedProperty() {
        return dateCreated;
    }
    
    // dateModified -- g,s,p
    public Date getDateModified() {
    	if (dateModified != null) return dateModified.get();
    	else                      return null;
    }
    public void setDateModified(Date dateModified) {
        this.dateModified.set(dateModified);
    }
    public ObjectProperty<Date> dateModifiedProperty() {
        return dateModified;
    }

    // userCreated -- g,s,p 
 	public String getUserCreated() {
 		if (userCreated != null) return userCreated.get();
    	else                     return null;
 	}
 	public void setUserCreated(String userCreated) {
 	    this.userCreated.set(userCreated);
 	}
 	public StringProperty userCreatedProperty() {
 	    return userCreated;
 	}
 	
 	// userModified -- g,s,p 
  	public String getUserModified() {
  		if (userModified != null) return userModified.get();
    	else                      return null;
  	}
  	public void setUserModified(String userModified) {
  	    this.userModified.set(userModified);
  	}
  	public StringProperty userModifiedProperty() {
  	    return userModified;
  	}
}
