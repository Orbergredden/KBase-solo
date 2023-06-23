
package app.model.business;

import java.util.Date;

import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;

/*
 * Класс содержит информацию об одном заголовке информационного блока документа
 * 
 * @author Игорь Макаревич
 */
public class InfoHeaderItem extends SimpleItem {
	/**
	 * Id раздела
	 */
	private final LongProperty sectionId;
	/**
	 * Id типа информационного блока (текст, ссылка, картинка и тд)
	 */
	private final LongProperty infoTypeId;
	/**
	 * Id - Стиль шаблона инфо блока.
	 */
	//private final LongProperty infoTypeStyleId;
	private final LongProperty templateStyleId;
	/**
	 * Id - ссылка на инфу в таблице с информационным блоком соответствующего типа
	 */
	private final LongProperty infoId;
	/**
	 * Положение (порядковый номер) в документе.
	 */
	private final LongProperty position;
	
	/**
	 * Конструктор по умолчанию.
	 */
	public InfoHeaderItem() {
		this(0, 0, 0, 0, 0, 0, null, null);
	}
	
	/**
	 * Конструктор с основными данными
	 * @param
	 */
	public InfoHeaderItem(long id, long sectionId, long infoTypeId, long templateStyleId, long infoId,
			              long position, String name, String descr) {
		super(id, name, descr);
		this.sectionId       = new SimpleLongProperty(sectionId);
		this.infoTypeId      = new SimpleLongProperty(infoTypeId);
		this.templateStyleId = new SimpleLongProperty(templateStyleId);
		this.infoId          = new SimpleLongProperty(infoId);
		this.position        = new SimpleLongProperty(position);
	}
	
	/**
	 * Конструктор со всеми данными
	 * @param
	 */
	public InfoHeaderItem(
			long id, long sectionId, long infoTypeId, long templateStyleId, long infoId,
            long position, String name, String descr,
			Date dateCreated, Date dateModified, String userCreated, String userModified) {
		super(id, name, descr, dateCreated, dateModified, userCreated, userModified);
		this.sectionId       = new SimpleLongProperty(sectionId);
		this.infoTypeId      = new SimpleLongProperty(infoTypeId);
		this.templateStyleId = new SimpleLongProperty(templateStyleId);
		this.infoId          = new SimpleLongProperty(infoId);
		this.position        = new SimpleLongProperty(position);
	}
	
	/**
	 * Конструктор
	 * @param
	 */
	public InfoHeaderItem(InfoHeaderItem item) {
		super((SimpleItem)item);
		this.sectionId       = new SimpleLongProperty(item.getSectionId());
		this.infoTypeId      = new SimpleLongProperty(item.getInfoTypeId());
		this.templateStyleId = new SimpleLongProperty(item.getTemplateStyleId());
		this.infoId          = new SimpleLongProperty(item.getInfoId());
		this.position        = new SimpleLongProperty(item.getPosition());
	}

	// sectionId  -- g,s,p
	public long getSectionId() {
	    return sectionId.get();
	}
	public void setSectionId(long sectionId) {
	    this.sectionId.set(sectionId);
	}
	public LongProperty sectionIdProperty() {
	    return sectionId;
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
	
	// templateStyleId  -- g,s,p
	public long getTemplateStyleId() {
	    return templateStyleId.get();
	}
	public void setTemplateStyleId(long templateStyleId) {
	    this.templateStyleId.set(templateStyleId);
	}
	public LongProperty templateStyleIdProperty() {
	    return templateStyleId;
	}
	
	// infoId  -- g,s,p
	public long getInfoId() {
	    return infoId.get();
	}
	public void setInfoId(long infoId) {
	    this.infoId.set(infoId);
	}
	public LongProperty infoIdProperty() {
	    return infoId;
	}
	
	// position  -- g,s,p
	public long getPosition() {
	    return position.get();
	}
	public void setPosition(long position) {
	    this.position.set(position);
	}
	public LongProperty positionProperty() {
	    return position;
	}
}
