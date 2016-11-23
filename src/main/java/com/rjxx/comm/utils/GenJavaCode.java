package com.rjxx.comm.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

/**
 * Created by Administrator on 2016/10/10.
 */
public class GenJavaCode {

    public static void main(String[] args) {
        new GenJavaCode();
    }

    private String javaFilePath = "./";
    private String authorName = "ZhangBing";
    String baseOutputFiePath = javaFilePath + "/src/main/java/";

    private String fullDomainClassName = "com.rjxx.taxeasy.domains.Zzfpqp";
    private String tableName = "t_zzfpqp";
    private String simpleDomainClassName = null;
    private String domainParamsName = null;
    private String servicePackageName = "com.rjxx.taxeasy.service";
    private String daoPackageName = "com.rjxx.taxeasy.dao";

    private String jpaJavaFilePath = null;
    private String jpaJavaFullClassName = null;
    private String mapperJavaFilePath = null;
    private String mapperJavaFullClassName = null;
    private String mapperXmlFilePath = null;
    private String serviceJavaFilePath = null;

    private StringBuilder jpaJavaStringBuilder = new StringBuilder();
    private StringBuilder mapperJavaStringBuilder = new StringBuilder();
    private StringBuilder mapperXmlStringBuilder = new StringBuilder();
    private StringBuilder serviceJavaStringBuilder = new StringBuilder();


    public GenJavaCode() {
        int pos = fullDomainClassName.lastIndexOf(".");
        simpleDomainClassName = fullDomainClassName.substring(pos + 1);
        domainParamsName = lowCapFirstChar(simpleDomainClassName);
        jpaJavaFilePath = baseOutputFiePath + daoPackageName.replace(".", "/") + "/" + simpleDomainClassName + "JpaDao.java";
        jpaJavaFullClassName = daoPackageName + "." + simpleDomainClassName + "JpaDao";
        mapperJavaFilePath = baseOutputFiePath + daoPackageName.replace(".", "/") + "/" + simpleDomainClassName + "Mapper.java";
        mapperJavaFullClassName = daoPackageName + "." + simpleDomainClassName + "Mapper";
        mapperXmlFilePath = baseOutputFiePath + daoPackageName.replace(".", "/") + "/" + simpleDomainClassName + "Mapper.xml";
        serviceJavaFilePath = baseOutputFiePath + servicePackageName.replace(".", "/") + "/" + simpleDomainClassName + "Service.java";

        buildJpaJavaContent();
        buildMapperJavaContent();
        buildMapperXmlContent();
        buildServiceJavaContent();
        try {
            FileWriter fw = new FileWriter(jpaJavaFilePath);
            PrintWriter pw = new PrintWriter(fw);
            pw.println(jpaJavaStringBuilder.toString());
            pw.flush();
            pw.close();
            fw = new FileWriter(mapperJavaFilePath);
            pw = new PrintWriter(fw);
            pw.println(mapperJavaStringBuilder.toString());
            pw.flush();
            pw.close();
            fw = new FileWriter(mapperXmlFilePath);
            pw = new PrintWriter(fw);
            pw.println(mapperXmlStringBuilder.toString());
            pw.flush();
            pw.close();
            fw = new FileWriter(serviceJavaFilePath);
            pw = new PrintWriter(fw);
            pw.println(serviceJavaStringBuilder.toString());
            pw.flush();
            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 生成jpaDao文件内容
     */
    private void buildJpaJavaContent() {
        jpaJavaStringBuilder.append("package " + daoPackageName + ";\r\n");
        jpaJavaStringBuilder.append("\r\n");
        jpaJavaStringBuilder.append("import " + fullDomainClassName + ";\r\n");
        jpaJavaStringBuilder.append("import org.springframework.data.repository.CrudRepository;\r\n");
        jpaJavaStringBuilder.append("\r\n");
        //注释部分
        jpaJavaStringBuilder.append("/**\r\n");
        jpaJavaStringBuilder.append(" * 由GenJavaCode类自动生成\r\n");
        jpaJavaStringBuilder.append(" * <p>\r\n");
        jpaJavaStringBuilder.append(" * " + new Date() + "\r\n");
        jpaJavaStringBuilder.append(" *\r\n");
        jpaJavaStringBuilder.append(" * @" + this.authorName + "\r\n");
        jpaJavaStringBuilder.append(" */ \r\n");

        jpaJavaStringBuilder.append("public interface " + simpleDomainClassName + "JpaDao extends CrudRepository<" + simpleDomainClassName + ", Integer> {\r\n");
        jpaJavaStringBuilder.append("\r\n");
        jpaJavaStringBuilder.append("}\r\n");
    }

    /**
     * 生成Mapper的java文件
     */
    private void buildMapperJavaContent() {
        mapperJavaStringBuilder.append("package " + daoPackageName + ";\r\n");
        mapperJavaStringBuilder.append("\r\n");
        mapperJavaStringBuilder.append("import com.rjxx.comm.mybatis.MybatisRepository;\r\n");
        mapperJavaStringBuilder.append("import com.rjxx.comm.mybatis.Pagination;\r\n");
        mapperJavaStringBuilder.append("import com.rjxx.taxeasy.domains." + simpleDomainClassName + ";\r\n");
        mapperJavaStringBuilder.append("\r\n");
        mapperJavaStringBuilder.append("import java.util.List;\r\n");
        mapperJavaStringBuilder.append("import java.util.Map;\r\n");
        mapperJavaStringBuilder.append("\r\n");
        //注释部分
        mapperJavaStringBuilder.append("/**\r\n");
        mapperJavaStringBuilder.append(" * 由GenJavaCode类自动生成\r\n");
        mapperJavaStringBuilder.append(" * <p>\r\n");
        mapperJavaStringBuilder.append(" * " + new Date() + "\r\n");
        mapperJavaStringBuilder.append(" *\r\n");
        mapperJavaStringBuilder.append(" * @" + this.authorName + "\r\n");
        mapperJavaStringBuilder.append(" */ \r\n");

        mapperJavaStringBuilder.append("@MybatisRepository\r\n");
        mapperJavaStringBuilder.append("public interface " + simpleDomainClassName + "Mapper {\r\n");
        mapperJavaStringBuilder.append("\r\n");
        mapperJavaStringBuilder.append("    public " + simpleDomainClassName + " findOneByParams(Map params);\r\n");
        mapperJavaStringBuilder.append("\r\n");
        mapperJavaStringBuilder.append("    public List<" + simpleDomainClassName + "> findAllByParams(Map params);\r\n");
        mapperJavaStringBuilder.append("\r\n");
        mapperJavaStringBuilder.append("    public List<" + simpleDomainClassName + "> findByPage(Pagination pagination);\r\n");
        mapperJavaStringBuilder.append("\r\n");
        mapperJavaStringBuilder.append("}\r\n");
    }

    /**
     * 生成Mapper的xml文件
     */
    private void buildMapperXmlContent() {
        mapperXmlStringBuilder.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n");
        mapperXmlStringBuilder.append("<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\"\r\n");
        mapperXmlStringBuilder.append("        \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">\r\n");
        mapperXmlStringBuilder.append("<mapper namespace=\"" + mapperJavaFullClassName + "\">\r\n");
        mapperXmlStringBuilder.append("\r\n");
        mapperXmlStringBuilder.append("    <select id=\"findOneByParams\" parameterType=\"map\" resultType=\"" + domainParamsName + "\">\r\n");
        mapperXmlStringBuilder.append("        select * from " + tableName + " where yxbz = '1'\r\n");
        mapperXmlStringBuilder.append("        <if test=\"orderBy != null\">\r\n");
        mapperXmlStringBuilder.append("            order by #{orderBy}\r\n");
        mapperXmlStringBuilder.append("        </if>\r\n");
        mapperXmlStringBuilder.append("        limit 1\r\n");
        mapperXmlStringBuilder.append("    </select>\r\n");
        mapperXmlStringBuilder.append("\r\n");
        mapperXmlStringBuilder.append("    <select id=\"findAllByParams\" parameterType=\"map\" resultType=\"" + domainParamsName + "\">\r\n");
        mapperXmlStringBuilder.append("        select * from " + tableName + " where yxbz = '1'\r\n");
        mapperXmlStringBuilder.append("        <if test=\"orderBy != null\">\r\n");
        mapperXmlStringBuilder.append("            order by #{orderBy}\r\n");
        mapperXmlStringBuilder.append("        </if>\r\n");
        mapperXmlStringBuilder.append("    </select>\r\n");
        mapperXmlStringBuilder.append("\r\n");
        mapperXmlStringBuilder.append("    <select id=\"findByPage\" parameterType=\"pagination\" resultType=\"" + domainParamsName + "\">\r\n");
        mapperXmlStringBuilder.append("        select * from " + tableName + " where yxbz = '1'\r\n");
        mapperXmlStringBuilder.append("        <if test=\"params.orderBy != null\">\r\n");
        mapperXmlStringBuilder.append("            order by #{params.orderBy}\r\n");
        mapperXmlStringBuilder.append("        </if>\r\n");
        mapperXmlStringBuilder.append("    </select>\r\n");
        mapperXmlStringBuilder.append("\r\n");
        mapperXmlStringBuilder.append("</mapper>\r\n");
    }

    /**
     * 生成service的java文件
     */
    private void buildServiceJavaContent() {
        serviceJavaStringBuilder.append("package " + servicePackageName + ";\r\n");
        serviceJavaStringBuilder.append("\r\n");
        serviceJavaStringBuilder.append("import com.rjxx.comm.mybatis.Pagination;\r\n");
        serviceJavaStringBuilder.append("import " + jpaJavaFullClassName + ";\r\n");
        serviceJavaStringBuilder.append("import " + mapperJavaFullClassName + ";\r\n");
        serviceJavaStringBuilder.append("import " + fullDomainClassName + ";\r\n");
        serviceJavaStringBuilder.append("import org.springframework.beans.factory.annotation.Autowired;\r\n");
        serviceJavaStringBuilder.append("import org.springframework.stereotype.Service;\r\n");
        serviceJavaStringBuilder.append("\r\n");
        serviceJavaStringBuilder.append("import java.util.List;\r\n");
        serviceJavaStringBuilder.append("import java.util.Map;\r\n");
        serviceJavaStringBuilder.append("\r\n");

        //注释部分
        serviceJavaStringBuilder.append("/**\r\n");
        serviceJavaStringBuilder.append(" * 由GenJavaCode类自动生成\r\n");
        serviceJavaStringBuilder.append(" * <p>\r\n");
        serviceJavaStringBuilder.append(" * " + new Date() + "\r\n");
        serviceJavaStringBuilder.append(" *\r\n");
        serviceJavaStringBuilder.append(" * @" + this.authorName + "\r\n");
        serviceJavaStringBuilder.append(" */ \r\n");

        serviceJavaStringBuilder.append("@Service\r\n");
        serviceJavaStringBuilder.append("public class " + simpleDomainClassName + "Service {\r\n");
        serviceJavaStringBuilder.append("\r\n");
        serviceJavaStringBuilder.append("    @Autowired\r\n");
        serviceJavaStringBuilder.append("    private " + simpleDomainClassName + "JpaDao " + domainParamsName + "JpaDao;\r\n");
        serviceJavaStringBuilder.append("\r\n");
        serviceJavaStringBuilder.append("    @Autowired\r\n");
        serviceJavaStringBuilder.append("    private " + simpleDomainClassName + "Mapper " + domainParamsName + "Mapper;\r\n");
        serviceJavaStringBuilder.append("\r\n");
        serviceJavaStringBuilder.append("    public " + simpleDomainClassName + " findOne(int id) {\r\n");
        serviceJavaStringBuilder.append("        return " + domainParamsName + "JpaDao.findOne(id);\r\n");
        serviceJavaStringBuilder.append("    }\r\n");
        serviceJavaStringBuilder.append("\r\n");
        serviceJavaStringBuilder.append("    public void save(" + simpleDomainClassName + " " + domainParamsName + ") {\r\n");
        serviceJavaStringBuilder.append("        " + domainParamsName + "JpaDao.save(" + domainParamsName + ");\r\n");
        serviceJavaStringBuilder.append("    }\r\n");
        serviceJavaStringBuilder.append("\r\n");
        serviceJavaStringBuilder.append("    public void save(List<" + simpleDomainClassName + "> " + domainParamsName + "List) {\r\n");
        serviceJavaStringBuilder.append("        " + domainParamsName + "JpaDao.save(" + domainParamsName + "List);\r\n");
        serviceJavaStringBuilder.append("    }\r\n");
        serviceJavaStringBuilder.append("\r\n");
        serviceJavaStringBuilder.append("    public " + simpleDomainClassName + " findOneByParams(Map params) {\r\n");
        serviceJavaStringBuilder.append("        return " + domainParamsName + "Mapper.findOneByParams(params);\r\n");
        serviceJavaStringBuilder.append("    }\r\n");
        serviceJavaStringBuilder.append("\r\n");
        serviceJavaStringBuilder.append("    public List<" + simpleDomainClassName + "> findAllByParams(Map params) {\r\n");
        serviceJavaStringBuilder.append("        return " + domainParamsName + "Mapper.findAllByParams(params);\r\n");
        serviceJavaStringBuilder.append("    }\r\n");
        serviceJavaStringBuilder.append("\r\n");
        serviceJavaStringBuilder.append("    public List<" + simpleDomainClassName + "> findByPage(Pagination pagination) {\r\n");
        serviceJavaStringBuilder.append("        return " + domainParamsName + "Mapper.findByPage(pagination);\r\n");
        serviceJavaStringBuilder.append("    }\r\n");
        serviceJavaStringBuilder.append("\r\n");
        serviceJavaStringBuilder.append("}\r\n");
    }

    private String lowCapFirstChar(String str) {
        return str.substring(0, 1).toLowerCase() + str.substring(1);
    }

}
