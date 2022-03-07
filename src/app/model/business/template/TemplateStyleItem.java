
package app.model.business.template;

import java.util.Date;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/*
 * Класс содержит информацию об одном стиле типа информационного блока
 * @author Игорь Макаревич
 * @version 2.00.00.002   07.04.2021
 */
public class TemplateStyleItem extends TemplateSimpleItem {
	/**
	 * Id родительской директории стиля или 0, если стиль (дирктория) находится в корне
	 */
	private final LongProperty parentId;
	/**
	 * 0-стиль, 1-директория ; (для зарезервированных) 10 - стиль, 11 - директория
	 */
	private final IntegerProperty type;
	/**
	 * Id типа инфоблока
	 */
	private final LongProperty infoTypeId;
	/**
	 * уникальный текстовый идентификатор для зарезервированных стилей
	 */
	private final StringProperty tag;
	
	/**
	 * Конструктор по умолчанию.
	 */
	public TemplateStyleItem() {
		this(0, 0, 0, 0, null, null, null);
	}
	
	/**
	 * Конструктор с основными данными
	 * @param
	 */
	public TemplateStyleItem(long id, long parentId, int type, long infoTypeId, String name, String descr, String tag) {
		super(id, name, descr, 0,
				(((type == 0)||(type == 10)) ? TYPE_ITEM_STYLE : TYPE_ITEM_DIR_STYLE),
				type, infoTypeId);
		this.parentId     = new SimpleLongProperty(parentId);
		this.type         = new SimpleIntegerProperty(type);
		this.infoTypeId   = new SimpleLongProperty(infoTypeId);
		this.tag          = new SimpleStringProperty(tag);
	}
	
	/**
	 * Конструктор со всеми данными
	 * @param
	 */
	public TemplateStyleItem(long id, long parentId, int type, long infoTypeId, String name, String descr, String tag,
                  			 Date dateCreated, Date dateModified, String userCreated, String userModified) {
		super(id, name, descr, 0,
				(((type == 0)||(type == 10)) ? TYPE_ITEM_STYLE : TYPE_ITEM_DIR_STYLE),
				type, infoTypeId,
				dateCreated, dateModified, userCreated, userModified);
		this.parentId     = new SimpleLongProperty(parentId);
		this.type         = new SimpleIntegerProperty(type);
		this.infoTypeId   = new SimpleLongProperty(infoTypeId);
		this.tag          = new SimpleStringProperty(tag);
	}
	
	/**
	 * Конструктор
	 * @param
	 */
	public TemplateStyleItem(TemplateStyleItem item) {
		super((TemplateSimpleItem)item);
		this.parentId     = new SimpleLongProperty(item.getParentId());
		this.type         = new SimpleIntegerProperty(item.getType());
		this.infoTypeId   = new SimpleLongProperty(item.getInfoTypeId());
		this.tag          = new SimpleStringProperty(item.getTag());
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
	
	// tag  -- g,s,p
	public String getTag() {
		return tag.get();
	}
	public void setTag(String tag) {
		this.tag.set(tag);
	}
	public StringProperty tagProperty() {
		return tag;
	}
}
