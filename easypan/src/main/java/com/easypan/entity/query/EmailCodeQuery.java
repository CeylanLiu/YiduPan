package com.easypan.entity.query;

import java.util.Date;


/**
 * 邮箱验证码参数
 */
public class EmailCodeQuery extends BaseParam {


	/**
	 * 邮箱
	 */
	private String email;

	private String emailFuzzy;

	/**
	 * 编号
	 */
	private String code;

	private String codeFuzzy;

	/**
	 * 创建时间
	 */
	private String createTime;

	private String createTimeStart;

	private String createTimeEnd;

	/**
	 * 0:未使用 1:已使用
	 */
	private Integer status;


	public void setEmail(String email){
		this.email = email;
	}

	public String getEmail(){
		return this.email;
	}

	public void setEmailFuzzy(String emailFuzzy){
		this.emailFuzzy = emailFuzzy;
	}

	public String getEmailFuzzy(){
		return this.emailFuzzy;
	}

	public void setCode(String code){
		this.code = code;
	}

	public String getCode(){
		return this.code;
	}

	public void setCodeFuzzy(String codeFuzzy){
		this.codeFuzzy = codeFuzzy;
	}

	public String getCodeFuzzy(){
		return this.codeFuzzy;
	}

	public void setcreateTime(String createTime){
		this.createTime = createTime;
	}

	public String getcreateTime(){
		return this.createTime;
	}

	public void setcreateTimeStart(String createTimeStart){
		this.createTimeStart = createTimeStart;
	}

	public String getcreateTimeStart(){
		return this.createTimeStart;
	}
	public void setcreateTimeEnd(String createTimeEnd){
		this.createTimeEnd = createTimeEnd;
	}

	public String getcreateTimeEnd(){
		return this.createTimeEnd;
	}

	public void setStatus(Integer status){
		this.status = status;
	}

	public Integer getStatus(){
		return this.status;
	}

}
