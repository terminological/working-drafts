package uk.co.terminological.ctakes;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import uk.co.terminological.javapig.sqlloader.Column;

public class ProxyResultSetMapper {

	public static <T extends Object> Iterator<T> iterator(final ResultSet av, Class<T> type) {
		return new Iterator<T>() {
			boolean started = false;
			@Override
			public boolean hasNext() {
				try {
					return !av.isLast();
				} catch (SQLException e) {
					return false;
				}
			}

			@Override
			public T next() {
				if (!hasNext()) throw new NoSuchElementException();
				try {
					if (started) av.next();
					started = true;
					return read(av,type);
				} catch (SQLException e) {
					throw new NoSuchElementException(e.getMessage());
				}
			}
		};
	}
	
	public static <T extends Object> Stream<T> stream(final ResultSet av, Class<T> type) {
		Iterable<T> iterable = () -> iterator(av,type);
		return StreamSupport.stream(iterable.spliterator(), false);
	}
	
	
	@SuppressWarnings("unchecked")
	public static <T extends Object> T read(final ResultSet av, Class<T> type) throws SQLException {
		
		final Map<String,Object> methodMap = new HashMap<>();
		for (Method m: type.getMethods()) {
			Column c = m.getAnnotation(Column.class);
			methodMap.put(m.getName(), av.getObject(c.name(),m.getReturnType()));
		}
		
		//TODO: https://jrebel.com/rebellabs/recognize-and-conquer-java-proxies-default-methods-and-method-handles/
		return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class[] {type}, new InvocationHandler() {
			
			@Override
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				if (methodMap.containsKey(method.getName())) {
					return methodMap.get(method.getName());
				} else {
					if (method.getName().equals("toString")) return "proxy class of "+type.getCanonicalName();
					if (method.getName().equals("equals")) return new Exception("equals not implemented");
					if (method.getName().equals("hashcode")) throw new Exception("hashcode not implemented");
					throw new Exception("unknown method type: "+method.getName());
				}
			}
		});
	}
	
	public static long write(Object o, String table, Connection conn) {
		
        List<Object> values = new ArrayList<>();
        List<String> fields = new ArrayList<>();
        StringBuilder fieldString = new StringBuilder();
        StringBuilder valueString = new StringBuilder();
        
		for (Method m: o.getClass().getMethods()) {
			Column c = m.getAnnotation(Column.class);
			if (c == null) throw new RuntimeException(o.getClass().getCanonicalName()+" not properly annotated with @Column");
			try {
				values.add(m.invoke(o));
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				throw new RuntimeException("couldn't read value",e);
			}
			fields.add(c.name());
			if (fieldString.length()>0) fieldString.append(",");
			fieldString.append(c.name());
			if (valueString.length()>0) fieldString.append(",");
			valueString.append("?");
		}
        
		String SQL = "INSERT INTO "+table+" ("+fieldString.toString()+") "
                + "VALUES("+valueString.toString()+")";
 		
        long id = 0;
 
        try (PreparedStatement pstmt = conn.prepareStatement(SQL,
                Statement.RETURN_GENERATED_KEYS)) {
        	pstmt.
            for (int i=0; i<values.size();i++) {
            	pstmt.setObject(i+1, values.get(i));
            }
 
            int affectedRows = pstmt.executeUpdate();
            // check the affected rows 
            if (affectedRows > 0) {
                // get the ID back
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        id = rs.getLong(1);
                    }
                } catch (SQLException ex) {
                    System.out.println(ex.getMessage());
                }
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        return id;
    }
}
