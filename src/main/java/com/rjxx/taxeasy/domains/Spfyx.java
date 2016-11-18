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
 * t_spfyx 实体类
 * 由GenEntityMysql类自动生成
 * Fri Nov 18 11:39:48 CST 2016
 * @ZhangBing
 */ 
@Entity
@Table(name="t_spfyx")
public class Spfyx  implements Serializable {

@Id
@GeneratedValue(strategy=GenerationType.IDENTITY)
	protected Integer id;

/**
 * 受票方id
 */ 
@Column(name="spfid")
	protected Integer spfid;

/**
 * 关联账号类型
 */ 
@Column(name="glzhlx")
	protected String glzhlx;

/**
 * 关联账号
 */ 
@Column(name="glzh")
	protected String glzh;

/**
 * 关联账号表id
 */ 
@Column(name="glid")
	protected Integer glid;

/**
 * 邮箱
 */ 
@Column(name="email")
	protected String email;

/**
 * 有效标志
 */ 
@Column(name="yxbz")
	protected String yxbz;

@Column(name="lrsj")
@JsonSerialize(using = JsonDatetimeFormat.class)
@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	protected Date lrsj;

/**
 * 单据号
 */ 
@Column(name="djh")
	protected Integer djh;


	public Integer getId(){
		return id;
	}

	public void setId(Integer id){
		this.id=id;
	}

	public Integer getSpfid(){
		return spfid;
	}

	public void setSpfid(Integer spfid){
		this.spfid=spfid;
	}

	public String getGlzhlx(){
		return glzhlx;
	}

	public void setGlzhlx(String glzhlx){
		this.glzhlx=glzhlx;
	}

	public String getGlzh(){
		return glzh;
	}

	public void setGlzh(String glzh){
		this.glzh=glzh;
	}

	public Integer getGlid(){
		return glid;
	}

	public void setGlid(Integer glid){
		this.glid=glid;
	}

	public String getEmail(){
		return email;
	}

	public void setEmail(String email){
		this.email=email;
	}

	public String getYxbz(){
		return yxbz;
	}

	public void setYxbz(String yxbz){
		this.yxbz=yxbz;
	}

	public Date getLrsj(){
		return lrsj;
	}

	public void setLrsj(Date lrsj){
		this.lrsj=lrsj;
	}

	public Integer getDjh(){
		return djh;
	}

	public void setDjh(Integer djh){
		this.djh=djh;
	}

}

