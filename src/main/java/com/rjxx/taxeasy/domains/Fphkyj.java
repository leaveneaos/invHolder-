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
 * t_fphkyj 实体类
 * 由GenEntityMysql类自动生成
 * Thu Oct 20 13:06:18 CST 2016
 * @ZhangBing
 */ 
@Entity
@Table(name="t_fphkyj")
public class Fphkyj  implements Serializable {

@Id
@GeneratedValue(strategy=GenerationType.IDENTITY)
	protected Integer id;

@Column(name="sjrxm")
	protected String sjrxm;

@Column(name="dwmc")
	protected String dwmc;

@Column(name="sjdz")
	protected String sjdz;

@Column(name="yhm")
	protected String yhm;

@Column(name="lxdh")
	protected String lxdh;

@Column(name="kplsh")
	protected String kplsh;

@Column(name="djh")
	protected String djh;

@Column(name="yxbz")
	protected String yxbz;

@Column(name="lrsj")
@JsonSerialize(using = JsonDatetimeFormat.class)
@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	protected Date lrsj;

@Column(name="xgsj")
@JsonSerialize(using = JsonDatetimeFormat.class)
@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	protected Date xgsj;

@Column(name="kprq")
@JsonSerialize(using = JsonDatetimeFormat.class)
@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	protected Date kprq;

@Column(name="xfid")
	protected Integer xfid;

@Column(name="ddh")
	protected String ddh;

@Column(name="fpdm")
	protected String fpdm;

@Column(name="fphm")
	protected String fphm;

@Column(name="gfmc")
	protected String gfmc;

@Column(name="sfyj")
	protected String sfyj;


	public Integer getId(){
		return id;
	}

	public void setId(Integer id){
		this.id=id;
	}

	public String getSjrxm(){
		return sjrxm;
	}

	public void setSjrxm(String sjrxm){
		this.sjrxm=sjrxm;
	}

	public String getDwmc(){
		return dwmc;
	}

	public void setDwmc(String dwmc){
		this.dwmc=dwmc;
	}

	public String getSjdz(){
		return sjdz;
	}

	public void setSjdz(String sjdz){
		this.sjdz=sjdz;
	}

	public String getYhm(){
		return yhm;
	}

	public void setYhm(String yhm){
		this.yhm=yhm;
	}

	public String getLxdh(){
		return lxdh;
	}

	public void setLxdh(String lxdh){
		this.lxdh=lxdh;
	}

	public String getKplsh(){
		return kplsh;
	}

	public void setKplsh(String kplsh){
		this.kplsh=kplsh;
	}

	public String getDjh(){
		return djh;
	}

	public void setDjh(String djh){
		this.djh=djh;
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

	public Date getXgsj(){
		return xgsj;
	}

	public void setXgsj(Date xgsj){
		this.xgsj=xgsj;
	}

	public Integer getXfid(){
		return xfid;
	}

	public void setXfid(Integer xfid){
		this.xfid=xfid;
	}

	public String getDdh(){
		return ddh;
	}

	public void setDdh(String ddh){
		this.ddh=ddh;
	}

	public String getFpdm(){
		return fpdm;
	}

	public void setFpdm(String fpdm){
		this.fpdm=fpdm;
	}

	public String getFphm(){
		return fphm;
	}

	public void setFphm(String fphm){
		this.fphm=fphm;
	}

	public String getGfmc(){
		return gfmc;
	}

	public void setGfmc(String gfmc){
		this.gfmc=gfmc;
	}

	public String getSfyj(){
		return sfyj;
	}

	public void setSfyj(String sfyj){
		this.sfyj=sfyj;
	}

	public Date getKprq() {
		return kprq;
	}

	public void setKprq(Date kprq) {
		this.kprq = kprq;
	}

}

