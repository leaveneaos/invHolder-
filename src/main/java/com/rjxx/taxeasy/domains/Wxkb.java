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
 * t_wxkb 实体类
 * 由GenEntityMysql类自动生成
 * Tue Oct 25 17:47:44 CST 2016
 * @ZhangBing
 */ 
@Entity
@Table(name="t_wxkb")
public class Wxkb  implements Serializable {

@Id
@GeneratedValue(strategy=GenerationType.IDENTITY)
	protected Integer id;

@Column(name="access_token")
	protected String accessToken;

@Column(name="scsj")
@JsonSerialize(using = JsonDatetimeFormat.class)
@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	protected Date scsj;

@Column(name="expires_in")
	protected String expiresIn;


	public Integer getId(){
		return id;
	}

	public void setId(Integer id){
		this.id=id;
	}

	public String getAccessToken(){
		return accessToken;
	}

	public void setAccessToken(String accessToken){
		this.accessToken=accessToken;
	}

	public Date getScsj(){
		return scsj;
	}

	public void setScsj(Date scsj){
		this.scsj=scsj;
	}

	public String getExpiresIn(){
		return expiresIn;
	}

	public void setExpiresIn(String expiresIn){
		this.expiresIn=expiresIn;
	}

}

