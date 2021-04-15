
package app.model.business.template;

import app.model.business.SimpleItem;
import java.util.Date;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;

/**
 * Базовый класс-элемент для для различных классов шаблонов
 * Класс содержит информацию о шаблоне, теме, стиле или файле.
 * Используется в смешанных списках.
 * @author Igor Makarevich
 * @version 2.00.00.002   04.04.2021
 */
public class TemplateSimpleItem extends SimpleItem {
	// for "typeItem"
	public static final int TYPE_ITEM_ROOT                  = 0;
	public static final int TYPE_ITEM_SECTION_THEME         = 10;
	public static final int TYPE_ITEM_THEME                 = 1;
	public static final int TYPE_ITEM_SECTION_FILE          = 2;
	public static final int TYPE_ITEM_FILE                  = 3;
	public static final int TYPE_ITEM_SECTION_FILE_OPTIONAL = 4;
	public static final int TYPE_ITEM_FILE_OPTIONAL         = 5;
	public static final int TYPE_ITEM_SECTION_STYLE         = 6;
	public static final int TYPE_ITEM_STYLE                 = 7;
	public static final int TYPE_ITEM_SECTION_TEMPLATE      = 8;
	public static final int TYPE_ITEM_TEMPLATE              = 9;
	
	protected final LongProperty themeId;
	
	/**
	 * Тип записи: 
	 * 0 - корень
	 * 1 - тема
	 * 2 - раздел с обязательными файлами
	 * 3 - обязательный файл
	 * 4 - раздел с не обязательными файлами
	 * 5 - не обязательный файл
	 * 6 - раздел со стилями (какой именно вид стиля определяется дополнительной переменной) 
	 * 7 - стиль
	 * 8 - раздел с шаблонами
	 * 9 - шаблон
	 */
	protected final IntegerProperty typeItem;
	/**
	 * Подтип записи. В случае стиля содежит ид типа инфо блока или -1 - зарезервированный стиль
	 */
	protected final LongProperty subtypeItem;
	
	/**
	 * Конструктор по умолчанию.
	 */
	public TemplateSimpleItem() {
		this(0, null, null, 0, 0, 0);
	}
	
	/**
	 * Конструктор с основными данными
	 * @param
	 */
	public TemplateSimpleItem(
			long id, String name, String descr, long themeId, int typeItem, long subtypeItem) {
		super(id, name, descr);
		this.themeId          = new SimpleLongProperty(themeId);
		this.typeItem         = new SimpleIntegerProperty(typeItem);
		this.subtypeItem      = new SimpleLongProperty(subtypeItem);
	}
	
	/**
	 * Конструктор со всеми данными
	 * @param
	 */
	public TemplateSimpleItem(
			long id, String name, String descr, long themeId, int typeItem, long subtypeItem,
			Date dateCreated, Date dateModified, String userCreated, String userModified) {
		super(id, name, descr, dateCreated, dateModified, userCreated, userModified);
		this.themeId          = new SimpleLongProperty(themeId);
		this.typeItem         = new SimpleIntegerProperty(typeItem);
		this.subtypeItem      = new SimpleLongProperty(subtypeItem);
	}
	
	/**
	 * Конструктор
	 * @param
	 */
	public TemplateSimpleItem(TemplateSimpleItem item) {
		super((SimpleItem)item);
		this.themeId      = new SimpleLongProperty(item.getThemeId());
		this.typeItem     = new SimpleIntegerProperty(item.getTypeItem());
		this.subtypeItem  = new SimpleLongProperty(item.getSubtypeItem());
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
	
	// typeItem  -- g,s,p
	public int getTypeItem() {
	    return typeItem.get();
	}
	public void setTypeItem(int typeItem) {
	    this.typeItem.set(typeItem);
	}
	public IntegerProperty typeItemProperty() {
	    return typeItem;
	}
	
	// subtypeItem  -- g,s,p
	public long getSubtypeItem() {
	    return subtypeItem.get();
	}
	public void setSubtypeItem(long subtypeItem) {
	    this.subtypeItem.set(subtypeItem);
	}
	public LongProperty subtypeItemProperty() {
	    return subtypeItem;
	}
}
