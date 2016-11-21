package com.rjxx.taxeasy.domains;

import java.util.Date;
import javax.persistence.*;
import org.hibernate.annotations.GenericGenerator;
import com.rjxx.comm.json.JsonDateFormat;
import com.rjxx.comm.json.JsonDatetimeFormat;
import org.springframework.format.annotation.DateTimeFormat;
import java.io.Serializable;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
/**
 * t_tqjl 实体类
 * 由GenEntityMysql类自动生成
 * Thu Nov 03 14:26:26 CST 2016
 * @ZhangBing
 */ 
@Entity
@Table(name="t_tqjl")
public class Tqjl  implements Serializable {

@Id
@GeneratedValue(strategy=GenerationType.IDENTITY)
	protected Integer id;

@Column(name="djh")
	protected String djh;

@Column(name="tqsj")
@JsonSerialize(using = JsonDatetimeFormat.class)
@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	protected Date tqsj;

@Column(name="ip")
	protected String ip;
@Column(name="jlly")
protected String jlly;
/**
 * 浏览器信息
 */ 
@Column(name="llqxx")
	protected String llqxx;


	public Integer getId(){
		return id;
	}

	public void setId(Integer id){
		this.id=id;
	}

	public String getDjh(){
		return djh;
	}

	public void setDjh(String djh){
		this.djh=djh;
	}

	public Date getTqsj(){
		return tqsj;
	}

	public void setTqsj(Date tqsj){
		this.tqsj=tqsj;
	}

	public String getIp(){
		return ip;
	}

	public void setIp(String ip){
		this.ip=ip;
	}

	public String getLlqxx(){
		return llqxx;
	}

	public void setLlqxx(String llqxx){
		this.llqxx=llqxx;
	}

	public String getJlly() {
		return jlly;
	}

	public void setJlly(String jlly) {
		this.jlly = jlly;
	}

}

