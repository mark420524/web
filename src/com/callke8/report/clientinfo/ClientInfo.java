package com.callke8.report.clientinfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.callke8.utils.ArrayUtils;
import com.callke8.utils.BlankUtils;
import com.callke8.utils.DateFormatUtils;
import com.callke8.utils.MemoryVariableUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
/**
 * 
 * 表结构
 * CREATE TABLE `client_info` (
  `CLIENT_NO` bigint(32) NOT NULL AUTO_INCREMENT,
  `CLIENT_NAME` varchar(32) DEFAULT NULL,
  `CLIENT_TELEPHONE` varchar(32) DEFAULT NULL,
  `CLIENT_TELEPHONE2` varchar(32) DEFAULT NULL,
  `CLIENT_LEVEL` varchar(1) DEFAULT NULL,
  `CLIENT_SEX` varchar(1) DEFAULT NULL,
  `CLIENT_QQ` varchar(32) DEFAULT NULL,
  `CLIENT_EMAIL` varchar(32) DEFAULT NULL,
  `CLIENT_COMPANY` varchar(64) DEFAULT NULL,
  `CLIENT_ADDRESS` varchar(64) DEFAULT NULL,
  `CREATE_TIME` datetime DEFAULT NULL,
  `VAR1` varchar(255) DEFAULT NULL,
  `VAR2` varchar(255) DEFAULT NULL,
  `VAR3` varchar(255) DEFAULT NULL,
  `VAR4` varchar(255) DEFAULT NULL,
  `VAR5` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`CLIENT_NO`)
) 
 * 
 * @author hwz
 *
 */
@SuppressWarnings("serial")
public class ClientInfo extends Model<ClientInfo> {
	public static ClientInfo dao = new ClientInfo();
	
	/**
	 * 根据客户的号码，查询客户的信息
	 * 
	 * @param tel
	 * @return
	 */
	public Record getClientInfoByTelephone(String tel) {
		
		String sql = "select * from client_info where CLIENT_TELEPHONE=? limit 1";
		
		Record record = Db.findFirst(sql,tel);
		
		return record;
	}
	
	
	/**
	 * 新增客户信息
	 * @param callTask
	 * @return
	 */
	public boolean add(Record clientInfo) {
		//对象为空时，返回false
		if(BlankUtils.isBlank(clientInfo) || BlankUtils.isBlankStr(clientInfo.get("CLIENT_TELEPHONE"))) {
			return false;
		}
		//在新增客户信息之前，先判断当前的号码,是否已经存在相同的记录
		Record r = getClientInfoByTelephone(clientInfo.get("CLIENT_TELEPHONE").toString());
		if(!BlankUtils.isBlank(r)) {
			return false;
		}
		
		clientInfo.set("CREATE_TIME",DateFormatUtils.getCurrentDate());
		
		boolean b = Db.save("client_info","CLIENT_NO",clientInfo);
		
		return b;
	}
	
	/**
	 * 根据客户的编码删除客户资料
	 * @param clientNo
	 * @return
	 */
	public boolean del(String clientNo) {
		boolean b = false;
		int count = 0;
		
		if(BlankUtils.isBlank(clientNo)) {
			return b;
		}
		
		String sql = "delete from client_info where CLIENT_NO=?";
		
		count = Db.update(sql, clientNo);
		
		if(count>0) {
			b = true;
		}
		
		return b;
	}
	
	
	/**
	 * 修改
	 * @param client
	 * @return
	 */
	public boolean update(ClientInfo client) {
		boolean b = false;
		int count = 0;
		
		String sql = "update client_info set CLIENT_NAME=?,CLIENT_TELEPHONE=?,CLIENT_TELEPHONE2=?,CLIENT_LEVEL=?,CLIENT_SEX=?,LOCATION=?,CLIENT_QQ=?,CLIENT_EMAIL=?,CLIENT_COMPANY=?,CLIENT_ADDRESS=? where CLIENT_NO=?";
		
		count = Db.update(sql,client.get("CLIENT_NAME"),client.get("CLIENT_TELEPHONE"),client.get("CLIENT_TELEPHONE2"),client.get("CLIENT_LEVEL"),client.get("CLIENT_SEX"),client.get("LOCATION"),client.get("CLIENT_QQ"),client.get("CLIENT_EMAIL"),client.get("CLIENT_COMPANY"),client.get("CLIENT_ADDRESS"),client.get("CLIENT_NO"));
		
		if(count>0) {
			b = true;
		}
		
		return b;
	}
	
	public Page<Record> getClientInfoByPaginate(int currentPage,int numPerPage,String clientName,String clientTelephone,String clientLevel,String clientSex,String startTime,String endTime) {
		StringBuilder sb = new StringBuilder();
		Object[] pars = new Object[7];
		int index = 0;
		
		sb.append("from client_info where 1=1");
		
		if(!BlankUtils.isBlank(clientName)) {
			sb.append(" and CLIENT_NAME like ?");
			pars[index] = "%" + clientName + "%";
			index++;
		}
		
		if(!BlankUtils.isBlank(clientTelephone)) {
			sb.append(" and CLIENT_TELEPHONE like ?");
			pars[index] = "%" + clientTelephone + "%";
			index++;
		}
		
		if(!BlankUtils.isBlank(clientLevel) && !clientLevel.equalsIgnoreCase("0")) {
			sb.append(" and CLIENT_LEVEL=?");
			pars[index] = clientLevel;
			index++;
		}
		
		if(!BlankUtils.isBlank(clientSex) && !clientSex.equalsIgnoreCase("2")) {
			sb.append(" and CLIENT_SEX=?");
			pars[index] = clientSex;
			index++;
		}
		
		if(!BlankUtils.isBlank(startTime)) {
			sb.append(" and CREATE_TIME>=?");
			pars[index] = startTime + " 00:00:00";
			index++;
		}
		
		if(!BlankUtils.isBlank(endTime)) {
			sb.append(" and CREATE_TIME<=?");
			pars[index] = endTime + " 23:59:59";
			index++;
		}
		
		
		Page<Record> page = Db.paginate(currentPage, numPerPage, "select *", sb.toString() + " ORDER BY CLIENT_NO DESC", ArrayUtils.copyArray(index, pars));
		
		return page;
	}
	
	
	@SuppressWarnings("unchecked")
	public Map getClientInfoByPaginateToMap(int currentPage,int numPerPage,String clientName,String clientTelephone,String clientLevel,String clientSex,String startTime,String endTime) {
		
		Map m = new HashMap();
		Page<Record> page = getClientInfoByPaginate(currentPage, numPerPage, clientName, clientTelephone, clientLevel, clientSex,startTime,endTime);
		
		//先取出 list,然后将用户级别赋入
		List<Record> newList = new ArrayList<Record>();
		List<Record> list = page.getList();
		
		for(Record record:list) {
			String clientLevelValue = record.get("CLIENT_LEVEL"); //先取出客户级别
			
			String dictName = MemoryVariableUtil.getDictName("CLIENT_LEVEL",clientLevelValue); 
			record.set("CLIENT_LEVEL_DESC", dictName);
			newList.add(record);
		}
		
		
		m.put("total", page.getTotalRow());
		m.put("rows", newList);
		
		return m;
	}
	
}
