var access_token// 两小时刷新一次,每天限制2000次
var expires_in;
var openid;
var refresh_token;

function init(){
	$.ajax({
		url : '../pjj/getOpenid',
		method : 'post',
		success : function(data) {
			if (data.success) {
				getJy();
			}else{
				window.location.href = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=wx9abc729e2b4637ee&redirect_uri=http://fpj.datarj.com/fp.html&response_type=code&scope=snsapi_base&state=rjxx#wechat_redirect";
			}
		},
		error : function(data) {
			alert(data.errcode + data.errmsg);
		}
	});
}

// 用户同意授权，获取code
function getCode() {
	window.location.href = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=wx9abc729e2b4637ee&redirect_uri=http://fpj.datarj.com/fp.html&response_type=code&scope=snsapi_userinfo&state=rjxx#wechat_redirect";
}
// 通过code换取网页授权access_token
function getToken(code) {
	$.ajax({
		async : false,
		url : '../pjj/getToken',
		data : {
			"appid" : 'wx9abc729e2b4637ee',
			"apiurl" : 'https://api.weixin.qq.com/sns/oauth2/access_token',
			"code" : code
		},
		method : 'post',
		success : function(data) {
			if (data.success) {
				openid = data.openid;
				access_token = data.access_token;
				expires_in = data.expires_in;
				refresh_token = data.refresh_token;
			} else {
				return;
			}
		},
		error : function(data) {
			alert(data.errcode + data.errmsg);
		}
	});
}
// 刷新access_token（如果需要）
function getRefresh() {
	$.ajax({
		async : false,
		url : '../pjj/getRefresh',
		data : {
			"appid" : 'wx9abc729e2b4637ee',
			"apiurl" : 'https://api.weixin.qq.com/sns/oauth2/refresh_token',
			"grant_type" : 'refresh_token',
			"refresh_token" : refresh_token

		},
		method : 'post',
		success : function(data) {
			if (data.success) {
				openid = data.openid;
				access_token = data.access_token;
				expires_in = data.expires_in;
				refresh_token = data.refresh_token;
			} else {
				alert(data.msg);
				return;
			}
		},
		error : function(data) {
			alert(data.errcode + data.errmsg);
		}
	});
}
// 拉取用户信息(需scope为 snsapi_userinfo)
function getUserMsg() {
	$.ajax({
		async : false,
		url : '../pjj/getUserMsg',
		data : {

			"access_token" : access_token,
			"apiurl" : 'https://api.weixin.qq.com/sns/userinfo',
			"openid" : openid

		// ?access_token=ACCESS_TOKEN&openid=OPENID&lang=zh_CN

		},
		method : 'post',
		success : function(data) {
			if (data.success) {
			} else {
//				alert(data.msg);
//				return;
			}
		},
		error : function(data) {
			alert(data.errcode + data.errmsg);
		}
	});
}
// 获取用户发票夹中所以订单
function getJy() {
	$
			.ajax({
				async : false,
				url : '../pjj/getKhjy',
				method : 'post',
				data : {
					
					"rows" : 10,
					"page" : page,
					"gsdm" : gsdm
		
				},
				success : function(data) {
					if (page == 1) {
						$("#all").children("li").remove();
					}
					if (data.fps && data.fps.length > 0) {
						bool = false;
						for (var i = 0; i < data.fps.length; i++) {
							var val = data.fps[i];
							var zt = val.ztbz;
							var fpzt;
							var flag = true;
							if (zt == "0") {
								fpzt = "重开待审";
							}else if (zt == "1") {
								fpzt = "已重开";
							}else if (zt == "2") {
								fpzt = "重开审核未通过";
								flag = false;
							}else if (zt == "3") {
								fpzt = "换开待审";
							}else if (zt == "4") {
								fpzt = "已换开";
							}else if (zt == "5") {
								fpzt = "换开审核未通过";
								flag = false;
							}else{
								zt = "7";
								fpzt = "正常开具";
							}
							var str = '<li class="mui-table-view-cell mui-media">'+
							'<a class="mui-navigate-right" value="'+ val.djh+'">'
								+ ' <img class="mui-media-object mui-pull-left" '
								+ 'src="../images/'+val.gsdm+'.jpg">'
								+'<div class="mui-media-body" style="font-size: 15px;">发票抬头：'
								+ val.gfmc
								+ '<p class="mui-ellipsis">开票金额：'
								+ val.jshj
								+ '元</p><p class="mui-ellipsis">开票时间：'
								+ val.kprq
								+ '</p><p class="mui-ellipsis">发票状态：'
								+ fpzt
								+ '</p>';
							if (!flag) {
								str += '<p class="mui-ellipsis">审核未通过原因：'
									+ val.ckbtgyy
									+ '</p>'
							}
							str += '</div></a></li>';
							if (gsdm == "sqj") {
								$("#all1").append(str);
							}else{
								$("#all").append(str);
							}
							
						}
					} else if (!data.fps && page == 1) {
						bool = true;
						mui.confirm("没有您要查询的发票", "提示", [ '是' ], function(e) {
							
						});
					}else{
						bool = true;
					}
					page = page + 1;
				},
				error : function(data) {
					alert(data.errcode + data.errmsg);
				}
			});
}

// 获取交易流水所开发票
function getFp(val) {
	 $.ajax({
			url : '../pjj/getCkhk',
			method : 'post',
			data : {
				"djh" : val
			},
			success : function(data) {
				if (data.success) {
					if (data.ckhk) {
						if (data.ckhk.ztbz == "2" || data.ckhk.ztbz == "5") {
							window.location.href = '../pjj/saveFp?djh=' + val;
						}else{
							mui.confirm("发票已处理或正在处理，不能操作", "提示", [ '是' ], function(e) {});
						}
					}else{
						window.location.href = '../pjj/saveFp?djh=' + val;
					}
				}
			},
			error : function(data) {
				alert(data.errcode + data.errmsg);
			}
		});
}
