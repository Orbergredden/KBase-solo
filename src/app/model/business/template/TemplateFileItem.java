
package app.model.business.template;

import app.lib.FileUtil;
import app.lib.ShowAppMsg;

import java.util.Date;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.image.Image;

/**
 * Класс содержит информацию об одном файле (или файловой директории) темы шаблонов.
 * @author Igor Makarevich
 * @version 2.00.00.001   06.04.2021
 */
public class TemplateFileItem extends TemplateSimpleItem {
	public static final int FILE_TYPE_TEXT     = 1;
	public static final int FILE_TYPE_IMAGE    = 2;
	public static final int FILE_TYPE_BINARY   = 3;
	
	/**
	 * Id родительской файловой директории или 0, если файл (дирктория) находится в корне
	 */
	private final LongProperty parentId;
	/**
	 * 0-файл, 1-директория ; (для необязательных файлов) 10 - файл, 11 - директория
	 */
	private final IntegerProperty type;
	/**
	 * Тип файла : 1 - текстовый ; 2 - картинка ; 3 - бинарный
	 */
	private final IntegerProperty fileType;
	
	private final StringProperty fileName;
	/**
	 * Текст
	 */
	private final StringProperty body;
	/**
	 * Картинка
	 */
	public Image bodyImage;
	
	/**
	 * Конструктор по умолчанию.
	 */
	public TemplateFileItem() {
		this(0, 0, 0, 0, 0, null, null, null, null);
	}
	
	/**
	 * Конструктор с основными данными
	 * @param
	 */
	public TemplateFileItem(
			long id, long parentId, long themeId, int type, int fileType, String fileName, String descr, 
			String body, Image bodyImage) {
		super(id, fileName, descr, themeId, 
				((type < 10) ? ((type == 0) ? TYPE_ITEM_FILE : TYPE_ITEM_DIR_FILE) : 
					((type == 10) ? TYPE_ITEM_FILE_OPTIONAL : TYPE_ITEM_DIR_FILE_OPTIONAL)), 
				type);
		flag = new SimpleLongProperty(fileType);

		this.parentId         = new SimpleLongProperty(parentId);
		this.type             = new SimpleIntegerProperty(type);
		this.fileType         = new SimpleIntegerProperty(fileType);
		this.fileName         = new SimpleStringProperty(fileName);
		this.body             = new SimpleStringProperty(body);
		this.bodyImage        = bodyImage;
	}
	
	/**
	 * Конструктор со всеми данными
	 * @param
	 */
	public TemplateFileItem(
			long id, long parentId, long themeId, int type, int fileType, String fileName, String descr, 
			String body, Image bodyImage, 
			Date dateCreated, Date dateModified, String userCreated, String userModified) {
		super(id, fileName, descr, themeId, 
				((type < 10) ? ((type == 0) ? TYPE_ITEM_FILE : TYPE_ITEM_DIR_FILE) : 
					((type == 10) ? TYPE_ITEM_FILE_OPTIONAL : TYPE_ITEM_DIR_FILE_OPTIONAL)), 
				type,
			  dateCreated, dateModified, userCreated, userModified);
		flag = new SimpleLongProperty(fileType);

		this.parentId         = new SimpleLongProperty(parentId);
		this.type             = new SimpleIntegerProperty(type);
		this.fileType         = new SimpleIntegerProperty(fileType);
		this.fileName         = new SimpleStringProperty(fileName);
		this.body             = new SimpleStringProperty(body);
		this.bodyImage        = bodyImage;
	}
	
	/**
	 * Конструктор
	 * @param
	 */
	public TemplateFileItem(TemplateFileItem item) {
		super((TemplateSimpleItem)item);
		this.parentId     = new SimpleLongProperty(item.getParentId());
		this.type         = new SimpleIntegerProperty(item.getType());
		this.fileType     = new SimpleIntegerProperty(item.getFileType());
		this.fileName     = new SimpleStringProperty(item.getFileName());
		this.body         = new SimpleStringProperty(item.getBody());
		this.bodyImage    = item.bodyImage;
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
	
	// fileType  -- g,s,p
	public int getFileType() {
	    return fileType.get();
	}
	public void setFileType(int fileType) {
	    this.fileType.set(fileType);
	}
	public IntegerProperty fileTypeProperty() {
	    return fileType;
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
	
	/**
	 * Сохраняем файл в дисковый кеш.
	 */
	public void saveToDisk (String path) {
		switch (getFileType()) {
		case FILE_TYPE_TEXT :
			//FileUtil.writeTextFile(path+"_files/"+getFileName(), getBody());
			FileUtil.writeTextFile(path+getFileName(), getBody());
			break;
		case FILE_TYPE_IMAGE :
			//FileUtil.writeImageFile(path +"_files/"+ getFileName(), bodyImage);
			FileUtil.writeImageFile(path + getFileName(), bodyImage);
			break;
		default :
			ShowAppMsg.showAlert("WARNING", "Сохранение файла на диск", "Для данного типа файла сохранение не реализовано.", 
		             "Файл не сохранен.");
		}
	}
}
