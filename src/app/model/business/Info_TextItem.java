
package app.model.business;

import java.util.Date;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/*
 * Класс содержит тело информационного блока документа типа "Простой текст"
 * 
 * @author Игорь Макаревич
 */
public class Info_TextItem extends SimpleItem {
	/**
	 * Заголовок инфо блока
	 */
	private final StringProperty title;
	/**
	 * Текст инфоблока
	 */
	private final StringProperty text;
	/**
	 * 1 - показывать заголовок ; 0 или NULL - не показывать
	 */
	private final IntegerProperty isShowTitle;
	
	/**
	 * Конструктор по умолчанию.
	 */
	public Info_TextItem() {
		this(0, null, null, 0);
	}
	
	/**
	 * Конструктор с основными данными
	 * @param
	 */
	public Info_TextItem(long id, String title, String text, int isShowTitle) {
		super(id, null, null);
		this.title       = new SimpleStringProperty(title);
		this.text        = new SimpleStringProperty(text);
		this.isShowTitle = new SimpleIntegerProperty(isShowTitle);
	}
	
	/**
	 * Конструктор со всеми данными
	 * @param
	 */
	public Info_TextItem(
			long id, String title, String text, int isShowTitle,
			Date dateCreated, Date dateModified, String userCreated, String userModified) {
		super(id, null, null, dateCreated, dateModified, userCreated, userModified);
		this.title       = new SimpleStringProperty(title);
		this.text        = new SimpleStringProperty(text);
		this.isShowTitle = new SimpleIntegerProperty(isShowTitle);
	}
	
	/**
	 * Конструктор
	 * @param
	 */
	public Info_TextItem(Info_TextItem item) {
		super((SimpleItem)item);
		this.title       = new SimpleStringProperty(item.getTitle());
		this.text        = new SimpleStringProperty(item.getText());
		this.isShowTitle = new SimpleIntegerProperty(item.getIsShowTitle());
	}
	
	// title  -- g,s,p
	public String getTitle() {
	    return title.get();
	}
	public void setTitle(String title) {
	    this.title.set(title);
	}
	public StringProperty titleProperty() {
	    return title;
	}
	
	// text  -- g,s,p
	public String getText() {
	    return text.get();
	}
	public void setText(String text) {
	    this.text.set(text);
	}
	public StringProperty textProperty() {
	    return text;
	}
	
	// isShowTitle  -- g,s,p
	public int getIsShowTitle() {
	    return isShowTitle.get();
	}
	public void setIsShowTitle(int isShowTitle) {
	    this.isShowTitle.set(isShowTitle);
	}
	public IntegerProperty isShowTitleProperty() {
	    return isShowTitle;
	}
}
