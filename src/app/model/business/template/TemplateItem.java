
package app.model.business.template;

import java.util.Date;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Класс содержит информацию об одном шаблоне документа (его инфо блока).
 * @author Игорь Макаревич
 * @version 2.00.00.001   11.04.2021
 */
public class TemplateItem extends TemplateSimpleItem {
	/**
	 * Id родительской директории шаблона или 0, если шаблон (дирктория) находится в корне
	 */
	private final LongProperty parentId;
	/**
	 * 0-шаблон, 1-директория ; (для зарезервированных) 10 - шаблон, 11 - директория
	 */
	private final IntegerProperty type;
	/**
	 * исходный код шаблона 
	 */
	private final StringProperty body;
	
	/**
	 * Конструктор по умолчанию.
	 */
	public TemplateItem() {
		this(0, 0, 0, null, null, null);
	}
	
	/**
	 * Конструктор с основными данными
	 * @param
	 */
	public TemplateItem(long id, long parentId, int type, String name, String descr, String body) {
		super(id, name, descr, 0, 
				(((type == 0)||(type == 10)) ? TYPE_ITEM_TEMPLATE : TYPE_ITEM_DIR_TEMPLATE),
				type, 0);
		this.parentId       = new SimpleLongProperty(parentId);
		this.type           = new SimpleIntegerProperty(type);
		this.body           = new SimpleStringProperty(body);
	}
	
	/**
	 * Конструктор со всеми данными
	 * @param
	 */
	public TemplateItem(long id, long parentId, int type, String name, String descr, String body,
			            Date dateCreated, Date dateModified, String userCreated, String userModified) {
		super(id, name, descr, 0, 
				(((type == 0)||(type == 10)) ? TYPE_ITEM_TEMPLATE : TYPE_ITEM_DIR_TEMPLATE),
				type, 0, dateCreated, dateModified, userCreated, userModified);
		this.parentId       = new SimpleLongProperty(parentId);
		this.type           = new SimpleIntegerProperty(type);
		this.body           = new SimpleStringProperty(body);
	}
	
	/**
	 * Конструктор
	 * @param
	 */
	public TemplateItem(TemplateItem item) {
		super((TemplateSimpleItem)item);
		this.parentId       = new SimpleLongProperty(item.getParentId());
		this.type           = new SimpleIntegerProperty(item.getType());
		this.body           = new SimpleStringProperty(item.getBody());
	}
	
	// parentId  -- g,s,p
	public long getParentId() {
	    return parentId.get();
	}
	public void setParentId(long parentId) {
	    this.parentId.set(parentId);
	}
	public LongProperty parentIdProperty() {
	    return parentId;
	}
		
	// type  -- g,s,p
	public int getType() {
	    return type.get();
	}
	public void setType(int type) {
	    this.type.set(type);
	}
	public IntegerProperty typeProperty() {
	    return type;
	}
	
	// body -- g,s,p 
	public String getBody() {
		return body.get();
	}
	public void setBody(String body) {
		this.body.set(body);
	}
	public StringProperty bodyProperty() {
		return body;
	}
}
