
package app.model.business;

import java.util.Date;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.image.Image;

/*
 * Класс содержит тело информационного блока документа типа "Изображение"
 * 
 * @author Игорь Макаревич
 */
public class Info_ImageItem extends SimpleItem {
	/**
	 * Заголовок инфо блока
	 */
	private final StringProperty title;
	/**
	 * Изображение
	 */
	public Image image;
	/**
	 * Ширина изображения, если не указана, то оригинальная
	 */
	private final IntegerProperty width;
	/**
	 * Высота изображения, если не указана, то оригинальная
	 */
	private final IntegerProperty height;
	/**
	 * Текст инфоблока
	 */
	private final StringProperty text;
	/**
	 * 1 - показывать заголовок ; 0 или NULL - не показывать
	 */
	private final IntegerProperty isShowTitle;
	/**
	 * 1 - показывать описание ; 0 или NULL - не показывать
	 */
	private final IntegerProperty isShowDescr;
	/**
	 * 1 - показывать текст ; 0 или NULL - не показывать
	 */
	private final IntegerProperty isShowText;
	
	/**
	 * Конструктор по умолчанию.
	 */
	public Info_ImageItem() {
		this(0, null, null, 0, 0, null, null, 0, 0, 0);
	}
	
	/**
	 * Конструктор с основными данными
	 * @param
	 */
	public Info_ImageItem(long id, String title, 
			              Image image, int width, int height, String descr, String text, 
			              int isShowTitle, int isShowDescr, int isShowText) {
		super(id, null, descr);
		this.title       = new SimpleStringProperty(title);
		this.image       = image;
		this.width       = new SimpleIntegerProperty(width);
		this.height      = new SimpleIntegerProperty(height);
		this.text        = new SimpleStringProperty(text);
		this.isShowTitle = new SimpleIntegerProperty(isShowTitle);
		this.isShowDescr = new SimpleIntegerProperty(isShowDescr);
		this.isShowText  = new SimpleIntegerProperty(isShowText);
	}
	
	/**
	 * Конструктор со всеми данными
	 * @param
	 */
	public Info_ImageItem(
			long id, String title, 
            Image image, int width, int height, String descr, String text, 
            int isShowTitle, int isShowDescr, int isShowText,
			Date dateCreated, Date dateModified, String userCreated, String userModified) {
		super(id, null, descr, dateCreated, dateModified, userCreated, userModified);
		this.title       = new SimpleStringProperty(title);
		this.image       = image;
		this.width       = new SimpleIntegerProperty(width);
		this.height      = new SimpleIntegerProperty(height);
		this.text        = new SimpleStringProperty(text);
		this.isShowTitle = new SimpleIntegerProperty(isShowTitle);
		this.isShowDescr = new SimpleIntegerProperty(isShowDescr);
		this.isShowText  = new SimpleIntegerProperty(isShowText);
	}
	
	/**
	 * Конструктор
	 * @param
	 */
	public Info_ImageItem(Info_ImageItem item) {
		super((SimpleItem)item);
		this.title       = new SimpleStringProperty(item.getTitle());
		this.image       = item.image;
		this.width       = new SimpleIntegerProperty(item.getWidth());
		this.height      = new SimpleIntegerProperty(item.getHeight());
		this.text        = new SimpleStringProperty(item.getText());
		this.isShowTitle = new SimpleIntegerProperty(item.getIsShowTitle());
		this.isShowDescr = new SimpleIntegerProperty(item.getIsShowDescr());
		this.isShowText  = new SimpleIntegerProperty(item.getIsShowText());
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
	
	// width  -- g,s,p
	public int getWidth() {
	    return width.get();
	}
	public void setWidth(int width) {
	    this.width.set(width);
	}
	public IntegerProperty widthProperty() {
	    return width;
	}
	
	// height  -- g,s,p
	public int getHeight() {
	    return height.get();
	}
	public void setHeight(int height) {
	    this.height.set(height);
	}
	public IntegerProperty heightProperty() {
	    return height;
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
	
	// isShowDescr  -- g,s,p
	public int getIsShowDescr() {
	    return isShowDescr.get();
	}
	public void setIsShowDescr(int isShowDescr) {
	    this.isShowDescr.set(isShowDescr);
	}
	public IntegerProperty isShowDescrProperty() {
	    return isShowDescr;
	}
	
	// isShowText  -- g,s,p
	public int getIsShowText() {
	    return isShowText.get();
	}
	public void setIsShowText(int isShowText) {
	    this.isShowText.set(isShowText);
	}
	public IntegerProperty isShowTextProperty() {
	    return isShowText;
	}
}
