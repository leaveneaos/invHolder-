package com.rjxx.taxeasy.domains;

import javax.persistence.*;
import org.hibernate.annotations.GenericGenerator;
import com.rjxx.comm.json.JsonDateFormat;
import com.rjxx.comm.json.JsonDatetimeFormat;
import org.springframework.format.annotation.DateTimeFormat;
import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
/**
 * t_ckhk 实体类
 * 由GenEntityMysql类自动生成
 * Thu Nov 03 14:28:48 GMT+08:00 2016
 * @ZhangBing
 */ 
@Entity
@Table(name="t_ckhk")
public class Ckhk  implements Serializable {

/**
 * 主键id
 */ 
@Id
@GeneratedValue(strategy=GenerationType.IDENTITY)
	protected Integer id;

/**
 * 开票流水号
 */ 
@Column(name="djh")
	protected Integer djh;

/**
 * 微信id
 */ 
@Column(name="unionid")
	protected String unionid;

/**
 * 发票抬头
 */ 
@Column(name="gfmc")
	protected String gfmc;

/**
 * 税号
 */ 
@Column(name="gfsh")
	protected String gfsh;

/**
 * 地址
 */ 
@Column(name="gfdz")
	protected String gfdz;

/**
 * 电话
 */ 
@Column(name="gfdh")
	protected String gfdh;

/**
 * 银行
 */ 
@Column(name="gfyh")
	protected String gfyh;

/**
 * 银行账号
 */ 
@Column(name="gfyhzh")
	protected String gfyhzh;

/**
 * 邮箱
 */ 
@Column(name="gfyx")
	protected String gfyx;

/**
 * 手机
 */ 
@Column(name="gfsj")
	protected String gfsj;

/**
 * 有效标志：0，无效；1，有效
 */ 
@Column(name="yxbz")
	protected String yxbz;

/**
 * 状态标志：0，重开待审；1，重开审核通过；2，重开审核未通过；3，换开待审；4，换开审核通过；5，换开审核未通过
 */ 
@Column(name="ztbz")
	protected String ztbz;

/**
 * 重开或换开原因
 */ 
@Column(name="ckhkyy")
	protected String ckhkyy;

/**
 * 未通过原因
 */ 
@Column(name="ckbtgyy")
	protected String ckbtgyy;

/**
 * 申请时间
 */ 
@Column(name="sqsj")
	protected Date sqsj;


	public Integer getId(){
		return id;
	}

	public void setId(Integer id){
		this.id=id;
	}

	public Integer getDjh() {
		return djh;
	}

	public void setDjh(Integer djh) {
		this.djh = djh;
	}

	public String getUnionid(){
		return unionid;
	}

	public void setUnionid(String unionid){
		this.unionid=unionid;
	}

	public String getGfmc(){
		return gfmc;
	}

	public void setGfmc(String gfmc){
		this.gfmc=gfmc;
	}

	public String getGfsh(){
		return gfsh;
	}

	public void setGfsh(String gfsh){
		this.gfsh=gfsh;
	}

	public String getGfdz(){
		return gfdz;
	}

	public void setGfdz(String gfdz){
		this.gfdz=gfdz;
	}

	public String getGfdh(){
		return gfdh;
	}

	public void setGfdh(String gfdh){
		this.gfdh=gfdh;
	}

	public String getGfyh(){
		return gfyh;
	}

	public void setGfyh(String gfyh){
		this.gfyh=gfyh;
	}

	public String getGfyhzh(){
		return gfyhzh;
	}

	public void setGfyhzh(String gfyhzh){
		this.gfyhzh=gfyhzh;
	}

	public String getYxbz(){
		return yxbz;
	}

	public void setYxbz(String yxbz){
		this.yxbz=yxbz;
	}

	public String getZtbz(){
		return ztbz;
	}

	public void setZtbz(String ztbz){
		this.ztbz=ztbz;
	}

	public String getCkhkyy(){
		return ckhkyy;
	}

	public void setCkhkyy(String ckhkyy){
		this.ckhkyy=ckhkyy;
	}

	public String getCkbtgyy(){
		return ckbtgyy;
	}

	public void setCkbtgyy(String ckbtgyy){
		this.ckbtgyy=ckbtgyy;
	}

	public String getGfyx() {
		return gfyx;
	}

	public void setGfyx(String gfyx) {
		this.gfyx = gfyx;
	}

	public String getGfsj() {
		return gfsj;
	}

	public void setGfsj(String gfsj) {
		this.gfsj = gfsj;
	}

	public Date getSqsj() {
		return sqsj;
	}

	public void setSqsj(Date sqsj) {
		this.sqsj = sqsj;
	}

}

