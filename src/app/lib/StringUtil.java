package app.lib;

/**
 * Данный класс предназначен для работы со строками различного формата.
 * 
 * @author Igor Makarevich
 */
public class StringUtil {
	/**
	 * Возвращает id из указанной строки формата "Название (id)".
	 */
	public static long getIdFromComboName (String comboName) {
		long retVal = 0;
		int posBegin= comboName.lastIndexOf("(");
		int posEnd  = comboName.lastIndexOf(")");
		String strResult = comboName.substring(posBegin+1, posEnd);
		
		retVal = Long.parseLong(strResult);
		
		return retVal;
	}
}
