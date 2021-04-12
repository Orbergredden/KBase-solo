
package app.model.business.templates_old;

import app.model.business.SimpleItem;
import java.util.Date;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;

/**
 * Базовый класс-элемент для для различных классов шаблонов
 * Класс содержит информацию о шаблоне, теме или файле для шаблонов.
 * Используется в смешанных списках.
 * @author Igor Makarevich
 * @version 1.00.00.001   01.03.2018
 */
public class TemplateSimpleItem extends SimpleItem {
	// for "type"
	public static final int TYPE_ROOT              = 0;
	public static final int TYPE_THEME             = 1;
	public static final int TYPE_DIR_FOR_FILES     = 2;
	public static final int TYPE_FILE              = 3;
	public static final int TYPE_DIR_FOR_TEMPLATES = 4;
	public static final int TYPE_STYLE             = 6;
	public static final int TYPE_TEMPLATE          = 5;
	// for "fileType"
	public static final int FILETYPE_REQUIRED_FILE = 0;    // обязательные файлы
	public static final int FILETYPE_MAIN_FILE     = 1;    // основной файл
	public static final int FILETYPE_NO_INFO_FILE  = 2;    // "Информация отсутствует"
	public static final int FILETYPE_LOAD_FILE     = 3;    // "Загрузка..."
	// for "fileTypeExt"
	public static final int FILETYPEEXT_TEXT       = 1;    // текстовый
	public static final int FILETYPEEXT_IMAGE      = 2;    // картинка
	public static final int FILETYPEEXT_BINARY     = 3;    // бинарный
	
	protected final LongProperty themeId;
	
	/**
	 * Тип записи: 
	 * 0 - корень
	 * 1 - тема
	 * 2 - папка для обязательных файлов
	 * 3 - обязательный файл
	 * 4 - папка для шаблонов определенного типа
	 * 6 - стиль шаблона
	 * 5 - шаблон 
	 */
	protected final IntegerProperty type;
	/**
	 * Тип файла в случае файлов. Id инфо блока в случае стилей и шаблонов
	 */
	protected final LongProperty fileType;
	/**
	 * file type by extension
	 */
	protected final IntegerProperty fileTypeExt;
	/**
	 * Такой же класс внутри нашего.
	 * Сделан для хранения стиля, если основной обьект шаблон.
	 */
	protected TemplateSimpleItem subItem;
	
	/**
	 * Конструктор по умолчанию.
	 */
	public TemplateSimpleItem() {
		this(0, 0, null, null, 0, 0, 0);
	}
	
	/**
	 * Конструктор с основными данными
	 * @param
	 */
	public TemplateSimpleItem(
			long id, long themeId, String name, String descr, int type, long fileType, int fileTypeExt) {
		super(id, name, descr);
		this.themeId          = new SimpleLongProperty(themeId);
		this.type             = new SimpleIntegerProperty(type);
		this.fileType         = new SimpleLongProperty(fileType);
		this.fileTypeExt      = new SimpleIntegerProperty(fileTypeExt);
	}
	
	/**
	 * Конструктор со всеми данными
	 * @param
	 */
	public TemplateSimpleItem(
			long id, long themeId, String name, String descr, int type, long fileType, int fileTypeExt,
			Date dateCreated, Date dateModified, String userCreated, String userModified) {
		super(id, name, descr, dateCreated, dateModified, userCreated, userModified);
		this.themeId          = new SimpleLongProperty(themeId);
		this.type             = new SimpleIntegerProperty(type);
		this.fileType         = new SimpleLongProperty(fileType);
		this.fileTypeExt      = new SimpleIntegerProperty(fileTypeExt);
	}
	
	/**
	 * Конструктор
	 * @param
	 */
	public TemplateSimpleItem(TemplateSimpleItem item) {
		super((SimpleItem)item);
		this.themeId      = new SimpleLongProperty(item.getThemeId());
		this.type         = new SimpleIntegerProperty(item.getType());
		this.fileType     = new SimpleLongProperty(item.getFileType());
		this.fileTypeExt  = new SimpleIntegerProperty(item.getFileTypeExt());
	}
	
	// themeId  -- g,s,p
	public long getThemeId() {
		return themeId.get();
	}
	public void setThemeId(long themeId) {
	    this.themeId.set(themeId);
	}
	public LongProperty themeIdProperty() {
		return themeId;
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
	public long getFileType() {
	    return fileType.get();
	}
	public void setFileType(long fileType) {
	    this.fileType.set(fileType);
	}
	public LongProperty fileTypeProperty() {
	    return fileType;
	}
	
	// fileTypeExt  -- g,s,p
	public int getFileTypeExt() {
	    return fileTypeExt.get();
	}
	public void setFileTypeExt(int fileTypeExt) {
	    this.fileTypeExt.set(fileTypeExt);
	}
	public IntegerProperty fileTypeExtProperty() {
	    return fileTypeExt;
	}
	
	// getter/setter for TemplateSimpleItem subItem
	public TemplateSimpleItem getSubItem() {  return subItem;  }
	public void setSubItem (TemplateSimpleItem tsi) {  subItem = tsi;  }
}
