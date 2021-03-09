
package app.model.business;

import java.util.Date;

import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;

/*
 * Класс содержит информацию об одном стиле типа информационного блока
 * 
 * @author Игорь Макаревич
 */
public class InfoTypeStyleItem extends SimpleItem {
	/**
	 * Id родительского стиля или null, если это корневой стиль в импе инфоблока
	 */
	private final LongProperty parentId;
	/**
	 * Id типа инфоблока
	 */
	private final LongProperty infoTypeId;
	
	/**
	 * Конструктор по умолчанию.
	 */
	public InfoTypeStyleItem() {
		this(0, 0, 0, null, null);
	}
	
	/**
	 * Конструктор с основными данными
	 * @param
	 */
	public InfoTypeStyleItem(long id, long parentId, long infoTypeId, String name, String descr) {
		super(id, name, descr);
		this.parentId     = new SimpleLongProperty(parentId);
		this.infoTypeId   = new SimpleLongProperty(infoTypeId);
	}
	
	/**
	 * Конструктор со всеми данными
	 * @param
	 */
	public InfoTypeStyleItem(
			long id, long parentId, long infoTypeId, String name, String descr,
			Date dateCreated, Date dateModified, String userCreated, String userModified) {
		super(id, name, descr, dateCreated, dateModified, userCreated, userModified);
		this.parentId     = new SimpleLongProperty(parentId);
		this.infoTypeId   = new SimpleLongProperty(infoTypeId);
	}
	
	/**
	 * Конструктор
	 * @param
	 */
	public InfoTypeStyleItem(InfoTypeStyleItem item) {
		super((SimpleItem)item);
		this.parentId     = new SimpleLongProperty(item.getParentId());
		this.infoTypeId   = new SimpleLongProperty(item.getInfoTypeId());
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
	
	// infoTypeId  -- g,s,p
	public long getInfoTypeId() {
	    return infoTypeId.get();
	}
	public void setInfoTypeId(long infoTypeId) {
	    this.infoTypeId.set(infoTypeId);
	}
	public LongProperty infoTypeIdProperty() {
	    return infoTypeId;
	}
}
