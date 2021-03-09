
package app.model.business;

import java.util.Date;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.image.Image;

/**
 * Класс содержит информацию об одном информационном разделе.
 * 
 * @author Игорь Макаревич
 */
public class SectionItem extends SimpleItem {
	private final LongProperty parentId;
	private final LongProperty iconId; 
	public Image icon;
	private final ObjectProperty<Date> dateModifiedInfo;
	
	private final LongProperty iconIdRoot;  // Корневая иконка поддерева для выбора иконок для данного и дочерних разделов
	private final LongProperty iconIdDef;   // Иконка по умолчанию для данного и дочерних разделов
	private final LongProperty themeId;     // Тема (шаблонов) для показа документа
	private final IntegerProperty cacheType;// Тип кеширования : 1 - документы кешируются на локальном диске; 2 - кешируются в БД; 3 - кешируются на диске только обязательные файлы
	
	/**
	 * Конструктор по умолчанию.
	 */
	public SectionItem() {
		this(0, 0, null, 0, null, null, null, null, null, null, null, 0, 0, 0, 0);
	}
	
	/**
	 * Конструктор
	 * @param
	 */
	public SectionItem(
			long id, long parentId, String name, long iconId, Image icon, String descr, 
			Date dateCreated, Date dateModified, String userCreated, String userModified, Date dateModifiedInfo,
			long iconIdRoot, long iconIdDef, long themeId, int cacheType) {
		super(id, name, descr, dateCreated, dateModified, userCreated, userModified);
		this.parentId         = new SimpleLongProperty(parentId);
		this.iconId           = new SimpleLongProperty(iconId);
		this.icon             = icon;
		this.dateModifiedInfo = new SimpleObjectProperty<Date>(dateModifiedInfo);
		
		this.iconIdRoot       = new SimpleLongProperty(iconIdRoot);
		this.iconIdDef        = new SimpleLongProperty(iconIdDef);
		this.themeId          = new SimpleLongProperty(themeId);
		this.cacheType        = new SimpleIntegerProperty(cacheType);
	}
	
	/**
	 * Конструктор
	 * @param
	 */
	public SectionItem(SectionItem item) {
		super((SimpleItem)item);
		this.parentId     = new SimpleLongProperty(item.getParentId());
		this.iconId       = new SimpleLongProperty(item.getIconId());
		this.icon         = item.icon;
		this.dateModifiedInfo = new SimpleObjectProperty<Date>(item.getDateModifiedInfo());
		
		this.iconIdRoot       = new SimpleLongProperty(item.getIconIdRoot());
		this.iconIdDef        = new SimpleLongProperty(item.getIconIdDef());
		this.themeId          = new SimpleLongProperty(item.getThemeId());
		this.cacheType        = new SimpleIntegerProperty(item.getCacheType());
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
		
	// iconId  -- g,s,p
    public long getIconId() {
    	return iconId.get();
	}
	public void setIconId(long iconId) {
	    this.iconId.set(iconId);
	}
	public LongProperty iconIdProperty() {
	    return iconId;
	}

  	// dateModifiedInfo -- g,s,p
    public Date getDateModifiedInfo() {
        return dateModifiedInfo.get();
    }
    public void setDateModifiedInfo(Date dateModifiedInfo) {
        this.dateModifiedInfo.set(dateModifiedInfo);
    }
    public ObjectProperty<Date> dateModifiedInfoProperty() {
        return dateModifiedInfo;
    }
    
    // iconIdRoot  -- g,s,p
    public long getIconIdRoot() {
    	return iconIdRoot.get();
	}
	public void setIconIdRoot(long iconIdRoot) {
	    this.iconIdRoot.set(iconIdRoot);
	}
	public LongProperty iconIdRootProperty() {
	    return iconIdRoot;
	}
	
	// iconIdDef  -- g,s,p
    public long getIconIdDef() {
    	return iconIdDef.get();
	}
	public void setIconIdDef(long iconIdDef) {
	    this.iconIdDef.set(iconIdDef);
	}
	public LongProperty iconIdDefProperty() {
	    return iconIdDef;
	}
	
	// themeId  -- g,s,p
    public long getThemeId() {
    	return themeId.get();
	}
	public void setThemeId(long themeId) {
	    this.themeId.set(themeId);
	}
	public LongProperty themeIdDefProperty() {
	    return themeId;
	}
	
	// cacheType  -- g,s,p
    public int getCacheType() {
    	return cacheType.get();
	}
	public void setCacheType(int cacheType) {
	    this.cacheType.set(cacheType);
	}
	public IntegerProperty cacheTypeProperty() {
	    return cacheType;
	}
}
