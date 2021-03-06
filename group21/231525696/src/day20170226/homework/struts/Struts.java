package day20170226.homework.struts;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import common.BeanHelper;

/**
 * 0. 读取配置文件struts.xml
 *	
 *	1. 根据actionName找到相对应的class ， 例如LoginAction,   通过反射实例化（创建对象）
 *	        据parameters中的数据，调用对象的setter方法， 例如parameters中的数据是 
 *	    ("name"="test" ,  "password"="1234") ,     	
 *	        那就应该调用 setName和setPassword方法
 *
 * 	2. 通过反射调用对象的exectue 方法， 并获得返回值，例如"success"
 *
 *	3. 通过反射找到对象的所有getter方法（例如 getMessage）,  
 *	        通过反射来调用， 把值和属性形成一个HashMap , 例如 {"message":  "登录成功"} ,  
 *	        放到View对象的parameters
 *
 *	4. 根据struts.xml中的 <result> 配置,以及execute的返回值，  确定哪一个jsp，  
 *     放到View对象的jsp字段中。
 *     
 *    @author RedKnife on 2017-03-05 15:22:23 
*/
public class Struts {
	
	public static View runAction(String actionName, Map<String,String> parameters) {
		
		View view = new View();
		InputStream is = Struts.class.getResourceAsStream("/day20170226/homework/struts/struts.xml");
		SAXReader reader = new SAXReader(); 
		Document doc;
		
		try {
			doc = reader.read(is);
			Element root = doc.getRootElement(); 
			//action node
			Element actionNode = getNode("action", "name", actionName, root); 
			if (actionNode == null) {
				throw new RuntimeException("do not have this action: " + actionName);
			}
			//get Class
			Class<?> c = Class.forName(actionNode.attributeValue("class"));
			//invoke by map's value
			Object ob = BeanHelper.invokeClassFromMap(c, parameters);
			//resultNode's name
			String resultName = String.valueOf(BeanHelper.excuteBeanMethod(ob, "execute", null));
			
			Element resultNode = getNode("result", "name", resultName, actionNode); 
			
			//package view
			view.setJsp(resultNode.getTextTrim());
			view.setParameters(BeanHelper.getBeanFields(ob));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (DocumentException e) {
			e.printStackTrace();
		} 
		
    	return view;
    }    
	
	/**
	 * Find the 'eleName' node from the parent and attribute's value = value  
	 * @param eleName   node's name
	 * @param attrName  node's attribute name
	 * @param value		attribute's value
	 * @param parent	parent node
	 * @return
	 */
	public static Element getNode(String eleName, String attrName, String value, Element parent){
		List<Element> actions = parent.elements(eleName);
		for(Element activeNode : actions){
			if(activeNode.attributeValue(attrName).equals(value)){
				return activeNode;
			}
		}
		return null;
	}
}
