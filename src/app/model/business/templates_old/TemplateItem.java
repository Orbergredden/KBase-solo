
package app.model.business.templates_old;

import java.util.Date;

import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Класс содержит информацию об одном шаблоне документа (его инфо блока).
 * 
 * @author Игорь Макаревич
 */
public class TemplateItem extends TemplateSimpleItem {
	private final LongProperty infoTypeId;
	private final LongProperty infoTypeStyleId;
	private final StringProperty fileName;
	private final StringProperty body;
	
	/**
	 * Конструктор по умолчанию.
	 */
	public TemplateItem() {
		this(0, 0, 0, 0, null, null, null, null);
	}
	
	/**
	 * Конструктор с основными данными
	 * @param
	 */
	public TemplateItem(
			long id, long themeId, long infoTypeId, long infoTypeStyleId,
			String name, String fileName, String descr, String body) {
		super(id, themeId, name, descr, TYPE_TEMPLATE, 0, 0);
		this.infoTypeId       = new SimpleLongProperty(infoTypeId);
		this.infoTypeStyleId  = new SimpleLongProperty(infoTypeStyleId);
		this.fileName         = new SimpleStringProperty(fileName);
		this.body             = new SimpleStringProperty(body);
	}
	
	/**
	 * Конструктор со всеми данными
	 * @param
	 */
	public TemplateItem(
			long id, long themeId, long infoTypeId, long infoTypeStyleId,
			String name, String fileName, String descr, String body,
			Date dateCreated, Date dateModified, String userCreated, String userModified) {
		super(id, themeId, name, descr, TYPE_TEMPLATE, 0, 0, dateCreated, dateModified, userCreated, userModified);
		this.infoTypeId       = new SimpleLongProperty(infoTypeId);
		this.infoTypeStyleId  = new SimpleLongProperty(infoTypeStyleId);
		this.fileName         = new SimpleStringProperty(fileName);
		this.body             = new SimpleStringProperty(body);
	}
	
	/**
	 * Конструктор
	 * @param
	 */
	public TemplateItem(TemplateItem item) {
		super((TemplateSimpleItem)item);
		this.infoTypeId      = new SimpleLongProperty(item.getInfoTypeId());
		this.infoTypeStyleId = new SimpleLongProperty(item.getInfoTypeStyleId());
		this.fileName        = new SimpleStringProperty(item.getFileName());
		this.body            = new SimpleStringProperty(item.getBody());
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
	
	// infoTypeStyleId  -- g,s,p
	public long getInfoTypeStyleId() {
		return infoTypeStyleId.get();
	}
	public void setInfoTypeStyleId(long infoTypeStyleId) {
	    this.infoTypeStyleId.set(infoTypeStyleId);
	}
	public LongProperty infoTypeStyleIdProperty() {
		return infoTypeStyleId;
	}
	
	// fileName  -- g,s,p
	public String getFileName() {
		return fileName.get();
	}
	public void setFileName(String fileName) {
		this.fileName.set(fileName);
	}
	public StringProperty fileNameProperty() {
			return fileName;
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
