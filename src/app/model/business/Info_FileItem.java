package app.model.business;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/*
 * Класс содержит тело информационного блока документа типа "Файл"
 * 
 * @author Игорь Макаревич
 */
public class Info_FileItem extends SimpleItem {
	/**
	 * Заголовок инфо блока
	 */
	private final StringProperty title;
	/**
	 * тело файла
	 */
	private byte[] fileBody;
	/**
	 * Имя файла
	 */
	//private final StringProperty fileName; -- берем из SimpleItem.name
	/**
	 * иконка для показа типа файла 
	 */
	private final LongProperty iconId;
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
	public Info_FileItem() {
		this(0, null, null, null, 0, null, null, 0, 0, 0);
	}
	
	/**
	 * Конструктор с основными данными
	 * @param
	 */
	public Info_FileItem(long id, String title, byte[] fileBody, String fileName,
			             long iconId, String descr, String text, 
			             int isShowTitle, int isShowDescr, int isShowText) {
		super(id, fileName, descr);
		this.title       = new SimpleStringProperty(title);
		this.fileBody    = fileBody;
		this.iconId      = new SimpleLongProperty(iconId);
		this.text        = new SimpleStringProperty(text);
		this.isShowTitle = new SimpleIntegerProperty(isShowTitle);
		this.isShowDescr = new SimpleIntegerProperty(isShowDescr);
		this.isShowText  = new SimpleIntegerProperty(isShowText);
	}
	
	/**
	 * Конструктор со всеми данными
	 * @param
	 */
	public Info_FileItem(long id, String title, byte[] fileBody, String fileName,
            long iconId, String descr, String text, 
            int isShowTitle, int isShowDescr, int isShowText,
            Date dateCreated, Date dateModified, String userCreated, String userModified) {
		super(id, fileName, descr, dateCreated, dateModified, userCreated, userModified);
		this.title       = new SimpleStringProperty(title);
		this.fileBody    = fileBody;
		this.iconId      = new SimpleLongProperty(iconId);
		this.text        = new SimpleStringProperty(text);
		this.isShowTitle = new SimpleIntegerProperty(isShowTitle);
		this.isShowDescr = new SimpleIntegerProperty(isShowDescr);
		this.isShowText  = new SimpleIntegerProperty(isShowText);
	}
	
	/**
	 * Конструктор
	 * @param
	 */
	public Info_FileItem(Info_FileItem item) {
		super((SimpleItem)item);
		this.title       = new SimpleStringProperty(item.getTitle());
		this.fileBody    = item.getFileBody();
		this.iconId      = new SimpleLongProperty(item.getIconId());
		this.text        = new SimpleStringProperty(item.getText());
		this.isShowTitle = new SimpleIntegerProperty(item.getIsShowTitle());
		this.isShowDescr = new SimpleIntegerProperty(item.getIsShowDescr());
		this.isShowText  = new SimpleIntegerProperty(item.getIsShowText());
	}
	
	/**
	 * Write item to file.
	 * @param filePath
	 * @throws IOException
	 */
	public void saveToFile (String filePath) throws IOException {
		File file = new File(filePath);
		
    	if (file != null) {
    		FileOutputStream fos = new FileOutputStream(file);
            fos.write(fileBody, 0, fileBody.length);
   			fos.close();
		}
	}
	//TODO 02.03.2020

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
	
	// title  -- g,s
	public byte[] getFileBody() {
	    return fileBody;
	}
	public void setFileBody(byte[] fileBody) {
	    this.fileBody = fileBody;
	}
	
	// iconId  -- g,s,p
	public long getIconId() {
	    return iconId.get();
	}
	public void setIconId(long iconId) {
	    this.iconId.set(iconId);
	}
	public LongProperty idProperty() {
	    return iconId;
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
