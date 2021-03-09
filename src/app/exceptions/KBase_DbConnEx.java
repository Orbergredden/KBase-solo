package app.exceptions;

/**
 * Класс исключительной ситуации. 
 * Возбуждается при ошибке соединения с БД. 
 * 
 * @author Igor Makarevich
 */
public class KBase_DbConnEx  extends KBase_Ex {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 */
	public KBase_DbConnEx(String errMsg, Object obj) {
		super(0, "", errMsg, obj);
	}
}
