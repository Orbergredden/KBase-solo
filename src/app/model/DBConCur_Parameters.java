package app.model;

import app.db.DBMain;
import app.exceptions.KBase_DbConnEx;

/**
 * Класс активного соединения с БД
 * @author Igor Makarevich
 */
public class DBConCur_Parameters {
	/**
	 * Параметры подключения
	 */
	public DBConn_Parameters param;
	/**
	 * Id подключения. Нужен для идентификации в списке активных подключений
	 */
	public int Id;
	/**
	 * Обьект работы с БД
	 */
	public DBMain db;
	
	/**
	 * Конструктор
	 * @throws KBase_DbConnEx 
	 */
	public DBConCur_Parameters (DBConn_Parameters param, int Id) throws KBase_DbConnEx {
		String strConnParam;
		
		this.param = param;
		this.Id    = Id;
		
		// for example : "jdbc:postgresql://172.17.11.15:5432/kbase"
		strConnParam = "jdbc:"+ param.getType() +"://"+ param.getHost() +":"+ param.getPort() +"/"+ param.getName();
		//System.out.println(strConnParam);
		db = new DBMain(strConnParam, param.getLogin(), param.getPassword());
	}
}
