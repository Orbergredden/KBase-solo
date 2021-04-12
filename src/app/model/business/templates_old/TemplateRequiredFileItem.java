
package app.model.business.templates_old;

import app.lib.FileUtil;
import app.lib.ShowAppMsg;

import java.util.Date;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.image.Image;

/**
 * Класс содержит информацию об одном обязательном файле темы шаблонов.
 * 
 * @author Игорь Макаревич
 */
public class TemplateRequiredFileItem extends TemplateSimpleItem {

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
	public TemplateRequiredFileItem() {
		this(0, 0, null, null, null, null, 0, 0);
	}
	
	/**
	 * Конструктор с основными данными
	 * @param
	 */
	public TemplateRequiredFileItem(
			long id, long themeId, String fileName, String descr, String body, Image bodyImage, int fileType, int fileTypeExt) {
		super(id, themeId, fileName, descr, TYPE_FILE, fileType, fileTypeExt);
		this.fileName         = new SimpleStringProperty(fileName);
		this.body             = new SimpleStringProperty(body);
		this.bodyImage        = bodyImage;
	}
	
	/**
	 * Конструктор со всеми данными
	 * @param
	 */
	public TemplateRequiredFileItem(
			long id, long themeId, String fileName, String descr, String body, Image bodyImage, int fileType, int fileTypeExt, 
			Date dateCreated, Date dateModified, String userCreated, String userModified) {
		super(id, themeId, fileName, descr, TYPE_FILE, fileType, fileTypeExt, 
			  dateCreated, dateModified, userCreated, userModified);
		this.fileName         = new SimpleStringProperty(fileName);
		this.body             = new SimpleStringProperty(body);
		this.bodyImage        = bodyImage;
	}
	
	/**
	 * Конструктор
	 * @param
	 */
	public TemplateRequiredFileItem(TemplateRequiredFileItem item) {
		super((TemplateSimpleItem)item);
		this.fileName     = new SimpleStringProperty(item.getFileName());
		this.body         = new SimpleStringProperty(item.getBody());
		this.bodyImage    = item.bodyImage;
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
	
		switch (getFileTypeExt()) {
		case FILETYPEEXT_TEXT :
			//FileUtil.writeTextFile(path+"_files/"+getFileName(), getBody());
			FileUtil.writeTextFile(path+getFileName(), getBody());
			break;
		case FILETYPEEXT_IMAGE :
			//FileUtil.writeImageFile(path +"_files/"+ getFileName(), bodyImage);
			FileUtil.writeImageFile(path + getFileName(), bodyImage);
			break;
		default :
			ShowAppMsg.showAlert("WARNING", "Сохранение файла на диск", "Для данного типа файла сохранение не реализовано.", 
		             "Файл не сохранен.");
		}
	}
}
