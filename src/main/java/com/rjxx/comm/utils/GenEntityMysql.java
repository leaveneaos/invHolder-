package com.rjxx.comm.utils;


import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.*;
import java.util.Date;

/**
 * 从数据库表反射出实体类，自动生成实体类
 *
 * @author ZhangBing
 */
public class GenEntityMysql {

    private String packageOutPath = "com.rjxx.taxeasy.domains";//指定实体生成所在包的路径
    private String authorName = "ZhangBing";//作者名字
    private String tablename = "t_zzfpqp";//表名
    private String tableComment = null;//表注释
    private String javaFilePath = "./";
    private String[] colnames; // 列名数组
    private String[] filedNames;//属性名，遵守驼峰规则
    private Map<String, String> columnCommentMap = new HashMap<String, String>();
    private String[] colTypes; //列名类型数组
    private int[] colSizes; //列名大小数组
    private boolean f_util = false; // 是否需要导入包java.util.*
    private boolean f_sql = false; // 是否需要导入包java.sql.*
    private boolean f_jpa = true; // 是否需要生成基于注解的JPA实体对象

    //数据库连接
    private static final String URL = "jdbc:mysql://192.168.1.200:3306/dzfp-dev";
    private static final String NAME = "dzfp-dev";
    private static final String PASS = "dzfpdev1234";
    private static final String DRIVER = "com.mysql.jdbc.Driver";

    /*
     * 构造函数
     */
    public GenEntityMysql() throws Exception {
        List<String> list = null;
        if (StringUtils.isBlank(tablename)) {
            list = getTableName();
        } else {
            list = new ArrayList<String>() {{
                add(tablename);
            }};
        }

        for (int p = 0; p < list.size(); p++) {
            tablename = list.get(p);
            //创建连接
            Connection con;
            //查要生成实体类的表
            String sql = "select * from " + tablename;
            PreparedStatement pStemt = null;
            try {
                try {
                    Class.forName(DRIVER);
                } catch (ClassNotFoundException e1) {
                    e1.printStackTrace();
                }
                con = DriverManager.getConnection(URL, NAME, PASS);
                getTableComment(con);
                getTableColumnComment(con);
                pStemt = con.prepareStatement(sql);
                ResultSetMetaData rsmd = pStemt.getMetaData();
                int size = rsmd.getColumnCount();    //统计列
                colnames = new String[size];
                filedNames = new String[size];
                colTypes = new String[size];
                colSizes = new int[size];
                for (int i = 0; i < size; i++) {
                    colnames[i] = rsmd.getColumnName(i + 1).toLowerCase();//小写
                    filedNames[i] = StringUtils.underlineToCamel2(colnames[i]);//转换成属性
                    colTypes[i] = rsmd.getColumnTypeName(i + 1);

                    if (colTypes[i].equalsIgnoreCase("datetime") || colTypes[i].equalsIgnoreCase("timestamp")) {
                        f_util = true;
                    }
                    if (colTypes[i].equalsIgnoreCase("image") || colTypes[i].equalsIgnoreCase("text")) {
                        f_sql = true;
                    }
                    colSizes[i] = rsmd.getColumnDisplaySize(i + 1);
                }

                String content = parse(colnames, colTypes, colSizes);

                try {
                    String outputPath = javaFilePath + "/src/main/java/" + this.packageOutPath.replace(".", "/") + "/" + generateClassName() + ".java";
                    FileWriter fw = new FileWriter(outputPath);
                    PrintWriter pw = new PrintWriter(fw);
                    pw.println(content);
                    pw.flush();
                    pw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } catch (SQLException e) {
                e.printStackTrace();
            } finally {

            }
        }
        System.out.println("生成完毕！");
    }

    private String generateClassName() {
        String className = tablename.toLowerCase();
        if (className.startsWith("t_")) {
            className = className.substring(2);
        }
        return initcap(StringUtils.underlineToCamel2(className));
    }

    /**
     * Java方法 得到当前数据库下所有的表名
     */
    private List<String> getTableName() {
        List<String> list = new ArrayList<String>();
        try {
            DatabaseMetaData meta = DriverManager.getConnection(URL, NAME, PASS).getMetaData();
            ResultSet rs = meta.getTables(null, null, null, new String[]{"TABLE"});
            while (rs.next()) {
                list.add(rs.getString(3));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 获取表的注释
     *
     * @param con
     * @throws Exception
     */
    private void getTableComment(Connection con) throws Exception {
        String sql = "show table status where name = '" + tablename + "'";
        ResultSet resultSet = con.createStatement().executeQuery(sql);
        if (resultSet.next()) {
            tableComment = resultSet.getString("Comment");
        }
    }

    /**
     * 获取表的注释
     *
     * @param con
     * @throws Exception
     */
    private void getTableColumnComment(Connection con) throws Exception {
        String sql = "show full fields from " + tablename;
        ResultSet resultSet = con.createStatement().executeQuery(sql);
        while (resultSet.next()) {
            columnCommentMap.put(resultSet.getString("Field").toLowerCase(), resultSet.getString("Comment"));
        }
    }

    /**
     * 功能：生成实体类主体代码
     *
     * @param colnames
     * @param colTypes
     * @param colSizes
     * @return
     */
    private String parse(String[] colnames, String[] colTypes, int[] colSizes) {
        StringBuffer sb = new StringBuffer();
        sb.append("package " + this.packageOutPath + ";\r\n");
        sb.append("\r\n");

        //判断是否导入工具包
        if (f_util) {
            sb.append("import java.util.Date;\r\n");
        }
        if (f_sql) {
            sb.append("import java.sql.*;\r\n");
        }

        //jpa
        if (f_jpa) {
            sb.append("import javax.persistence.*;\r\n");
            sb.append("import org.hibernate.annotations.GenericGenerator;\r\n");
            sb.append("import com.rjxx.comm.json.JsonDateFormat;\r\n" +
                    "import com.rjxx.comm.json.JsonDatetimeFormat;\r\n" +
                    "import org.springframework.format.annotation.DateTimeFormat;\r\n" +
                    "import java.io.Serializable;\r\n" +
                    "import com.fasterxml.jackson.databind.annotation.JsonSerialize;\r\n");
        }

        //注释部分
        sb.append("/**\r\n");
        sb.append(" * " + tablename + " 实体类\r\n");
        if (StringUtils.isNotBlank(tableComment)) {
            sb.append(" * " + tableComment + "\r\n");
        }
        //GenEntityMysql
        sb.append(" * 由GenEntityMysql类自动生成\r\n");
        sb.append(" * " + new Date() + "\r\n");
        sb.append(" * @" + this.authorName + "\r\n");
        sb.append(" */ \r\n");

        if (f_jpa) {
            sb.append("@Entity\r\n");
            sb.append("@Table(name=\"" + tablename + "\")\r\n");
        }
        //实体部分
        sb.append("public class " + generateClassName() + "  implements Serializable {\r\n\r\n");
        processAllAttrs(sb);//属性
        processAllMethod(sb);//get set方法
        sb.append("}\r\n");

        //System.out.println(sb.toString());
        return sb.toString();
    }

    /**
     * 功能：生成所有属性
     *
     * @param sb
     */
    private void processAllAttrs(StringBuffer sb) {
        for (int i = 0; i < colnames.length; i++) {
            String comment = columnCommentMap.get(colnames[i]);
            if (StringUtils.isNotBlank(comment)) {
                sb.append("/**\r\n");
                sb.append(" * " + comment + "\r\n");
                sb.append(" */ \r\n");
            }
            if (filedNames[i].equals("id")) {
                sb.append("@Id\r\n" +
                        "@GeneratedValue(strategy=GenerationType.IDENTITY)\r\n");
            } else {
                sb.append("@Column(name=\"" + colnames[i] + "\")\r\n");
            }
            if (colTypes[i].equalsIgnoreCase("date")) {
                sb.append("@JsonSerialize(using = JsonDateFormat.class)\r\n");
                sb.append("@DateTimeFormat(pattern=\"yyyy-MM-dd\")\r\n");
            } else if (colTypes[i].equalsIgnoreCase("datetime") || colTypes[i].equalsIgnoreCase("timestamp")) {
                sb.append("@JsonSerialize(using = JsonDatetimeFormat.class)\r\n");
                sb.append("@DateTimeFormat(pattern=\"yyyy-MM-dd HH:mm:ss\")\r\n");
            }
            sb.append("\tprotected " + sqlType2JavaType(colTypes[i]) + " " + filedNames[i] + ";\r\n\r\n");
        }
        sb.append("\r\n");
    }

    /**
     * 功能：生成所有方法
     *
     * @param sb
     */
    private void processAllMethod(StringBuffer sb) {

        for (int i = 0; i < colnames.length; i++) {
            sb.append("\tpublic " + sqlType2JavaType(colTypes[i]) + " get" + initcap(filedNames[i]) + "(){\r\n");
            sb.append("\t\treturn " + filedNames[i] + ";\r\n");
            sb.append("\t}\r\n\r\n");
            sb.append("\tpublic void set" + initcap(filedNames[i]) + "(" + sqlType2JavaType(colTypes[i]) + " " + filedNames[i] + "){\r\n");
            sb.append("\t\tthis." + filedNames[i] + "=" + filedNames[i] + ";\r\n");
            sb.append("\t}\r\n\r\n");
        }

    }

    /**
     * 将输入字符串的首字母改成大写
     *
     * @param str
     * @return
     */
    private String initcap(String str) {
        //第2个字母就是大写字母,第1个字母是小写字母，则直接返回原字符串
        if (str.length() > 1 && Character.isUpperCase(str.charAt(1)) && Character.isLowerCase(str.charAt(0))) {
            return str;
        }
        char[] ch = str.toCharArray();
        if (ch[0] >= 'a' && ch[0] <= 'z') {
            ch[0] = (char) (ch[0] - 32);
        }
        return new String(ch);
    }

    /**
     * 功能：获得列的数据类型
     *
     * @param sqlType
     * @return
     */
    private String sqlType2JavaType(String sqlType) {

        if (sqlType.equalsIgnoreCase("bit")) {
            return "Boolean";
        } else if (sqlType.equalsIgnoreCase("tinyint")) {
            return "Integer";
        } else if (sqlType.equalsIgnoreCase("smallint")) {
            return "Integer";
        } else if (sqlType.equalsIgnoreCase("int") || sqlType.equalsIgnoreCase("INT UNSIGNED")) {
            //INT UNSIGNED无符号整形
            return "Integer";
        } else if (sqlType.equalsIgnoreCase("bigint")) {
            return "Long";
        } else if (sqlType.equalsIgnoreCase("float")) {
            return "Double";
        } else if (sqlType.equalsIgnoreCase("decimal") || sqlType.equalsIgnoreCase("numeric")
                || sqlType.equalsIgnoreCase("real") || sqlType.equalsIgnoreCase("money")
                || sqlType.equalsIgnoreCase("smallmoney") || sqlType.equalsIgnoreCase("double")) {
            return "Double";
        } else if (sqlType.equalsIgnoreCase("varchar") || sqlType.equalsIgnoreCase("char")
                || sqlType.equalsIgnoreCase("nvarchar") || sqlType.equalsIgnoreCase("nchar")
                || sqlType.equalsIgnoreCase("text")) {
            return "String";
        } else if (sqlType.equalsIgnoreCase("datetime") || sqlType.equalsIgnoreCase("date") || sqlType.equalsIgnoreCase("timestamp")) {
            return "Date";
        } else if (sqlType.equalsIgnoreCase("image")) {
            return "Blod";
        } else if (sqlType.equalsIgnoreCase("binary") || sqlType.equalsIgnoreCase("varbinary")) {
            return "byte[]";
        }
        return null;
    }

    /**
     * 出口
     * TODO
     *
     * @param args
     */
    public static void main(String[] args) throws Exception {
        new GenEntityMysql();
    }
}