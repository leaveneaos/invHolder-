package com.rjxx.taxeasy.domains;

import javax.persistence.*;
import org.hibernate.annotations.GenericGenerator;
import com.rjxx.comm.json.JsonDateFormat;
import com.rjxx.comm.json.JsonDatetimeFormat;
import org.springframework.format.annotation.DateTimeFormat;
import java.io.Serializable;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
/**
 * t_gsxx 实体类
 * 由GenEntityMysql类自动生成
 * Thu Nov 03 17:18:34 CST 2016
 * @ZhangBing
 */ 
@Entity
@Table(name="t_gsxx")
public class Gsxx  implements Serializable {

/**
 * 公司代码
 */ 
	@Id
	protected String gsdm;

/**
 * 公司名称
 */ 
@Column(name="gsmc")
	protected String gsmc;

/**
 * 公司简称
 */ 
@Column(name="gsjc")
	protected String gsjc;

/**
 * 秘钥
 */ 
@Column(name="secret_key")
	protected String secretKey;


	public String getGsdm(){
		return gsdm;
	}

	public void setGsdm(String gsdm){
		this.gsdm=gsdm;
	}

	public String getGsmc(){
		return gsmc;
	}

	public void setGsmc(String gsmc){
		this.gsmc=gsmc;
	}

	public String getGsjc(){
		return gsjc;
	}

	public void setGsjc(String gsjc){
		this.gsjc=gsjc;
	}

	public String getSecretKey(){
		return secretKey;
	}

	public void setSecretKey(String secretKey){
		this.secretKey=secretKey;
	}

}

